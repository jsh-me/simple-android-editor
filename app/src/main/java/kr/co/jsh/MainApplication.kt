package kr.co.jsh

import android.app.Application
import com.facebook.stetho.Stetho
import kr.co.domain.koin.modules.databaseModule
import kr.co.domain.koin.modules.networkModule
import kr.co.domain.koin.modules.useCaseModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import timber.log.Timber

class MainApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        Stetho.newInitializerBuilder(this)
        Timber.plant(Timber.DebugTree())

        startKoin{
            androidLogger()
            androidContext(this@MainApplication)
            modules(mutableListOf(networkModule, useCaseModule, databaseModule))
        }
    }

}