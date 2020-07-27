package nl.spindle.phonelib.repository.call.controls

import nl.spindle.phonelib.model.Session
import nl.spindle.phonelib.repository.LinphoneCoreInstanceManager

class LinphoneSipActiveCallControlsRepository(private val linphoneCoreInstanceManager: LinphoneCoreInstanceManager) : SipActiveCallControlsRepository {

    override fun setMicrophone(on: Boolean) {
        linphoneCoreInstanceManager.safeLinphoneCore?.muteMic(on)
    }

    override fun setSpeaker(on: Boolean) {
        linphoneCoreInstanceManager.safeLinphoneCore?.enableSpeaker(on)
    }

    override fun setHold(session: Session, on: Boolean) {
        if (on) {
            linphoneCoreInstanceManager.safeLinphoneCore?.pauseCall(session.linphoneCall)
        } else {
            linphoneCoreInstanceManager.safeLinphoneCore?.resumeCall(session.linphoneCall)
        }
    }

    override fun isMicrophoneMuted(): Boolean {
        return linphoneCoreInstanceManager.safeLinphoneCore?.isMicMuted ?: false
    }

    override fun isSpeakerEnabled(): Boolean {
        return linphoneCoreInstanceManager.safeLinphoneCore?.isSpeakerEnabled ?: false
    }
}