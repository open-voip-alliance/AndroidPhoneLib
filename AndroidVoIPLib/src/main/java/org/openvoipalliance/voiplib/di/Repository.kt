package org.openvoipalliance.voiplib.di

import org.koin.dsl.module
import org.openvoipalliance.voiplib.repository.LinphoneCoreInstanceManager
import org.openvoipalliance.voiplib.repository.call.controls.LinphoneSipActiveCallControlsRepository
import org.openvoipalliance.voiplib.repository.call.controls.SipActiveCallControlsRepository
import org.openvoipalliance.voiplib.repository.call.session.LinphoneSipSessionRepository
import org.openvoipalliance.voiplib.repository.call.session.SipSessionRepository
import org.openvoipalliance.voiplib.repository.initialise.LinphoneSipInitialiseRepository
import org.openvoipalliance.voiplib.repository.initialise.SipInitialiseRepository
import org.openvoipalliance.voiplib.repository.registration.LinphoneSipRegisterRepository
import org.openvoipalliance.voiplib.repository.registration.SipRegisterRepository

fun getModules() = listOf(
        linphoneModule,
        presentationModule,
        repositoryModule
)

val presentationModule = module {
}

@Suppress("USELESS_CAST")
val repositoryModule = module {
    single { LinphoneSipInitialiseRepository(get(), get()) as SipInitialiseRepository }
    single { LinphoneSipRegisterRepository(get()) as SipRegisterRepository }

    single { LinphoneSipActiveCallControlsRepository(get()) as SipActiveCallControlsRepository  }
    single { LinphoneSipSessionRepository(get()) as SipSessionRepository }
}

val linphoneModule = module {
    single { LinphoneCoreInstanceManager(get()) }
}