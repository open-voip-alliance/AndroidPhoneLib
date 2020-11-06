package org.openvoipalliance.phonelib.repository.call.controls

import org.openvoipalliance.phonelib.model.Call
import org.openvoipalliance.phonelib.model.AttendedTransferSession
import org.openvoipalliance.phonelib.repository.LinphoneCoreInstanceManager

class LinphoneSipActiveCallControlsRepository(private val linphoneCoreInstanceManager: LinphoneCoreInstanceManager) : SipActiveCallControlsRepository {

    override fun setMicrophone(on: Boolean) {
        linphoneCoreInstanceManager.safeLinphoneCore?.enableMic(on)
    }

    override fun setHold(call: Call, on: Boolean) {
        if (on) {
            call.linphoneCall.pause()
        } else {
            call.linphoneCall.resume()
        }
    }

    override fun isMicrophoneMuted(): Boolean {
        val core = linphoneCoreInstanceManager.safeLinphoneCore ?: return false

        return !core.micEnabled()
    }

    override fun transferUnattended(call: Call, to: String) {
        call.linphoneCall.transfer(to)
    }

    override fun finishAttendedTransfer(attendedTransferSession: AttendedTransferSession) {
        attendedTransferSession.from.linphoneCall.transferToAnother(attendedTransferSession.to.linphoneCall)
    }

    override fun pauseCall(call: Call) {
        call.linphoneCall.pause()
    }

    override fun resumeCall(call: Call) {
        call.linphoneCall.resume()
    }

    override fun sendDtmf(call: Call, dtmf: String) {
        if (dtmf.length == 1) {
            call.linphoneCall.sendDtmf(dtmf[0])
        } else {
            call.linphoneCall.sendDtmfs(dtmf)
        }
    }

    override fun switchCall(from: Call, to: Call) {
        from.linphoneCall.pause()
        to.linphoneCall.resume()
    }
}