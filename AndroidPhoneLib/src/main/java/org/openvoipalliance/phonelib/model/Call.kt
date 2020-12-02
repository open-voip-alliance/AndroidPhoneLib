package org.openvoipalliance.phonelib.model

import org.linphone.core.Call.Dir.Incoming
import org.linphone.core.Call.Dir.Outgoing
import org.linphone.core.Reason
import org.linphone.core.Call as LinphoneCall

class Call(val linphoneCall: LinphoneCall) {

    val quality
        get() = Quality(linphoneCall.averageQuality, linphoneCall.currentQuality)

    val state
        get() = CallState.values().firstOrNull { it.name == linphoneCall.state.toString() }
                    ?: CallState.Unknown

    val displayName
        get() = linphoneCall.remoteAddress?.displayName ?: ""

    val phoneNumber
        get() = linphoneCall.remoteAddress?.username ?: ""

    val duration
        get() = linphoneCall.duration

    val reason
        get() = when (linphoneCall.reason) {
                Reason.None -> org.openvoipalliance.phonelib.model.Reason.NONE
                Reason.NoResponse -> org.openvoipalliance.phonelib.model.Reason.NO_RESPONSE
                Reason.Declined -> org.openvoipalliance.phonelib.model.Reason.DECLINED
                Reason.NotFound -> org.openvoipalliance.phonelib.model.Reason.NOT_FOUND
                Reason.NotAnswered -> org.openvoipalliance.phonelib.model.Reason.NOT_ANSWERED
                Reason.Busy -> org.openvoipalliance.phonelib.model.Reason.BUSY
                Reason.IOError -> org.openvoipalliance.phonelib.model.Reason.IO_ERROR
                Reason.DoNotDisturb -> org.openvoipalliance.phonelib.model.Reason.DO_NOT_DISTURB
                Reason.Unauthorized -> org.openvoipalliance.phonelib.model.Reason.UNAUTHORISED
                Reason.NotAcceptable -> org.openvoipalliance.phonelib.model.Reason.NOT_ACCEPTABLE
                Reason.NoMatch -> org.openvoipalliance.phonelib.model.Reason.NO_MATCH
                Reason.MovedPermanently -> org.openvoipalliance.phonelib.model.Reason.MOVED_PERMANENTLY
                Reason.Gone -> org.openvoipalliance.phonelib.model.Reason.GONE
                Reason.TemporarilyUnavailable -> org.openvoipalliance.phonelib.model.Reason.TEMPORARILY_UNAVAILABLE
                Reason.AddressIncomplete -> org.openvoipalliance.phonelib.model.Reason.ADDRESS_INCOMPLETE
                Reason.NotImplemented -> org.openvoipalliance.phonelib.model.Reason.NOT_IMPLEMENTED
                Reason.BadGateway -> org.openvoipalliance.phonelib.model.Reason.BAD_GATEWAY
                Reason.ServerTimeout -> org.openvoipalliance.phonelib.model.Reason.SERVER_TIMEOUT
                Reason.Unknown -> org.openvoipalliance.phonelib.model.Reason.UNKNOWN
                else -> org.openvoipalliance.phonelib.model.Reason.UNKNOWN
            }

    val callId: String?
        get() = linphoneCall.callLog.callId

    val direction = when (linphoneCall.callLog.dir) {
        Outgoing -> Direction.OUTGOING
        Incoming -> Direction.INCOMING
    }

    val isOnHold: Boolean
        get() = when (state) {
            CallState.Paused -> true
            else -> false
        }
}