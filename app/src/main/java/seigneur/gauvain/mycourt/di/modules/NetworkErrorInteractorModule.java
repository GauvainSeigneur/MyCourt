package seigneur.gauvain.mycourt.di.modules;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import seigneur.gauvain.mycourt.utils.rx.NetworkErrorHandler;
import seigneur.gauvain.mycourt.utils.rx.NetworkErrorHandlerImpl;

@Module
public class NetworkErrorInteractorModule {
    @Singleton
    @Provides
    public NetworkErrorHandler providesRxErrorInteractor(){
        return new NetworkErrorHandlerImpl();
    }
}
