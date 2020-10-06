package org.openvoipalliance.phonelib.repository.registration

import org.linphone.core.CoreException

internal interface SipRegisterRepository {
    @Throws(CoreException::class)
    fun registerUser(name: String, password: String, domain: String, port: String, stunServer: String?, encrypted: Boolean, registrationCallback: RegistrationCallback)

    fun unregister()
}