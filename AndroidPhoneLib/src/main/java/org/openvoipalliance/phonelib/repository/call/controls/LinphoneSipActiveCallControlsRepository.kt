package org.openvoipalliance.phonelib.repository.call.controls

import org.openvoipalliance.phonelib.model.Session
import org.openvoipalliance.phonelib.model.AttendedTransferSession
import org.openvoipalliance.phonelib.repository.LinphoneCoreInstanceManager

class LinphoneSipActiveCallControlsRepository(private val linphoneCoreInstanceManager: LinphoneCoreInstanceManager) : SipActiveCallControlsRepository {

    override fun setMicrophone(on: Boolean) {
        linphoneCoreInstanceManager.safeLinphoneCore?.enableMic(on)
    }

    override fun setHold(session: Session, on: Boolean) {
        if (on) {
            session.linphoneCall.pause()
        } else {
            session.linphoneCall.resume()
        }
    }

    override fun isMicrophoneMuted(): Boolean {
        return linphoneCoreInstanceManager.safeLinphoneCore?.micEnabled() ?: false
    }

    override fun transferUnattended(session: Session, to: String) {
        session.linphoneCall.transfer(to)
    }

    override fun finishAttendedTransfer(attendedTransferSession: AttendedTransferSession) {
        attendedTransferSession.from.linphoneCall.transferToAnother(attendedTransferSession.to.linphoneCall)
    }

    override fun pauseSession(session: Session) {
        session.linphoneCall.pause()
    }

    override fun resumeSession(session: Session) {
        session.linphoneCall.resume()
    }

    override fun switchSession(from: Session, to: Session) {
        from.linphoneCall.pause()
        to.linphoneCall.resume()
    }
}