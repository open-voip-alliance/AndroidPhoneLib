package org.openvoipalliance.phonelib.di

import android.content.Context
import org.koin.android.ext.koin.androidContext
import org.koin.core.component.KoinComponent
import org.koin.core.context.GlobalContext
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import org.koin.core.error.KoinAppAlreadyStartedException

internal class Injection(private val context: Context) : KoinComponent {
    init {
        GlobalContext.getOrNull() ?: startKoin {
            androidContext(context)
            modules(getModules())
        }
    }
}