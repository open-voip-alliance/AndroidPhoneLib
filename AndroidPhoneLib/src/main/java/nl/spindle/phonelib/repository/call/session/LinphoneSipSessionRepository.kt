package nl.spindle.phonelib.repository.call.session

import android.util.Log
import nl.spindle.phonelib.model.Reason
import nl.spindle.phonelib.model.Session
import nl.spindle.phonelib.model.SoftPhone
import nl.spindle.phonelib.repository.LinphoneCoreInstanceManager
import nl.spindle.phonelib.service.LinphoneService
import org.linphone.core.LinphoneAddress
import org.linphone.core.LinphoneCall
import org.linphone.core.LinphoneCoreException

private const val TAG = "LinphoneSipSession"

class LinphoneSipSessionRepository(private val linphoneCoreInstanceManager: LinphoneCoreInstanceManager) : SipSessionRepository {
    init {
        linphoneCoreInstanceManager.safeLinphoneCore?.enableEchoCancellation(true)
        linphoneCoreInstanceManager.safeLinphoneCore?.enableEchoLimiter(true)
    }

    override fun acceptIncoming(session: Session) {
        try {
            linphoneCoreInstanceManager.safeLinphoneCore?.acceptCall(session.linphoneCall)
        } catch (e: LinphoneCoreException) {
            e.printStackTrace()
        }
    }

    override fun declineIncoming(session: Session, reason: Reason) {
        try {
            linphoneCoreInstanceManager.safeLinphoneCore?.declineCall(session.linphoneCall, org.linphone.core.Reason.fromInt(reason.value))
        } catch (e: LinphoneCoreException) {
            e.printStackTrace()
        }
    }


    override fun callTo(number: String) : Session? {
        return callTo(number, false)
    }

    override fun callTo(number: String, isVideoCall: Boolean) : Session? {
        if (!LinphoneService.isInitialised || !linphoneCoreInstanceManager.initialised) {
            Log.e(TAG, "The LinphoneService isn't ready")
            return null
        }
        if (number.isEmpty()) {
            Log.e(TAG, "The entered phone number is empty")
            return null
        }
        val phone = SoftPhone()
        phone.userName = number
        phone.host = LinphoneService.sServerIP
        callTo(phone, isVideoCall)?.let { return Session(it) }
        return null
    }

    private fun callTo(bean: SoftPhone, isVideoCall: Boolean) : LinphoneCall? {
        val address: LinphoneAddress
        var call: LinphoneCall? = null
        address = try {
            linphoneCoreInstanceManager.safeLinphoneCore!!.interpretUrl(bean.userName + "@" + bean.host)
        } catch (e: LinphoneCoreException) {
            e.printStackTrace()
            return null
        }
        address.displayName = bean.displayName

        val params = linphoneCoreInstanceManager.safeLinphoneCore?.createCallParams(null)
        params?.videoEnabled = isVideoCall
        if (isVideoCall) {
            params?.enableLowBandwidth(false)
        }
        try {
            call = linphoneCoreInstanceManager.safeLinphoneCore?.inviteAddressWithParams(address, params)
        } catch (e: LinphoneCoreException) {
            e.printStackTrace()
        }
        return call
    }

    override fun end(session: Session) {
        linphoneCoreInstanceManager.safeLinphoneCore?.terminateCall(session.linphoneCall)
        if (linphoneCoreInstanceManager.safeLinphoneCore?.isInConference == true) {
            linphoneCoreInstanceManager.safeLinphoneCore?.terminateConference()
        }
    }
}