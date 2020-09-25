package nl.spindle.phonelib.repository.call.codecs

import android.content.Context
import nl.spindle.phonelib.model.Codec

interface SipConfigurationsRepository {
    fun setAudioCodecs(codecs: Set<Codec>)
    fun getAudioCodecs(): Set<Codec>
    fun resetAudioCodecs()
    fun setUserAgent(userAgent: String)
    fun getUserAgent(): String?
    fun resetUserAgent()
}