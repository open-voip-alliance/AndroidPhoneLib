package nl.spindle.phonelib.repository

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import nl.spindle.phonelib.di.Injection
import nl.spindle.phonelib.repository.initialise.SipInitialiseRepository
import org.koin.core.inject

class KeepAliveHandler : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val injection = Injection(context)
        val sipInitialiseRepository: SipInitialiseRepository by injection.inject()
        sipInitialiseRepository.refreshRegisters()
    }
}