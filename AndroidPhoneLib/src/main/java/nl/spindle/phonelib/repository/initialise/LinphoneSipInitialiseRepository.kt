package nl.spindle.phonelib.repository.initialise

import android.content.Context
import android.content.Intent
import nl.spindle.phonelib.repository.LinphoneCoreInstanceManager
import nl.spindle.phonelib.service.LinphoneService

internal class LinphoneSipInitialiseRepository(private val linphoneCoreInstanceManager: LinphoneCoreInstanceManager, private val context: Context) : SipInitialiseRepository {

    override fun initialise() {
        if (!LinphoneService.isInitialised) {
            val intent = Intent(Intent.ACTION_MAIN)
            intent.setClass(context, LinphoneService::class.java)
            context.startService(intent)
        }
    }

    override fun refreshRegisters(): Boolean {
        linphoneCoreInstanceManager.safeLinphoneCore?.let {
            it.refreshRegisters()
            return true
        }
        return false
    }

    override fun setSessionCallback(sessionCallback: SessionCallback?) {
        LinphoneService.setPhoneCallback(sessionCallback)
    }
}