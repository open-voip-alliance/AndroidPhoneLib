package org.openvoipalliance.phonelib.repository.call.session

import org.openvoipalliance.phonelib.model.Call
import org.openvoipalliance.phonelib.model.Reason

internal interface SipSessionRepository {
    fun acceptIncoming(call: Call)
    fun declineIncoming(call: Call, reason: Reason)
    fun callTo(number: String): Call
    fun end(call: Call)
}