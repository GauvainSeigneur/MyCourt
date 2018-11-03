package seigneur.gauvain.mycourt

import android.app.Activity
import android.app.Application

import com.squareup.leakcanary.LeakCanary

import javax.inject.Inject

import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import seigneur.gauvain.mycourt.di.DaggerAppComponent
import seigneur.gauvain.mycourt.di.modules.NetworkModule
import seigneur.gauvain.mycourt.di.AppComponent
import seigneur.gauvain.mycourt.di.modules.RoomModule
import seigneur.gauvain.mycourt.di.modules.SharedPrefsModule
import seigneur.gauvain.mycourt.di.modules.ConnectivityModule
import seigneur.gauvain.mycourt.di.modules.NetworkErrorInteractorModule
import seigneur.gauvain.mycourt.utils.timber.TimberLog

/**
 * Created by gse on 26/03/2018.
 */
class MyCourtApp : Application(), HasActivityInjector {

    @Inject
    lateinit var activityInjector: DispatchingAndroidInjector<Activity>

    private var applicationComponent: AppComponent? = null

    override fun onCreate() {
        super.onCreate()
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return
        }
        LeakCanary.install(this)
        applicationComponent = createComponent()
        inject() //get Application context
        TimberLog.init() //Init timberLog
    }

    private fun createComponent(): AppComponent {
        return DaggerAppComponent.builder()
                .application(this)
                .network(NetworkModule())
                .sharedPrefs(SharedPrefsModule(this))
                .connectivityWatcher(ConnectivityModule(this))
                .dataBase(RoomModule())
                .rxErrorHandler(NetworkErrorInteractorModule())
                .build()
    }

    private fun inject() {
        applicationComponent!!.inject(this)
    }

    override fun activityInjector(): AndroidInjector<Activity>? {
        return activityInjector
    }

}