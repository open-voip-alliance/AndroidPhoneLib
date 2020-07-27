package nl.spindle.phonelib.repository.call.codecs

import android.content.Context
import nl.spindle.phonelib.model.Codec

interface SipCodecsRepository {
    fun setAudioCodecs(context: Context, codecs: Set<Codec>)
    fun getAudioCodecs(context: Context): Set<Codec>
    fun resetAudioCodecs(context: Context)
}