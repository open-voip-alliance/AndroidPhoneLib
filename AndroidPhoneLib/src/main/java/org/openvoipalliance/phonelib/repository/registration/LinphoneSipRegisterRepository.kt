package org.openvoipalliance.phonelib.repository.registration

import android.R.attr
import android.util.Log
import org.openvoipalliance.phonelib.repository.LinphoneCoreInstanceManager
import org.linphone.core.*

private const val RANDOM_PORT =  -1

internal class LinphoneSipRegisterRepository(private val linphoneCoreInstanceManager: LinphoneCoreInstanceManager) : SipRegisterRepository {

    @Throws(CoreException::class)
    override fun registerUser(name: String, password: String, domain: String, port: String, stunServer: String?, encrypted: Boolean, registrationCallback: RegistrationCallback) {
        LinphoneCoreInstanceManager.registrationCallback = registrationCallback
        LinphoneCoreInstanceManager.serverIP = "$domain:$port"
        try {
            val identify = "sip:$name@$domain:$port"
            val proxy = "sip:$domain:$port"
            val identifyAddress = Factory.instance().createAddress(identify)
            val authInfo = Factory.instance().createAuthInfo(name, name, password,
                    null, null, "$domain:$port")
            authInfo.algorithm = null

            if (encrypted) {
                val transports: Transports = linphoneCoreInstanceManager.safeLinphoneCore?.transports!!
                transports.udpPort = 0
                transports.tcpPort = 0
                transports.tlsPort = RANDOM_PORT
                linphoneCoreInstanceManager.safeLinphoneCore?.transports = transports
                linphoneCoreInstanceManager.safeLinphoneCore?.mediaEncryption = MediaEncryption.SRTP
                linphoneCoreInstanceManager.safeLinphoneCore?.isMediaEncryptionMandatory = true
            }

            val prxCfg = linphoneCoreInstanceManager.safeLinphoneCore?.createProxyConfig()

            prxCfg?.enableRegister(true)

            prxCfg?.enableQualityReporting(false)
            prxCfg?.qualityReportingCollector = null
            prxCfg?.qualityReportingInterval = 0
            prxCfg?.identityAddress = identifyAddress

            prxCfg?.avpfRrInterval = 0
            prxCfg?.avpfMode = AVPFMode.Disabled

            prxCfg?.serverAddr = proxy
            prxCfg?.done()

            stunServer?.let {
                linphoneCoreInstanceManager.safeLinphoneCore?.stunServer = it
            }

            linphoneCoreInstanceManager.safeLinphoneCore?.apply {
                addProxyConfig(prxCfg)
                addAuthInfo(authInfo)
                defaultProxyConfig = prxCfg
                useRfc2833ForDtmf = true
                enableIpv6(false)
            }

        } catch (e: CoreException) {
            e.printStackTrace()
        }
    }

    override fun unregister() {
        val proxyConfigs = linphoneCoreInstanceManager.safeLinphoneCore?.proxyConfigList
        proxyConfigs?.forEach {
            it.edit()
            it.enableRegister(false)
            it.done()
            linphoneCoreInstanceManager.safeLinphoneCore?.removeProxyConfig(it)
        }
        linphoneCoreInstanceManager.safeLinphoneCore?.authInfoList?.forEach {
            linphoneCoreInstanceManager.safeLinphoneCore?.removeAuthInfo(it)
        }
    }
}