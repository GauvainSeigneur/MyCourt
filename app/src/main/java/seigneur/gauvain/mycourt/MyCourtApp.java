package seigneur.gauvain.mycourt;

import android.app.Activity;
import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;
import seigneur.gauvain.mycourt.di.modules.NetworkModule;
import seigneur.gauvain.mycourt.di.AppComponent;
import seigneur.gauvain.mycourt.di.DaggerAppComponent;
import seigneur.gauvain.mycourt.di.modules.RoomModule;
import seigneur.gauvain.mycourt.di.modules.SharedPrefsModule;
import seigneur.gauvain.mycourt.di.modules.ConnectivityModule;
import seigneur.gauvain.mycourt.di.modules.NetworkErrorInteractorModule;
import seigneur.gauvain.mycourt.utils.timber.TimberLog;

/**
 * Created by gse on 26/03/2018.
 */
public class MyCourtApp extends Application implements HasActivityInjector {

    @Inject
    DispatchingAndroidInjector<Activity> activityInjector;

    private AppComponent mAppComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);
        mAppComponent = createComponent();
        inject(); //get Application context
        TimberLog.init(); //Init timberLog
    }

    protected AppComponent createComponent() {
        return DaggerAppComponent.builder()
                    .application(this)
                    .network(new NetworkModule())
                    .sharedPrefs(new SharedPrefsModule(this))
                    .connectivityWatcher(new ConnectivityModule(this))
                    .dataBase(new RoomModule())
                    .rxErrorHandler(new NetworkErrorInteractorModule())
                    .build();
    }

    private void inject() {
        mAppComponent.inject(this);
    }

    @Override
    public AndroidInjector<Activity> activityInjector() {
        return activityInjector;
    }

    public AppComponent getApplicationComponent() {
        return mAppComponent;
    }

}