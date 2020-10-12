package org.openvoipalliance.phonelib.repository

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import org.openvoipalliance.phonelib.R
import org.openvoipalliance.phonelib.model.Codec
import org.openvoipalliance.phonelib.model.PathConfigurations
import org.openvoipalliance.phonelib.service.SimpleLinphoneCoreListener
import org.linphone.core.*
import org.linphone.core.LogLevel.*
import org.linphone.mediastream.Log
import org.openvoipalliance.phonelib.repository.initialise.LogLevel
import org.openvoipalliance.phonelib.repository.initialise.LogListener
import java.io.File
import java.io.IOException
import java.util.*

private const val TAG = "LinphoneManager"
private const val LINPHONE_DEBUG_TAG = "SIMPLE_LINPHONE"

private const val BITRATE_LIMIT = 36
private const val DOWNLOAD_BANDWIDTH = 1536
private const val UPLOAD_BANDWIDTH = 1536

class LinphoneCoreInstanceManager(private val mServiceContext: Context) : SimpleLinphoneCoreListener {
    private var destroyed: Boolean = false
    private var pathConfigurations: PathConfigurations
    private var timer: Timer? = null

    private var linphoneCore: Core? = null

    val initialised: Boolean get() = linphoneCore != null && !destroyed
    val safeLinphoneCore: Core?
        get() {
            return if (initialised) {
                linphoneCore
            } else {
                Log.e("Trying to get linphone core while not possible")
                null
            }
        }

    init {
        Factory.instance().setDebugMode(true, LINPHONE_DEBUG_TAG)

        pathConfigurations = PathConfigurations(mServiceContext.filesDir.absolutePath)
    }

    fun initialiseLinphone(context: Context, audioCodecs: Set<Codec>, listener: LogListener?) {
        Factory.instance().loggingService.addListener { _, _, lev, message ->
            listener?.onLogMessageWritten(when (lev) {
                Debug -> LogLevel.DEBUG
                Trace -> LogLevel.TRACE
                Message -> LogLevel.MESSAGE
                Warning -> LogLevel.WARNING
                Error -> LogLevel.ERROR
                Fatal -> LogLevel.FATAL
            }, message)
        }

        if (linphoneCore == null) {
            startLibLinphone(context, audioCodecs)
        }
    }

    @Synchronized
    private fun startLibLinphone(context: Context, audioCodecs: Set<Codec>) {
        try {
            copyAssetsFromPackage()
            linphoneCore = Factory.instance().createCoreWithConfig(getConfig(), context)
            linphoneCore?.addListener(context as CoreListener)
            try {
                initLibLinphone(audioCodecs)
            } catch (e: CoreException) {
                Log.e(e)
                return
            }
            val task: TimerTask = object : TimerTask() {
                override fun run() {
                    Handler(Looper.getMainLooper()).post {
                        linphoneCore?.iterate()
                    }
                }
            }
            timer = Timer("Linphone Scheduler")
            timer?.schedule(task, 0, 20)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "startLibLinphone: cannot start linphone")
        }
    }

    @Synchronized
    @Throws(CoreException::class)
    private fun initLibLinphone(audioCodecs: Set<Codec>) {
        setUserAgent(null)

        linphoneCore?.remoteRingbackTone = pathConfigurations.ringSound
        linphoneCore?.ring = pathConfigurations.ringSound

        linphoneCore?.playFile = pathConfigurations.pauseSound
        linphoneCore?.rootCa = pathConfigurations.linphoneRootCaFile
        linphoneCore?.remoteRingbackTone = pathConfigurations.ringSound

        val migrationResult = linphoneCore!!.migrateToMultiTransport()
        Log.d(TAG, "Migration to multi transport result = $migrationResult")
        linphoneCore?.isNetworkReachable = true
        linphoneCore?.enableEchoCancellation(true)
        linphoneCore?.enableAdaptiveRateControl(true)
        getConfig().setInt("audio", "codec_bitrate_limit", BITRATE_LIMIT)
        linphoneCore?.downloadBandwidth = DOWNLOAD_BANDWIDTH
        linphoneCore?.uploadBandwidth = UPLOAD_BANDWIDTH
        setCodecMime(audioCodecs)
        linphoneCore?.start()
    }

    fun setCodecMime(audioCodecs: Set<Codec>) {
        linphoneCore?.let {
            for (payloadType in it.audioPayloadTypes) {
                payloadType.enable(audioCodecs.contains(Codec.valueOf(payloadType.mimeType.toUpperCase(Locale.ROOT))))
            }
        }
    }

    @Throws(IOException::class)
    private fun copyAssetsFromPackage() {
        copyIfNotExist(mServiceContext, R.raw.oldphone_mono, pathConfigurations.ringSound)
        copyIfNotExist(mServiceContext, R.raw.ringback, pathConfigurations.ringBackSound)
        copyIfNotExist(mServiceContext, R.raw.toy_mono, pathConfigurations.pauseSound)
        copyIfNotExist(mServiceContext, R.raw.linphonerc_default, pathConfigurations.linphoneConfigFile)
        copyIfNotExist(mServiceContext, R.raw.linphonerc_factory, File(pathConfigurations.linphoneFactoryConfigFile).name)
        copyIfNotExist(mServiceContext, R.raw.lpconfig, pathConfigurations.linphoneConfigXsp)
        copyIfNotExist(mServiceContext, R.raw.rootca, pathConfigurations.linphoneRootCaFile)
    }

    @Suppress("DEPRECATION")
    internal fun setUserAgent(userAgent: String?) {
        try {
            val versionName = userAgent
                    ?: mServiceContext.packageManager.getPackageInfo(mServiceContext.packageName,
                            0).versionName ?: if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        mServiceContext.packageManager.getPackageInfo(mServiceContext.packageName, 0).longVersionCode.toString()
                    } else {
                        mServiceContext.packageManager.getPackageInfo(mServiceContext.packageName, 0).versionCode.toString()
                    }

            linphoneCore?.setUserAgent(System.getProperty("http.agent"), versionName)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
    }

    @Throws(IOException::class)
    fun copyIfNotExist(context: Context, resourceId: Int, target: String?) {
        val fileToCopy = File(target ?: "")
        if (!fileToCopy.exists()) {
            copyFromPackage(context, resourceId, fileToCopy.name)
        }
    }

    @Throws(IOException::class)
    fun copyFromPackage(context: Context, resourceId: Int, target: String?) {
        val outputStream = context.openFileOutput(target, 0)
        val inputStream = context.resources.openRawResource(resourceId)
        var readByte: Int
        val buff = ByteArray(8048)
        while (inputStream.read(buff).also { readByte = it } != -1) {
            outputStream.write(buff, 0, readByte)
        }
        outputStream.flush()
        outputStream.close()
        inputStream.close()
    }

    private fun getConfig(): Config {
        return safeLinphoneCore?.config
                ?: Factory.instance().createConfig(pathConfigurations.linphoneConfigFile)
    }

    @Synchronized
    fun destroy() {
        destroyed = true
        doDestroy()
    }

    private fun doDestroy() {
        try {
            timer?.cancel()
        } catch (e: RuntimeException) {
            e.printStackTrace()
        } finally {
            linphoneCore = null
        }
    }

    override fun onRegistrationStateChanged(lc: Core?, cfg: ProxyConfig?, cstate: RegistrationState?, message: String?) {

    }

    override fun onCallStateChanged(lc: Core?, call: Call?, cstate: Call.State?, message: String?) {

    }
}