package org.openvoipalliance.phonelib.repository.call.session

import org.openvoipalliance.phonelib.model.Reason
import org.openvoipalliance.phonelib.model.Call

interface SipSessionRepository {
    fun acceptIncoming(call: Call)
    fun declineIncoming(call: Call, reason: Reason)
    fun callTo(number: String): Call?
    fun callTo(number: String, isVideoCall: Boolean): Call?
    fun end(call: Call)
}