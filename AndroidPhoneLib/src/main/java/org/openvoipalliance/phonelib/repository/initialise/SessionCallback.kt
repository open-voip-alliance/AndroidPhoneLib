package org.openvoipalliance.phonelib.repository.initialise

import org.openvoipalliance.phonelib.model.Session

abstract class SessionCallback {
    open fun incomingCall(incomingSession: Session) {}
    open fun outgoingInit(session: Session) {}
    open fun sessionConnected(session: Session) {}
    open fun sessionEnded(session: Session) {}
    open fun sessionReleased(session: Session) {}
    open fun sessionUpdated(session: Session) {}
    open fun error(session: Session) {}
}