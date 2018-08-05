package seigneur.gauvain.mycourt.di;

import android.app.Application;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;
import seigneur.gauvain.mycourt.MyCourtApp;
import seigneur.gauvain.mycourt.di.modules.AppModule;
import seigneur.gauvain.mycourt.di.modules.NetworkModule;
import seigneur.gauvain.mycourt.di.modules.RoomModule;
import seigneur.gauvain.mycourt.di.modules.NetworkErrorInteractorModule;
import seigneur.gauvain.mycourt.di.modules.SharedPrefsModule;
import seigneur.gauvain.mycourt.di.modules.ConnectivityModule;

/**
 * Created by gse on 26/03/2018.
 */
@Singleton
@Component(modules =  {
        AppModule.class,
        NetworkModule.class,
        SharedPrefsModule.class,
        ConnectivityModule.class,
        RoomModule.class,
        NetworkErrorInteractorModule.class
       // UserModule.class
})
public interface AppComponent {
    void inject(MyCourtApp app);

    @Component.Builder
    interface Builder {
        @BindsInstance
        Builder application(Application application);
        Builder network(NetworkModule networkModule);
        Builder sharedPrefs(SharedPrefsModule sharedPrefsModule);
        Builder connectivityWatcher(ConnectivityModule connectivityModule);
        Builder dataBase(RoomModule roomModule);
        Builder rxErrorHandler(NetworkErrorInteractorModule networkErrorInteractorModule);
       // Builder userManager(UserModule userModule);
        AppComponent build();
    }

}
