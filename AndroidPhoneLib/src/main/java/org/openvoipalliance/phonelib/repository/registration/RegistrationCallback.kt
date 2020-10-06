package org.openvoipalliance.phonelib.repository.registration

import org.openvoipalliance.phonelib.model.RegistrationState

abstract class RegistrationCallback {
    open fun stateChanged(registrationState: RegistrationState) {}
}