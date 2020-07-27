package nl.spindle.phonelib.model

import org.linphone.core.LinphoneCall
import org.linphone.core.Reason

class Session(val linphoneCall: LinphoneCall) {

    val getState: CallState
        get() {
            return CallState.values().firstOrNull { it.name == linphoneCall.state.toString() }
                    ?: CallState.Unknown
        }

    val getDisplayName: String
        get() {
            return linphoneCall.remoteAddress.displayName
        }

    val getPhoneNumber: String
        get() {
            return linphoneCall.remoteAddress.userName
        }

    val getDuration: Int
        get() {
            return linphoneCall.duration
        }

    val getReason: nl.spindle.phonelib.model.Reason
        get() {
            return when (linphoneCall.reason) {
                Reason.None -> nl.spindle.phonelib.model.Reason.NONE
                Reason.NoResponse -> nl.spindle.phonelib.model.Reason.NO_RESPONSE
                Reason.BadCredentials -> nl.spindle.phonelib.model.Reason.BAD_CREDENTIALS
                Reason.Declined -> nl.spindle.phonelib.model.Reason.DECLINED
                Reason.NotFound -> nl.spindle.phonelib.model.Reason.NOT_FOUND
                Reason.NotAnswered -> nl.spindle.phonelib.model.Reason.NOT_ANSWERED
                Reason.Busy -> nl.spindle.phonelib.model.Reason.BUSY
                Reason.Media -> nl.spindle.phonelib.model.Reason.MEDIA
                Reason.IOError -> nl.spindle.phonelib.model.Reason.IO_ERROR
                Reason.DoNotDisturb -> nl.spindle.phonelib.model.Reason.DO_NOT_DISTURB
                Reason.Unauthorized -> nl.spindle.phonelib.model.Reason.UNAUTHORISED
                Reason.NotAcceptable -> nl.spindle.phonelib.model.Reason.NOT_ACCEPTABLE
                Reason.NoMatch -> nl.spindle.phonelib.model.Reason.NO_MATCH
                Reason.MovedPermanently -> nl.spindle.phonelib.model.Reason.MOVED_PERMANENTLY
                Reason.Gone -> nl.spindle.phonelib.model.Reason.GONE
                Reason.TemporarilyUnavailable -> nl.spindle.phonelib.model.Reason.TEMPORARILY_UNAVAILABLE
                Reason.AddressIncomplete -> nl.spindle.phonelib.model.Reason.ADDRESS_INCOMPLETE
                Reason.NotImplemented -> nl.spindle.phonelib.model.Reason.NOT_IMPLEMENTED
                Reason.BadGateway -> nl.spindle.phonelib.model.Reason.BAD_GATEWAY
                Reason.ServerTimeout -> nl.spindle.phonelib.model.Reason.SERVER_TIMEOUT
                Reason.Unknown -> nl.spindle.phonelib.model.Reason.UNKNOWN
                else -> nl.spindle.phonelib.model.Reason.UNKNOWN
            }
        }
}