package org.openvoipalliance.phonelib.repository.call.session

import android.util.Log
import org.openvoipalliance.phonelib.model.Reason
import org.openvoipalliance.phonelib.model.SoftPhone
import org.openvoipalliance.phonelib.repository.LinphoneCoreInstanceManager
import org.linphone.core.Address
import org.linphone.core.CoreException
import org.openvoipalliance.phonelib.model.Call
import org.linphone.core.Call as LinphoneCall

private const val TAG = "LinphoneSipSession"

class LinphoneSipSessionRepository(private val linphoneCoreInstanceManager: LinphoneCoreInstanceManager) : SipSessionRepository {
    init {
        linphoneCoreInstanceManager.safeLinphoneCore?.enableEchoCancellation(true)
        linphoneCoreInstanceManager.safeLinphoneCore?.enableEchoLimiter(true)
    }

    override fun acceptIncoming(call: Call) {
        try {
            linphoneCoreInstanceManager.safeLinphoneCore?.acceptCall(call.linphoneCall)
        } catch (e: CoreException) {
            e.printStackTrace()
        }
    }

    override fun declineIncoming(call: Call, reason: Reason) {
        try {
            linphoneCoreInstanceManager.safeLinphoneCore?.declineCall(call.linphoneCall, org.linphone.core.Reason.fromInt(reason.value))
        } catch (e: CoreException) {
            e.printStackTrace()
        }
    }


    override fun callTo(number: String) : Call? {
        return callTo(number, false)
    }

    override fun callTo(number: String, isVideoCall: Boolean) : Call? {
        if (!linphoneCoreInstanceManager.initialised) {
            Log.e(TAG, "The LinphoneService isn't ready")
            return null
        }
        if (number.isEmpty()) {
            Log.e(TAG, "The entered phone number is empty")
            return null
        }
        val phone = SoftPhone()
        phone.userName = number
        phone.host = linphoneCoreInstanceManager.config.auth.domain
        callTo(phone, isVideoCall)?.let { return Call(it) }
        return null
    }

    private fun callTo(bean: SoftPhone, isVideoCall: Boolean) : LinphoneCall? {
        val address: Address
        var call: LinphoneCall? = null
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

    override fun end(call: Call) {
        call.linphoneCall.terminate()
        if (linphoneCoreInstanceManager.safeLinphoneCore?.isInConference == true) {
            linphoneCoreInstanceManager.safeLinphoneCore?.terminateConference()
        }
    }
}