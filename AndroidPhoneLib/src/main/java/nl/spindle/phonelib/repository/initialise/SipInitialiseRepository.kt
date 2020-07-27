package nl.spindle.phonelib.repository.initialise

import android.content.Context

internal interface SipInitialiseRepository {
     fun initialise(context: Context)
     fun refreshRegisters(): Boolean
     fun setSessionCallback(sessionCallback: SessionCallback?)
}