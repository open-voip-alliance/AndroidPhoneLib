package org.openvoipalliance.phonelib.repository.registration

import android.R.attr
import org.linphone.core.*
import org.openvoipalliance.phonelib.repository.LinphoneCoreInstanceManager

internal class LinphoneSipRegisterRepository(private val linphoneCoreInstanceManager: LinphoneCoreInstanceManager) : SipRegisterRepository {

    @Throws(CoreException::class)
    override fun registerUser(name: String, password: String, domain: String, port: String, stunServer: String?, encrypted: Boolean, registrationCallback: RegistrationCallback) {
        LinphoneCoreInstanceManager.registrationCallback = registrationCallback
        LinphoneCoreInstanceManager.serverIP = "$domain:$port"

        val core = linphoneCoreInstanceManager.safeLinphoneCore ?: return

        core.transports = core.transports.apply {
            udpPort = if (encrypted) attr.port else RANDOM_PORT
            tcpPort = if (encrypted) attr.port else RANDOM_PORT
            tlsPort = RANDOM_PORT
        }

        core.mediaEncryption = if (encrypted) MediaEncryption.SRTP else MediaEncryption.None
        core.isMediaEncryptionMandatory = encrypted

        stunServer?.let {
            core.stunServer = it
        }

        val authInfo = Factory.instance().createAuthInfo(name, name, password,
                null, null, "$domain:$port").apply {
            algorithm = null
        }

        core.apply {
            addProxyConfig(createProxyConfig(core, name, domain, port))
            addAuthInfo(authInfo)
            defaultProxyConfig = core.proxyConfigList.first()
            useRfc2833ForDtmf = true
            enableIpv6(false)
        }
    }

    private fun createProxyConfig(core: Core, name: String, domain: String, port: String): ProxyConfig {
        val identify = "sip:$name@$domain:$port"
        val proxy = "sip:$domain:$port"
        val identifyAddress = Factory.instance().createAddress(identify)

        return core.createProxyConfig().apply {
            enableRegister(true)
            enableQualityReporting(false)
            qualityReportingCollector = null
            qualityReportingInterval = 0
            identityAddress = identifyAddress
            avpfRrInterval = 0
            avpfMode = AVPFMode.Disabled
            serverAddr = proxy
            done()
        }
    }

    override fun unregister() {
        val core = linphoneCoreInstanceManager.safeLinphoneCore ?: return

        core.proxyConfigList.forEach {
            it.edit()
            it.enableRegister(false)
            it.done()
            core.removeProxyConfig(it)
        }

        core.authInfoList.forEach {
            core.removeAuthInfo(it)
        }
    }

    companion object {
        const val RANDOM_PORT = -1
    }
}