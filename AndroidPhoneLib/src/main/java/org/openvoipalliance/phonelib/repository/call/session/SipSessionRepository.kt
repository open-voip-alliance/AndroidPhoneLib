package org.openvoipalliance.phonelib.repository.call.session

import org.openvoipalliance.phonelib.model.Reason
import org.openvoipalliance.phonelib.model.Session

interface SipSessionRepository {
    fun acceptIncoming(session: Session)
    fun declineIncoming(session: Session, reason: Reason)
    fun callTo(number: String): Session?
    fun callTo(number: String, isVideoCall: Boolean): Session?
    fun end(session: Session)
}