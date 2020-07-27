package nl.spindle.phonelib.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import nl.spindle.phonelib.model.Session
import nl.spindle.phonelib.repository.LinphoneCoreInstanceManager
import nl.spindle.phonelib.repository.call.codecs.SipCodecsRepository
import nl.spindle.phonelib.repository.initialise.SessionCallback
import nl.spindle.phonelib.repository.registration.RegistrationCallback
import org.koin.android.ext.android.inject
import org.linphone.core.LinphoneCall
import org.linphone.core.LinphoneCore
import org.linphone.core.LinphoneCore.RegistrationState
import org.linphone.core.LinphoneCoreFactoryImpl
import org.linphone.core.LinphoneProxyConfig

class LinphoneService : Service(), SimpleLinphoneCoreListener {
    private val linphoneCoreInstanceManager: LinphoneCoreInstanceManager by inject()
    private val sipCodecsRepository: SipCodecsRepository by inject()

    override fun onCreate() {
        super.onCreate()
        LinphoneCoreFactoryImpl.instance()
        linphoneCoreInstanceManager.initialiseLinphone(this@LinphoneService, sipCodecsRepository.getAudioCodecs( this@LinphoneService))
        instance = this
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e(TAG, "LinphoneService onDestroy execute")
        removeAllCallbacks()
        linphoneCoreInstanceManager.safeLinphoneCore?.destroy()
        linphoneCoreInstanceManager.destroy()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun removeAllCallbacks() {
        removePhoneCallback()
        removeRegistrationCallback()
    }

    override fun registrationState(linphoneCore: LinphoneCore, linphoneProxyConfig: LinphoneProxyConfig,
                                   registrationState: RegistrationState, s: String) {
        sRegistrationCallback?.stateChanged(when (registrationState) {
            RegistrationState.RegistrationNone -> {
                nl.spindle.phonelib.model.RegistrationState.NONE
            }
            RegistrationState.RegistrationProgress -> {
                nl.spindle.phonelib.model.RegistrationState.PROGRESS
            }
            RegistrationState.RegistrationOk -> {
                nl.spindle.phonelib.model.RegistrationState.REGISTERED
            }
            RegistrationState.RegistrationCleared -> {
                nl.spindle.phonelib.model.RegistrationState.CLEARED
            }
            RegistrationState.RegistrationFailed -> {
                nl.spindle.phonelib.model.RegistrationState.FAILED
            }
            else -> {
                nl.spindle.phonelib.model.RegistrationState.UNKNOWN
            }
        })
    }

    override fun callState(linphoneCore: LinphoneCore, linphoneCall: LinphoneCall, state: LinphoneCall.State, s: String) {
        Log.e(TAG, "callState: $state")
        when {
            state === LinphoneCall.State.IncomingReceived -> {
                sPhoneCallback?.incomingCall(Session(linphoneCall))
            }
            state === LinphoneCall.State.OutgoingInit -> {
                sPhoneCallback?.outgoingInit(Session(linphoneCall))
            }
            state === LinphoneCall.State.Connected -> {
                sPhoneCallback?.sessionConnected(Session(linphoneCall))
            }
            state === LinphoneCall.State.CallEnd -> {
                sPhoneCallback?.sessionEnded(Session(linphoneCall))
            }
            state === LinphoneCall.State.CallReleased -> {
                sPhoneCallback?.sessionReleased(Session(linphoneCall))
            }
            state === LinphoneCall.State.Error -> {
                sPhoneCallback?.error(Session(linphoneCall))
            }
            else -> {
                sPhoneCallback?.sessionUpdated(Session(linphoneCall))
            }
        }
    }

    companion object {
        private const val TAG = "LinphoneService"
        private var instance: LinphoneService? = null
        private var sPhoneCallback: SessionCallback? = null
        private var sRegistrationCallback: RegistrationCallback? = null
        internal var sServerIP: String? = null
        val isInitialised: Boolean get() = instance != null

        fun setPhoneCallback(phoneCallback: SessionCallback?) {
            ensureReady {
                sPhoneCallback = phoneCallback
            }
        }

        fun removePhoneCallback() {
            sPhoneCallback = null
        }

        fun setRegistrationCallback(registrationCallback: RegistrationCallback?) {
            sRegistrationCallback = registrationCallback
        }

        fun removeRegistrationCallback() {
            sRegistrationCallback = null
        }

        fun ensureReady(ready: () -> Unit) {
            Thread(Runnable {
                while (!isInitialised) {
                    try {
                        Thread.sleep(80)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                }
                ready()
            }).start()
        }
    }
}