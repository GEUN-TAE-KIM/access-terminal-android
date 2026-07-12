package com.gtkim.mobile_access_control

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.gtkim.mobile_access_control.di.Bootstrapper
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class AccessApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    @Inject
    lateinit var timberTree: Timber.Tree
    @Inject
    lateinit var bootstrapper: Bootstrapper

    override fun onCreate() {
        super.onCreate()
        Timber.plant(timberTree)
        bootstrapper.start()
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
