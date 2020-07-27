package nl.spindle.phonelib.repository.registration

import nl.spindle.phonelib.model.RegistrationState

abstract class RegistrationCallback {
    open fun stateChanged(registrationState: RegistrationState) {}
}