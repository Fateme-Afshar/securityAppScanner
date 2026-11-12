package com.amnpardaz.securityappscanner

import android.app.Application
import com.amnpardaz.securityappscanner.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class SecurityAppScannerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@SecurityAppScannerApplication)
            modules(appModule)
        }
    }
}
