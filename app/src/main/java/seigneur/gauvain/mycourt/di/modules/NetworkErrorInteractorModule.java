package seigneur.gauvain.mycourt.di.modules;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import seigneur.gauvain.mycourt.utils.rx.NetworkErrorHandler;
import seigneur.gauvain.mycourt.utils.rx.NetworkErrorHandlerImpl;

/**
 * Created by Gauvain on 26/06/2018.
 * This modules allows to provides NetworkErrorHandler interface into presenters to handle
 * IO errors during api request
 */
@Module
public class NetworkErrorInteractorModule {
    @Singleton
    @Provides
    public NetworkErrorHandler providesRxErrorInteractor(){
        return new NetworkErrorHandlerImpl();
    }
}
