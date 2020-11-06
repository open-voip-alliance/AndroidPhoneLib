package org.openvoipalliance.phonelib.repository

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import org.openvoipalliance.phonelib.R
import org.openvoipalliance.phonelib.model.Codec
import org.openvoipalliance.phonelib.model.PathConfigurations
import org.openvoipalliance.phonelib.service.SimpleLinphoneCoreListener
import org.linphone.core.*
import org.linphone.core.LogLevel.*
import org.openvoipalliance.phonelib.config.Config
import org.openvoipalliance.phonelib.model.Call
import org.linphone.core.Call as LinphoneCall
import org.openvoipalliance.phonelib.repository.initialise.LogLevel
import org.openvoipalliance.phonelib.repository.initialise.SessionCallback
import java.io.File
import java.io.IOException
import java.util.*

private const val LINPHONE_DEBUG_TAG = "SIMPLE_LINPHONE"

private const val BITRATE_LIMIT = 36
private const val DOWNLOAD_BANDWIDTH = 0
private const val UPLOAD_BANDWIDTH = 0

class LinphoneCoreInstanceManager(private val mServiceContext: Context): SimpleLinphoneCoreListener, LoggingServiceListener {
    private var destroyed: Boolean = false
    private var pathConfigurations: PathConfigurations = PathConfigurations(mServiceContext.filesDir.absolutePath)
    private var timer: Timer? = null
    lateinit var config: Config
        internal set
    private var linphoneCore: Core? = null

    val initialised: Boolean get() = linphoneCore != null && !destroyed

    val safeLinphoneCore: Core?
        get() {
            return if (initialised) {
                linphoneCore
            } else {
                Log.e(TAG, "Trying to get linphone core while not possible", Exception())
                null
            }
        }

    init {
        Factory.instance().setDebugMode(true, LINPHONE_DEBUG_TAG)
        pathConfigurations = PathConfigurations(mServiceContext.filesDir.absolutePath)
    }

    fun initialiseLinphone(context: Context, config: Config) {
        this.config = config
        Factory.instance().setDebugMode(true, LINPHONE_DEBUG_TAG)
        config.logListener.let { Factory.instance().loggingService.addListener(this) }
        startLibLinphone(context)
    }

    @Synchronized
    private fun startLibLinphone(context: Context) {
        try {
            copyAssetsFromPackage()
            linphoneCore = Factory.instance().createCoreWithConfig(
                    Factory.instance().createConfig(pathConfigurations.linphoneConfigFile),
                    context).apply {
                addListener(this@LinphoneCoreInstanceManager)
                enableDnsSrv(false)
                enableDnsSearch(false)
                start()
            }
            initLibLinphone()

            val task: TimerTask = object : TimerTask() {
                override fun run() {
                    Handler(Looper.getMainLooper()).post {
                        if (destroyed) {
                            cancel()
                            Factory.instance().loggingService.removeListener(this@LinphoneCoreInstanceManager)
                            linphoneCore?.isNetworkReachable = false
                            linphoneCore?.stop()
                            linphoneCore?.removeListener(this@LinphoneCoreInstanceManager)
                            linphoneCore = null
                            return@post
                        }

                        safeLinphoneCore?.iterate()
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
    private fun initLibLinphone() {
        val userConfig = this.config

        linphoneCore?.apply {
            setUserAgent(userConfig.userAgent, null)
            remoteRingbackTone = pathConfigurations.ringSound
            ring = userConfig.ring
            playFile = pathConfigurations.pauseSound
            rootCa = pathConfigurations.linphoneRootCaFile
            remoteRingbackTone = pathConfigurations.ringSound
            isNetworkReachable = true
            enableEchoCancellation(true)
            enableAdaptiveRateControl(true)
            config?.setInt("audio", "codec_bitrate_limit", BITRATE_LIMIT)
            downloadBandwidth = DOWNLOAD_BANDWIDTH
            uploadBandwidth = UPLOAD_BANDWIDTH
        }

        setCodecMime(config.codecs.toSet())
        destroyed = false
    }

    private fun setCodecMime(audioCodecs: Set<Codec>) {
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

    override fun onRegistrationStateChanged(lc: Core?, cfg: ProxyConfig?, cstate: RegistrationState?, message: String?) {

    }

    override fun onCallStateChanged(lc: Core?, linphoneCall: LinphoneCall?, state: LinphoneCall.State, message: String?) {
        Log.e(TAG, "callState: $state, Message: $message")

        val call = Call(linphoneCall ?: return)

        when {
            state === LinphoneCall.State.IncomingReceived -> {
                phoneCallback?.incomingCall(call)
            }
            state === LinphoneCall.State.OutgoingInit -> {
                phoneCallback?.outgoingInit(call)
            }
            state === LinphoneCall.State.Connected -> {
                phoneCallback?.sessionConnected(call)
            }
            state === LinphoneCall.State.End -> {
                phoneCallback?.sessionEnded(call)
            }
            state === LinphoneCall.State.Released -> {
                phoneCallback?.sessionReleased(call)
            }
            state === LinphoneCall.State.Error -> {
                phoneCallback?.error(call)
            }
            else -> {
                phoneCallback?.sessionUpdated(call)
            }
        }
    }

    override fun onGlobalStateChanged(lc: Core?, gstate: GlobalState?, message: String?) {
        super.onGlobalStateChanged(lc, gstate, message)
        gstate?.let { globalState = it }
    }

    @Synchronized
    fun destroy() {
        destroyed = true
    }

    companion object {
        const val TAG = "VOIPLIB-LINPHONE"
        var phoneCallback: SessionCallback? = null
        var globalState: GlobalState = GlobalState.Off
    }

    override fun onLogMessageWritten(service: LoggingService, domain: String, lev: org.linphone.core.LogLevel, message: String) {
        config.logListener?.onLogMessageWritten(when (lev) {
                Debug -> LogLevel.DEBUG
                Trace -> LogLevel.TRACE
                Message -> LogLevel.MESSAGE
                Warning -> LogLevel.WARNING
                Error -> LogLevel.ERROR
                Fatal -> LogLevel.FATAL
            }, message)
    }
}