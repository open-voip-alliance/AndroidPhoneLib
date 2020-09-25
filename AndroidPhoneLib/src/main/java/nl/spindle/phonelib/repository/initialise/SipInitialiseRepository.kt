package nl.spindle.phonelib.repository.initialise

internal interface SipInitialiseRepository {
     fun initialise()
     fun refreshRegisters(): Boolean
     fun setSessionCallback(sessionCallback: SessionCallback?)
}