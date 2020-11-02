package org.openvoipalliance.phonelib.repository.initialise

import android.content.Context
import android.content.Intent
import org.linphone.core.Factory
import org.openvoipalliance.phonelib.model.Codec
import org.openvoipalliance.phonelib.repository.LinphoneCoreInstanceManager

internal class LinphoneSipInitialiseRepository(private val linphoneCoreInstanceManager: LinphoneCoreInstanceManager, private val context: Context) : SipInitialiseRepository {

    private var codecs = setOf(Codec.G722, Codec.G729, Codec.GSM, Codec.ILBC, Codec.ISAC, Codec.L16, Codec.OPUS,
            Codec.PCMA, Codec.PCMU, Codec.SPEEX)

    private var logListener: LogListener? = null

    override fun initialise() {
        Factory.instance()
        linphoneCoreInstanceManager.initialiseLinphone(context, getAudioCodecs(), getLogListener())
    }

    override fun destroy() {
        LinphoneCoreInstanceManager.phoneCallback = null
        linphoneCoreInstanceManager.destroy()
    }

    override fun refreshRegisters(): Boolean {
        linphoneCoreInstanceManager.safeLinphoneCore?.let {
            it.refreshRegisters()
            return true
        }
        return false
    }

    override fun setSessionCallback(sessionCallback: SessionCallback?) {
        LinphoneCoreInstanceManager.phoneCallback = sessionCallback
    }

    override fun setUserAgent(userAgent: String) {
        linphoneCoreInstanceManager.setUserAgent(userAgent)
    }

    override fun setAudioCodecs(codecs: Set<Codec>) {
        this.codecs = codecs
        linphoneCoreInstanceManager.setCodecMime(codecs)
    }

    override fun getAudioCodecs() = codecs

    override fun getLogListener() = logListener

    override fun setLogListener(listener: LogListener) {
        this.logListener = listener
    }
}