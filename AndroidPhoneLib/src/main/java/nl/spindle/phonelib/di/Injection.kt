package nl.spindle.phonelib.di

import android.content.Context
import org.koin.android.ext.koin.androidContext
import org.koin.core.KoinComponent
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin

internal class Injection(private val context: Context) : KoinComponent {
    init {
        try {
            startKoin {
                androidContext(context)
                modules(getModules())
            }
        } catch (e: IllegalStateException) {
            // A KoinContext had already started, load the modules into the existing one.
            loadKoinModules(getModules())
        }
    }
}