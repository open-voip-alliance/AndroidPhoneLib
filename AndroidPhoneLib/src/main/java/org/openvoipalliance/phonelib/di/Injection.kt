package org.openvoipalliance.phonelib.di

import android.content.Context
import org.koin.android.ext.koin.androidContext
import org.koin.core.component.KoinComponent
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import org.koin.core.error.KoinAppAlreadyStartedException

internal class Injection(private val context: Context) : KoinComponent {
    init {
        try {
            startKoin {
                androidContext(context)
                modules(getModules())
            }
        } catch (e: KoinAppAlreadyStartedException) {
            // A KoinContext had already started, load the modules into the existing one.
            loadKoinModules(getModules())
        }
    }
}