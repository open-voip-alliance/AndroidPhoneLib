package org.openvoipalliance.phonelib.di

import android.content.Context
import org.koin.android.ext.koin.androidContext
import org.koin.core.Koin
import org.koin.core.KoinApplication
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.context.GlobalContext
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import org.koin.core.error.KoinAppAlreadyStartedException
import org.koin.dsl.koinApplication

internal class Injection(private val context: Context) : PhoneLibKoinComponent {
    init {
        phoneLibKoin = koinApplication {
            androidContext(context)
            modules(getModules())
        }
    }

    companion object {
        var phoneLibKoin: KoinApplication? = null
    }
}

interface PhoneLibKoinComponent : KoinComponent {
    @KoinApiExtension
    override fun getKoin(): Koin {
        return Injection.phoneLibKoin?.koin!!
    }
}