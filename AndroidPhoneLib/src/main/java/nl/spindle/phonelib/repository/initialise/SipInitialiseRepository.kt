package nl.spindle.phonelib.repository.initialise

import nl.spindle.phonelib.model.Codec

internal interface SipInitialiseRepository {
     fun initialise()
     fun refreshRegisters(): Boolean
     fun setSessionCallback(sessionCallback: SessionCallback?)
     fun setUserAgent(userAgent: String)
     fun setAudioCodecs(codecs: Set<Codec>)
     fun getAudioCodecs(): Set<Codec>
}