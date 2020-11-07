package org.openvoipalliance.phonelib.repository.call.controls

import org.openvoipalliance.phonelib.model.AttendedTransferSession
import org.openvoipalliance.phonelib.model.Call

interface SipActiveCallControlsRepository {
    fun setMicrophone(on: Boolean)

    fun setHold(call: Call, on: Boolean)

    fun isMicrophoneMuted(): Boolean

    fun transferUnattended(call: Call, to: String)

    fun finishAttendedTransfer(attendedTransferSession: AttendedTransferSession)

    fun switchCall(from: Call, to: Call)

    fun pauseCall(call: Call)

    fun resumeCall(call: Call)

    fun sendDtmf(call: Call, dtmf: String)
}