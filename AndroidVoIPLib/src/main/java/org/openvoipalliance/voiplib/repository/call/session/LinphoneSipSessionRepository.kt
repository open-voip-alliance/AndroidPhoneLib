package org.openvoipalliance.voiplib.repository.call.session

import org.linphone.core.Address
import org.linphone.core.CoreException
import org.openvoipalliance.voiplib.model.Call
import org.openvoipalliance.voiplib.model.Reason
import org.openvoipalliance.voiplib.model.SoftPhone
import org.openvoipalliance.voiplib.repository.LinphoneCoreInstanceManager
import org.linphone.core.Call as LinphoneCall

private const val TAG = "LinphoneSipSession"

internal class LinphoneSipSessionRepository(private val linphoneCoreInstanceManager: LinphoneCoreInstanceManager) : SipSessionRepository {
    init {
        linphoneCoreInstanceManager.safeLinphoneCore?.enableEchoCancellation(true)
        linphoneCoreInstanceManager.safeLinphoneCore?.enableEchoLimiter(true)
    }

    override fun acceptIncoming(call: Call) {
        try {
            call.linphoneCall.accept()
        } catch (e: CoreException) {
            e.printStackTrace()
        }
    }

    override fun declineIncoming(call: Call, reason: Reason) {
        try {
            call.linphoneCall.decline(org.linphone.core.Reason.fromInt(reason.value))
        } catch (e: CoreException) {
            e.printStackTrace()
        }
    }


    override fun callTo(number: String): Call {
        return callTo(number, false)
    }

    private fun callTo(number: String, isVideoCall: Boolean): Call {
        if (!linphoneCoreInstanceManager.initialised) {
            throw Exception("Linphone is not ready")
        }
        if (number.isEmpty()) {
            throw IllegalArgumentException("Phone number is not valid")
        }
        val phone = SoftPhone()
        phone.userName = number
        phone.host = linphoneCoreInstanceManager.config.auth.domain

        return Call(callTo(phone, isVideoCall) ?: throw Exception("Call failed"))
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