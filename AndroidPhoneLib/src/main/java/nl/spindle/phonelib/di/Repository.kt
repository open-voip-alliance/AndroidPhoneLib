package nl.spindle.phonelib.di

import nl.spindle.phonelib.presentation.call.video.LinphoneSipVideoPresenter
import nl.spindle.phonelib.repository.LinphoneCoreInstanceManager
import nl.spindle.phonelib.repository.call.codecs.DefaultSipCodecsRepository
import nl.spindle.phonelib.repository.call.codecs.SipCodecsRepository
import nl.spindle.phonelib.repository.call.controls.LinphoneSipActiveCallControlsRepository
import nl.spindle.phonelib.repository.call.controls.SipActiveCallControlsRepository
import nl.spindle.phonelib.repository.call.session.LinphoneSipSessionRepository
import nl.spindle.phonelib.repository.call.session.SipSessionRepository
import nl.spindle.phonelib.repository.call.video.LinphoneSipVideoCallRepository
import nl.spindle.phonelib.repository.call.video.SipVideoCallRepository
import nl.spindle.phonelib.repository.initialise.LinphoneSipInitialiseRepository
import nl.spindle.phonelib.repository.initialise.SipInitialiseRepository
import nl.spindle.phonelib.repository.registration.LinphoneSipRegisterRepository
import nl.spindle.phonelib.repository.registration.SipRegisterRepository
import org.koin.dsl.module

fun getModules() = listOf(
        linphoneModule,
        presentationModule,
        repositoryModule
)

val presentationModule = module {
    single { LinphoneSipVideoPresenter(get()) }
}

@Suppress("USELESS_CAST")
val repositoryModule = module {
    single { LinphoneSipInitialiseRepository(get()) as SipInitialiseRepository }
    single { LinphoneSipRegisterRepository(get()) as SipRegisterRepository }

    single { DefaultSipCodecsRepository() as SipCodecsRepository }

    single { LinphoneSipActiveCallControlsRepository(get()) as SipActiveCallControlsRepository  }
    single { LinphoneSipSessionRepository(get()) as SipSessionRepository }
    single { LinphoneSipVideoCallRepository(get()) as SipVideoCallRepository }
}

val linphoneModule = module {
    single { LinphoneCoreInstanceManager(get()) }
}