package seigneur.gauvain.mycourt.di.modules;

import android.content.Context;
import android.net.ConnectivityManager;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import seigneur.gauvain.mycourt.utils.ConnectivityReceiver;

@Module
public class ConnectivityModule {

    private Context mContext;

    public ConnectivityModule (Context context) {
        this.mContext=context;
    }

    @Singleton
    @Provides
    ConnectivityManager provideConnectivityManager() {
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm;
    }

    @Singleton
    @Provides
    ConnectivityReceiver provideConnectivityReceiver(ConnectivityManager connectivityManager) {
        return new ConnectivityReceiver(connectivityManager);
    }

}