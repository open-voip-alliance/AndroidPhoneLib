package org.openvoipalliance.phonelib.repository.registration

import android.R.attr
import org.openvoipalliance.phonelib.repository.LinphoneCoreInstanceManager
import org.openvoipalliance.phonelib.service.LinphoneService
import org.openvoipalliance.phonelib.service.LinphoneService.Companion.ensureReady
import org.openvoipalliance.phonelib.service.LinphoneService.Companion.setRegistrationCallback
import org.linphone.core.*

private const val RANDOM_PORT =  -1

internal class LinphoneSipRegisterRepository(private val linphoneCoreInstanceManager: LinphoneCoreInstanceManager) : SipRegisterRepository {

    @Throws(CoreException::class)
    override fun registerUser(name: String, password: String, domain: String, port: String, stunServer: String?, encrypted: Boolean, registrationCallback: RegistrationCallback) {
        ensureReady {
            setRegistrationCallback(registrationCallback)
            LinphoneService.sServerIP = "$domain:$port"
            try {
                val identify = "sip:$name@$domain:$port"
                val proxy = "sip:$domain:$port"
                val identifyAddress = Factory.instance().createAddress(identify)
                val authInfo = Factory.instance().createAuthInfo(name, name, password,
                        null, null, "$domain:$port")
                authInfo.algorithm = null

                if (encrypted) {
                    val transports: Transports = linphoneCoreInstanceManager.safeLinphoneCore?.transports!!
                    transports.udpPort = attr.port
                    transports.tcpPort = attr.port
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

                linphoneCoreInstanceManager.safeLinphoneCore?.addProxyConfig(prxCfg)
                linphoneCoreInstanceManager.safeLinphoneCore?.addAuthInfo(authInfo)
                linphoneCoreInstanceManager.safeLinphoneCore?.defaultProxyConfig = prxCfg

                linphoneCoreInstanceManager.safeLinphoneCore?.ensureRegistered()
                linphoneCoreInstanceManager.safeLinphoneCore?.refreshRegisters()

                linphoneCoreInstanceManager.safeLinphoneCore?.useRfc2833ForDtmf = true
                linphoneCoreInstanceManager.safeLinphoneCore?.enableIpv6(true)

            } catch (e: CoreException) {
                e.printStackTrace()
            }
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