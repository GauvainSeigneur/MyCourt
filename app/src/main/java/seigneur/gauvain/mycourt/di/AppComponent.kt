package seigneur.gauvain.mycourt.di

import android.app.Application

import javax.inject.Singleton

import dagger.BindsInstance
import dagger.Component
import seigneur.gauvain.mycourt.MyCourtApp
import seigneur.gauvain.mycourt.di.modules.AppModule
import seigneur.gauvain.mycourt.di.modules.NetworkModule
import seigneur.gauvain.mycourt.di.modules.RoomModule
import seigneur.gauvain.mycourt.di.modules.NetworkErrorInteractorModule
import seigneur.gauvain.mycourt.di.modules.SharedPrefsModule
import seigneur.gauvain.mycourt.di.modules.ConnectivityModule

/**
 * Created by gse on 26/03/2018.
 */
@Singleton
@Component(modules = [
    AppModule::class,
    NetworkModule::class,
    SharedPrefsModule::class,
    ConnectivityModule::class,
    RoomModule::class,
    NetworkErrorInteractorModule::class])
interface AppComponent {
    fun inject(app: MyCourtApp)

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: Application): Builder

        fun network(networkModule: NetworkModule): Builder
        fun sharedPrefs(sharedPrefsModule: SharedPrefsModule): Builder
        fun connectivityWatcher(connectivityModule: ConnectivityModule): Builder
        fun dataBase(roomModule: RoomModule): Builder
        fun rxErrorHandler(networkErrorInteractorModule: NetworkErrorInteractorModule): Builder
        // Builder userManager(UserModule userModule);
        fun build(): AppComponent
    }

}


