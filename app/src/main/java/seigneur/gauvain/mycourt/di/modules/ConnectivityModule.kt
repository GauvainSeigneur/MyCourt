package seigneur.gauvain.mycourt.di.modules

import android.content.Context
import android.net.ConnectivityManager
import javax.inject.Singleton

import dagger.Module
import dagger.Provides
import seigneur.gauvain.mycourt.utils.ConnectivityReceiver

@Module
class ConnectivityModule(private val mContext: Context) {

    @Singleton
    @Provides
    internal fun provideConnectivityManager(): ConnectivityManager {
        return mContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        //as to cast, in java it will be
        //return (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    @Singleton
    @Provides
    internal fun provideConnectivityReceiver(connectivityManager: ConnectivityManager): ConnectivityReceiver {
        return ConnectivityReceiver(connectivityManager)
    }

}