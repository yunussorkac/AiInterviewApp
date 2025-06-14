package com.yeslab.interviewapp

import android.app.Application
import com.yeslab.interviewapp.di.Modules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MyApp)
            modules(
                Modules.viewModelModule,
                Modules.repositoryModule,
                Modules.networkModule,
                Modules.roomModule
            )
        }
    }

}