package org.openvoipalliance.phonelib.di

import org.koin.dsl.module
import org.openvoipalliance.phonelib.repository.LinphoneCoreInstanceManager
import org.openvoipalliance.phonelib.repository.call.controls.LinphoneSipActiveCallControlsRepository
import org.openvoipalliance.phonelib.repository.call.controls.SipActiveCallControlsRepository
import org.openvoipalliance.phonelib.repository.call.session.LinphoneSipSessionRepository
import org.openvoipalliance.phonelib.repository.call.session.SipSessionRepository
import org.openvoipalliance.phonelib.repository.initialise.LinphoneSipInitialiseRepository
import org.openvoipalliance.phonelib.repository.initialise.SipInitialiseRepository
import org.openvoipalliance.phonelib.repository.registration.LinphoneSipRegisterRepository
import org.openvoipalliance.phonelib.repository.registration.SipRegisterRepository

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