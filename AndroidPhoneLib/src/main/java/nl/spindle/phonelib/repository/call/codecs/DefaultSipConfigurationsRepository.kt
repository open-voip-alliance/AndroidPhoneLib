package nl.spindle.phonelib.repository.call.codecs

import android.content.Context
import nl.spindle.phonelib.model.Codec
import nl.spindle.phonelib.repository.LinphoneCoreInstanceManager

const val SHARED_PREFERENCES = "PhoneLibPreferences"
const val CODECS = "Codecs"
const val USER_AGENT = "UserAgent"

class DefaultSipConfigurationsRepository(private val linphoneCoreInstanceManager: LinphoneCoreInstanceManager, private val context: Context) : SipConfigurationsRepository {

    override fun setAudioCodecs(codecs: Set<Codec>) {
        val codecsString = HashSet<String>()
        codecs.forEach { codecsString.add(it.name) }
        context.getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE).edit().putStringSet(CODECS, codecsString).apply()
        linphoneCoreInstanceManager.setCodecMime(codecs)
    }

    override fun getAudioCodecs(): Set<Codec> {
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

    override fun resetAudioCodecs() {
        context.getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE).edit().putStringSet(CODECS, null).apply()
    }

    override fun setUserAgent(userAgent: String) {
        context.getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE).edit().putString(USER_AGENT, userAgent).apply()
        linphoneCoreInstanceManager.setUserAgent(userAgent)
    }

    override fun resetUserAgent() {
        context.getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE).edit().remove(USER_AGENT).apply()
    }

    override fun getUserAgent(): String? = context.getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE).getString(USER_AGENT, null)
}