package org.openvoipalliance.phonelib.repository.registration

import org.linphone.core.CoreException
import org.openvoipalliance.phonelib.model.RegistrationState

typealias RegistrationCallback = (RegistrationState) -> Unit

internal interface SipRegisterRepository {
    @Throws(CoreException::class)
    fun register(callback: RegistrationCallback)

    fun unregister()
    fun isRegistered(): Boolean
}