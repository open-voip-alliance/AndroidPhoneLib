package nl.spindle.phonelib.repository

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import nl.spindle.phonelib.R
import nl.spindle.phonelib.model.Codec
import nl.spindle.phonelib.model.PathConfigurations
import nl.spindle.phonelib.service.SimpleLinphoneCoreListener
import org.linphone.core.*
import org.linphone.mediastream.Log
import java.io.File
import java.io.IOException
import java.util.*

private const val TAG = "LinphoneManager"
private const val LINPHONE_DEBUG_TAG = "SIMPLE_LINPHONE"

private const val BACK_CAM = 0
private const val BITRATE_LIMIT = 36
private const val DOWNLOAD_BANDWIDTH = 1536
private const val PREFERRED_VIDEO_SIZE = "720p"
private const val UPLOAD_BANDWIDTH = 1536

class LinphoneCoreInstanceManager(private val mServiceContext: Context) : SimpleLinphoneCoreListener {
    private var destroyed: Boolean = false
    private var pathConfigurations: PathConfigurations
    private var timer: Timer? = null

    private var linphoneCore: LinphoneCore? = null
    val initialised: Boolean get() = linphoneCore != null && !destroyed
    val safeLinphoneCore: LinphoneCore?
        get() {
            return if (initialised) {
                linphoneCore
            } else {
                Log.e("Trying to get linphone core while not possible")
                null
            }
        }

    init {
        LinphoneCoreFactory.instance().setDebugMode(true, LINPHONE_DEBUG_TAG)
        pathConfigurations = PathConfigurations(mServiceContext.filesDir.absolutePath)
    }

    fun initialiseLinphone(context: Context, audioCodecs: Set<Codec>) {
        if (linphoneCore == null) {
            startLibLinphone(context, audioCodecs)
        }
    }

    @Synchronized
    private fun startLibLinphone(context: Context, audioCodecs: Set<Codec>) {
        try {
            copyAssetsFromPackage()
            linphoneCore = LinphoneCoreFactory.instance().createLinphoneCore(this, pathConfigurations.linphoneConfigFile,
                    pathConfigurations.linphoneFactoryConfigFile, null, context)
            linphoneCore?.addListener(context as LinphoneCoreListener)
            try {
                initLibLinphone(audioCodecs)
            } catch (e: LinphoneCoreException) {
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
    @Throws(LinphoneCoreException::class)
    private fun initLibLinphone(audioCodecs: Set<Codec>) {
        linphoneCore?.setContext(mServiceContext)
        setUserAgent()
        linphoneCore?.videoDevice = BACK_CAM
        linphoneCore?.remoteRingbackTone = pathConfigurations.ringSound
        linphoneCore?.ring = pathConfigurations.ringSound

        linphoneCore?.setChatDatabasePath(pathConfigurations.linphoneChatDatabaseFile)
        linphoneCore?.setCpuCount(Runtime.getRuntime().availableProcessors())
        linphoneCore?.setPlayFile(pathConfigurations.pauseSound)
        linphoneCore?.setRootCA(pathConfigurations.linphoneRootCaFile)
        linphoneCore?.setTone(ToneID.CallWaiting, pathConfigurations.ringSound)

        val migrationResult = linphoneCore!!.migrateToMultiTransport()
        Log.d(TAG, "Migration to multi transport result = $migrationResult")
        linphoneCore?.isNetworkReachable = true
        linphoneCore?.enableEchoCancellation(true)
        linphoneCore?.enableAdaptiveRateControl(true)
        getConfig().setInt("audio", "codec_bitrate_limit", BITRATE_LIMIT)
        linphoneCore?.setPreferredVideoSizeByName(PREFERRED_VIDEO_SIZE)
        linphoneCore?.downloadBandwidth = DOWNLOAD_BANDWIDTH
        linphoneCore?.uploadBandwidth = UPLOAD_BANDWIDTH
        linphoneCore?.setVideoPolicy(linphoneCore!!.videoAutoInitiatePolicy, linphoneCore!!.videoAutoAcceptPolicy)
        linphoneCore?.enableVideo(true, true)
        setCodecMime(audioCodecs)
    }

    private fun setCodecMime(audioCodecs: Set<Codec>) {
        for (payloadType in linphoneCore!!.audioCodecs) {
            try {
                linphoneCore?.enablePayloadType(payloadType, audioCodecs.contains(Codec.valueOf(payloadType.mime.toUpperCase(Locale.ROOT))))
            } catch (e: LinphoneCoreException) {
                e.printStackTrace()
            }
        }
        for (payloadType in linphoneCore!!.videoCodecs) {
            try {
                Log.e(TAG, "setCodecMime: mime: " + payloadType.mime + " rate: " + payloadType.rate)
                linphoneCore?.enablePayloadType(payloadType, true)
            } catch (e: LinphoneCoreException) {
                e.printStackTrace()
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

    private fun setUserAgent() {
        try {
            val versionName = mServiceContext.packageManager.getPackageInfo(mServiceContext.packageName,
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

    private fun getConfig(): LpConfig {
        return safeLinphoneCore?.config
                ?: LinphoneCoreFactory.instance().createLpConfig(pathConfigurations.linphoneConfigFile)
    }

    @Synchronized
    fun destroy() {
        destroyed = true
        doDestroy()
    }

    private fun doDestroy() {
        try {
            timer?.cancel()
            linphoneCore?.destroy()
        } catch (e: RuntimeException) {
            e.printStackTrace()
        } finally {
            linphoneCore = null
        }
    }
}