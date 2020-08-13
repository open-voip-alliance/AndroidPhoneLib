package nl.spindle.phonelib.repository.registration

import org.linphone.core.LinphoneCoreException

internal interface SipRegisterRepository {
    @Throws(LinphoneCoreException::class)
    fun registerUser(name: String, password: String, domain: String, port: String, registrationCallback: RegistrationCallback)

    fun unregister()
}