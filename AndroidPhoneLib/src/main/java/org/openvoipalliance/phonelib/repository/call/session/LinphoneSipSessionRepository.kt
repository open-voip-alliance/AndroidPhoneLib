package org.openvoipalliance.phonelib.repository.call.session

import android.util.Log
import org.openvoipalliance.phonelib.model.Reason
import org.openvoipalliance.phonelib.model.Session
import org.openvoipalliance.phonelib.model.SoftPhone
import org.openvoipalliance.phonelib.repository.LinphoneCoreInstanceManager
import org.openvoipalliance.phonelib.service.LinphoneService
import org.linphone.core.Address
import org.linphone.core.Call
import org.linphone.core.CoreException

private const val TAG = "LinphoneSipSession"

class LinphoneSipSessionRepository(private val linphoneCoreInstanceManager: LinphoneCoreInstanceManager) : SipSessionRepository {
    init {
        linphoneCoreInstanceManager.safeLinphoneCore?.enableEchoCancellation(true)
        linphoneCoreInstanceManager.safeLinphoneCore?.enableEchoLimiter(true)
    }

    override fun acceptIncoming(session: Session) {
        try {
            linphoneCoreInstanceManager.safeLinphoneCore?.acceptCall(session.linphoneCall)
        } catch (e: CoreException) {
            e.printStackTrace()
        }
    }

    override fun declineIncoming(session: Session, reason: Reason) {
        try {
            linphoneCoreInstanceManager.safeLinphoneCore?.declineCall(session.linphoneCall, org.linphone.core.Reason.fromInt(reason.value))
        } catch (e: CoreException) {
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

    private fun callTo(bean: SoftPhone, isVideoCall: Boolean) : Call? {
        val address: Address
        var call: Call? = null
        address = try {
            linphoneCoreInstanceManager.safeLinphoneCore!!.interpretUrl(bean.userName + "@" + bean.host)!!
        } catch (e: CoreException) {
            e.printStackTrace()
            return null
        }
        address.displayName = bean.displayName

        val params = linphoneCoreInstanceManager.safeLinphoneCore?.createCallParams(null) ?: return null
        params.enableVideo(isVideoCall)
        if (isVideoCall) {
            params.enableLowBandwidth(false)
        }

        try {
            call = linphoneCoreInstanceManager.safeLinphoneCore?.inviteAddressWithParams(address, params)
        } catch (e: CoreException) {
            e.printStackTrace()
        }
        return call
    }

    override fun end(session: Session) {
        session.linphoneCall.terminate()
        if (linphoneCoreInstanceManager.safeLinphoneCore?.isInConference == true) {
            linphoneCoreInstanceManager.safeLinphoneCore?.terminateConference()
        }
    }
}