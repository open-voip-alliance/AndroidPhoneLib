package org.openvoipalliance.phonelib.repository.initialise

import org.openvoipalliance.phonelib.config.Config

internal interface SipInitialiseRepository {
     fun initialise(config: Config)
     fun swapConfig(config: Config)
     fun destroy()
     fun refreshRegisters(): Boolean
     fun currentConfig(): Config
}