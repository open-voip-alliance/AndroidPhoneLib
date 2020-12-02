package org.openvoipalliance.phonelib.repository.initialise

import android.content.Context
import org.linphone.core.Factory
import org.openvoipalliance.phonelib.repository.LinphoneCoreInstanceManager
import org.openvoipalliance.phonelib.config.Config as PhoneLibConfig

internal class LinphoneSipInitialiseRepository(private val linphoneCoreInstanceManager: LinphoneCoreInstanceManager, private val context: Context) : SipInitialiseRepository {

    override fun initialise(config: PhoneLibConfig) {
        Factory.instance()
        linphoneCoreInstanceManager.initialiseLinphone(context, config)
    }

    override fun destroy() {
        linphoneCoreInstanceManager.destroy()
    }

    override fun swapConfig(config: PhoneLibConfig) {
        linphoneCoreInstanceManager.config = config
    }

    override fun refreshRegisters(): Boolean {
        linphoneCoreInstanceManager.safeLinphoneCore?.let {
            it.refreshRegisters()
            return true
        }
        return false
    }

    override fun currentConfig(): org.openvoipalliance.phonelib.config.Config = linphoneCoreInstanceManager.config

    override fun isInitialised(): Boolean = linphoneCoreInstanceManager.initialised
}