package org.openvoipalliance.voiplib.repository.call.session

import org.openvoipalliance.voiplib.model.Call
import org.openvoipalliance.voiplib.model.Reason

internal interface SipSessionRepository {
    fun acceptIncoming(call: Call)
    fun declineIncoming(call: Call, reason: Reason)
    fun callTo(number: String): Call
    fun end(call: Call)
}