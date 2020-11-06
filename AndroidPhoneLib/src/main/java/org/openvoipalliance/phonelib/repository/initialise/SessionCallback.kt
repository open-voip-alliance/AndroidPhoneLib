package org.openvoipalliance.phonelib.repository.initialise

import org.openvoipalliance.phonelib.model.Call

abstract class SessionCallback {
    open fun incomingCall(incomingCall: Call) {}
    open fun outgoingInit(call: Call) {}
    open fun sessionConnected(call: Call) {}
    open fun sessionEnded(call: Call) {}
    open fun sessionReleased(call: Call) {}
    open fun sessionUpdated(call: Call) {}
    open fun error(call: Call) {}
}