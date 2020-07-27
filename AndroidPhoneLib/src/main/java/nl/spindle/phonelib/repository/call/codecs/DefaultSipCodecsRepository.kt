package nl.spindle.phonelib.repository.call.codecs

import android.content.Context
import nl.spindle.phonelib.model.Codec

const val SHARED_PREFERENCES = "PhoneLibPreferences"
const val CODECS = "Codecs"

class DefaultSipCodecsRepository : SipCodecsRepository {

    override fun setAudioCodecs(context: Context, codecs: Set<Codec>) {
        val codecsString = HashSet<String>()
        codecs.forEach { codecsString.add(it.name) }
        context.getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE).edit().putStringSet(CODECS, codecsString).apply()
    }

    override fun getAudioCodecs(context: Context): Set<Codec> {
        val codecs = HashSet<Codec>()
        val codecsString = context.getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE).getStringSet(CODECS, null)
        return if (codecsString == null) {
            setOf(Codec.G722, Codec.G729, Codec.GSM, Codec.ILBC, Codec.ISAC, Codec.L16, Codec.OPUS,
                    Codec.PCMA, Codec.PCMU, Codec.SPEEX)
        } else {
            codecsString.forEach { codecs.add(Codec.valueOf(it)) }
            codecs
        }
    }

    override fun resetAudioCodecs(context: Context) {
        context.getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE).edit().putStringSet(CODECS, null).apply()
    }
}