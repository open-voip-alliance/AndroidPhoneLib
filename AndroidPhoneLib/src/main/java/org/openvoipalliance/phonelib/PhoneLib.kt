package org.openvoipalliance.phonelib

import android.Manifest.permission.RECORD_AUDIO
import android.content.Context
import androidx.annotation.RequiresPermission
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.openvoipalliance.phonelib.config.Config
import org.openvoipalliance.phonelib.di.Injection
import org.openvoipalliance.phonelib.model.Call
import org.openvoipalliance.phonelib.repository.call.controls.SipActiveCallControlsRepository
import org.openvoipalliance.phonelib.repository.call.session.SipSessionRepository
import org.openvoipalliance.phonelib.repository.initialise.SipInitialiseRepository
import org.openvoipalliance.phonelib.repository.registration.RegistrationCallback
import org.openvoipalliance.phonelib.repository.registration.SipRegisterRepository

class PhoneLib private constructor(private val context: Context) {
    private val sipInitialiseRepository: SipInitialiseRepository by injection.inject()
    private val sipRegisterRepository: SipRegisterRepository by injection.inject()

    private val sipCallControlsRepository: SipActiveCallControlsRepository by injection.inject()
    private val sipSessionRepository: SipSessionRepository by injection.inject()


    /**
     * This needs to be called whenever this library needs to initialise. Without it, no other calls
     * can be done.
     */
    fun initialise(config: Config): PhoneLib {
        sipInitialiseRepository.initialise(config)
        return this
    }

    /**
     * Check to see if the phonelib is initialised and ready to make calls.
     *
     */
    val isInitialised: Boolean
        get() = sipInitialiseRepository.isInitialised()

    val isRegistered: Boolean
        get() = sipRegisterRepository.isRegistered()

    val isReady: Boolean
        get() = isInitialised && isRegistered

    /**
     * This registers your user on SIP. You need this before placing a call.
     *
     */
    fun register(callback: RegistrationCallback): PhoneLib {
        sipRegisterRepository.register(callback)
        return this
    }

    /**
     * Refreshes the configuration by destroying and then re-initialising the library.
     *
     */
    fun refreshConfig(config: Config) {
        destroy()
        initialise(config)
    }

    /**
     * Get the currently used config.
     *
     */
    val currentConfig by lazy { sipInitialiseRepository.currentConfig() }

    /**
     * This performs a direct swap of the current config without any restarts, not all
     * changes may take affect.
     *
     * It is recommended you use the copy function after getting the currentConfig.
     */
    fun swapConfig(config: Config) = sipInitialiseRepository.swapConfig(config)

    /**
     * Destroy this library completely.
     *
     */
    fun destroy() {
        unregister()
        sipInitialiseRepository.destroy()
    }

    /**
     * This unregisters your user on SIP.
     */
    fun unregister() = sipRegisterRepository.unregister()

    /**
     * This method audio calls a phone number
     * @param number the number dialed to
     * @return returns true when call succeeds, false when the number is an empty string or the
     * phone service isn't ready.
     */
    @RequiresPermission(RECORD_AUDIO)
    fun callTo(number: String) = sipSessionRepository.callTo(number)

    /**
     * Whether or not the microphone is currently muted.
     *
     * Set to TRUE to mute the microphone and prevent voice transmission.
     */
    var microphoneMuted
            get() = sipCallControlsRepository.isMicrophoneMuted()
            set(muted) = sipCallControlsRepository.setMicrophone(!muted)

    /**
     * Perform actions on the given call.
     *
     */
    fun actions(call: Call) = Actions(context, call)

    companion object {
        private var instance: PhoneLib? = null
        internal lateinit var injection: Injection

        @JvmStatic
        fun getInstance(context: Context): PhoneLib {
            if (instance != null) return instance as PhoneLib

            injection = Injection(context)
            val phoneLib = PhoneLib(context.applicationContext)
            instance = phoneLib
            return phoneLib
        }
    }
}