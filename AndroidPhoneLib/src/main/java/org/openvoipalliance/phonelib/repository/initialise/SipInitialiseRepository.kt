package org.openvoipalliance.phonelib.repository.initialise

import org.openvoipalliance.phonelib.model.Codec

internal interface SipInitialiseRepository {
     fun initialise()
     fun refreshRegisters(): Boolean
     fun setSessionCallback(sessionCallback: SessionCallback?)
     fun setUserAgent(userAgent: String)
     fun setAudioCodecs(codecs: Set<Codec>)
     fun getAudioCodecs(): Set<Codec>
}