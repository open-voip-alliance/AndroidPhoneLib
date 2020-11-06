package org.openvoipalliance.phonelib.repository.initialise

import org.openvoipalliance.phonelib.config.Config
import org.openvoipalliance.phonelib.model.Codec

internal interface SipInitialiseRepository {
     fun initialise(config: Config)
     fun destroy()
     fun refreshRegisters(): Boolean
     fun setSessionCallback(sessionCallback: SessionCallback?)
}