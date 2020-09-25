package nl.spindle.phonelib.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import nl.spindle.phonelib.model.Session
import nl.spindle.phonelib.repository.LinphoneCoreInstanceManager
import nl.spindle.phonelib.repository.call.codecs.SipConfigurationsRepository
import nl.spindle.phonelib.repository.initialise.SessionCallback
import nl.spindle.phonelib.repository.registration.RegistrationCallback
import org.koin.android.ext.android.inject
import org.linphone.core.*

class LinphoneService : Service(), SimpleLinphoneCoreListener {
    private val linphoneCoreInstanceManager: LinphoneCoreInstanceManager by inject()
    private val sipCodecsRepository: SipConfigurationsRepository by inject()

    override fun onCreate() {
        super.onCreate()
        Factory.instance()
        linphoneCoreInstanceManager.initialiseLinphone(this@LinphoneService, sipCodecsRepository.getAudioCodecs())
        instance = this
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e(TAG, "LinphoneService onDestroy execute")
        removeAllCallbacks()
        linphoneCoreInstanceManager.destroy()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun removeAllCallbacks() {
        removePhoneCallback()
        removeRegistrationCallback()
    }

    override fun onRegistrationStateChanged(lc: Core?, cfg: ProxyConfig?, cstate: RegistrationState?, message: String?) {
        sRegistrationCallback?.stateChanged(when (cstate) {
            RegistrationState.None -> {
                nl.spindle.phonelib.model.RegistrationState.NONE
            }
            RegistrationState.Progress -> {
                nl.spindle.phonelib.model.RegistrationState.PROGRESS
            }
            RegistrationState.Ok -> {
                nl.spindle.phonelib.model.RegistrationState.REGISTERED
            }
            RegistrationState.Cleared -> {
                nl.spindle.phonelib.model.RegistrationState.CLEARED
            }
            RegistrationState.Failed -> {
                nl.spindle.phonelib.model.RegistrationState.FAILED
            }
            else -> {
                nl.spindle.phonelib.model.RegistrationState.UNKNOWN
            }
        })
    }


    override fun onCallStateChanged(lc: Core?, linphoneCall: Call?, state: Call.State?, message: String?) {
        Log.e(TAG, "callState: $state, Message: $message")
        when {
            state === Call.State.IncomingReceived -> {
                sPhoneCallback?.incomingCall(Session(linphoneCall!!))
            }
            state === Call.State.OutgoingInit -> {
                sPhoneCallback?.outgoingInit(Session(linphoneCall!!))
            }
            state === Call.State.Connected -> {
                sPhoneCallback?.sessionConnected(Session(linphoneCall!!))
            }
            state === Call.State.End -> {
                sPhoneCallback?.sessionEnded(Session(linphoneCall!!))
            }
            state === Call.State.Released -> {
                sPhoneCallback?.sessionReleased(Session(linphoneCall!!))
            }
            state === Call.State.Error -> {
                sPhoneCallback?.error(Session(linphoneCall!!))
            }
            else -> {
                sPhoneCallback?.sessionUpdated(Session(linphoneCall!!))
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
            Thread {
                while (!isInitialised) {
                    try {
                        Thread.sleep(80)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                }
                ready()
            }.start()
        }
    }
}