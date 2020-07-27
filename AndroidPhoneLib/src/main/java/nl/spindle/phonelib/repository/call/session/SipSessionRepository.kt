package nl.spindle.phonelib.repository.call.session

import nl.spindle.phonelib.model.Reason
import nl.spindle.phonelib.model.Session

interface SipSessionRepository {
    fun acceptIncoming(session: Session)
    fun declineIncoming(session: Session, reason: Reason)
    fun callTo(number: String): Session?
    fun callTo(number: String, isVideoCall: Boolean): Session?
    fun end(session: Session)
}