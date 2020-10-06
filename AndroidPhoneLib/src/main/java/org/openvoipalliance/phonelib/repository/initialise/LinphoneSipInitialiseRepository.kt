package org.openvoipalliance.phonelib.repository.initialise

import android.content.Context
import android.content.Intent
import org.openvoipalliance.phonelib.model.Codec
import org.openvoipalliance.phonelib.repository.LinphoneCoreInstanceManager
import org.openvoipalliance.phonelib.service.LinphoneService

internal class LinphoneSipInitialiseRepository(private val linphoneCoreInstanceManager: LinphoneCoreInstanceManager, private val context: Context) : SipInitialiseRepository {

    private var codecs = setOf(Codec.G722, Codec.G729, Codec.GSM, Codec.ILBC, Codec.ISAC, Codec.L16, Codec.OPUS,
            Codec.PCMA, Codec.PCMU, Codec.SPEEX)

    override fun initialise() {
        if (!LinphoneService.isInitialised) {
            val intent = Intent(Intent.ACTION_MAIN)
            intent.setClass(context, LinphoneService::class.java)
            context.startService(intent)
        }
    }

    override fun refreshRegisters(): Boolean {
        linphoneCoreInstanceManager.safeLinphoneCore?.let {
            it.refreshRegisters()
            return true
        }
        return false
    }

    override fun setSessionCallback(sessionCallback: SessionCallback?) {
        LinphoneService.setPhoneCallback(sessionCallback)
    }

    override fun setUserAgent(userAgent: String) {
        linphoneCoreInstanceManager.setUserAgent(userAgent)
    }

    override fun setAudioCodecs(codecs: Set<Codec>) {
        this.codecs = codecs
        linphoneCoreInstanceManager.setCodecMime(codecs)
    }

    override fun getAudioCodecs() = codecs
}