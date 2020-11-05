package org.openvoipalliance.phonelib.repository

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.openvoipalliance.phonelib.di.Injection
import org.openvoipalliance.phonelib.repository.initialise.SipInitialiseRepository
import org.koin.core.component.inject

class KeepAliveHandler : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val injection = Injection(context)
        val sipInitialiseRepository: SipInitialiseRepository by injection.inject()
        sipInitialiseRepository.refreshRegisters()
    }
}