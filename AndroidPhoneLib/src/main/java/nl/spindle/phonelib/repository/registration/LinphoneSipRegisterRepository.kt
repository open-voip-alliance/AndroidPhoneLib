package nl.spindle.phonelib.repository.registration

import nl.spindle.phonelib.repository.LinphoneCoreInstanceManager
import nl.spindle.phonelib.service.LinphoneService
import nl.spindle.phonelib.service.LinphoneService.Companion.ensureReady
import nl.spindle.phonelib.service.LinphoneService.Companion.setRegistrationCallback
import org.linphone.core.LinphoneCoreException
import org.linphone.core.LinphoneCoreFactory

class LinphoneSipRegisterRepository(private val linphoneCoreInstanceManager: LinphoneCoreInstanceManager) : SipRegisterRepository {

    @Throws(LinphoneCoreException::class)
    override fun registerUser(name: String, password: String, domain: String, port: String, registrationCallback: RegistrationCallback) {
        ensureReady {
            setRegistrationCallback(registrationCallback)
            LinphoneService.sServerIP = "$domain:$port"
            try {
                val identify = "sip:$name@$domain:$port"
                val proxy = "sip:$domain:$port"
                val proxyAddr = LinphoneCoreFactory.instance().createLinphoneAddress(proxy)
                val identifyAddr = LinphoneCoreFactory.instance().createLinphoneAddress(identify)
                val authInfo = LinphoneCoreFactory.instance().createAuthInfo(name, null, password,
                        null, null, "$domain:$port")
                val prxCfg = linphoneCoreInstanceManager.safeLinphoneCore?.createProxyConfig(identifyAddr.asString(),
                        proxyAddr.asStringUriOnly(), proxyAddr.asStringUriOnly(), true)
                prxCfg?.enableAvpf(false)
                prxCfg?.avpfRRInterval = 0
                prxCfg?.enableQualityReporting(false)
                prxCfg?.qualityReportingCollector = null
                prxCfg?.qualityReportingInterval = 0
                prxCfg?.enableRegister(true)
                linphoneCoreInstanceManager.safeLinphoneCore?.addProxyConfig(prxCfg)
                linphoneCoreInstanceManager.safeLinphoneCore?.addAuthInfo(authInfo)
                linphoneCoreInstanceManager.safeLinphoneCore?.defaultProxyConfig = prxCfg
            } catch (e: LinphoneCoreException) {
                e.printStackTrace()
            }
        }
    }
}