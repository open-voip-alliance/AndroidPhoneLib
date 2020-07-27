package nl.spindle.phonelib.repository.call.controls

import nl.spindle.phonelib.model.Session

interface SipActiveCallControlsRepository {
    fun setMicrophone(on: Boolean)
    fun setSpeaker(on: Boolean)
    fun setHold(session: Session, on: Boolean)

    fun isMicrophoneMuted(): Boolean
    fun isSpeakerEnabled(): Boolean
}