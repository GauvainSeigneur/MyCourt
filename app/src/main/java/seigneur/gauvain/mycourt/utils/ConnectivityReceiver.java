package seigneur.gauvain.mycourt.utils;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class ConnectivityReceiver {

    ConnectivityManager connectivityManager;
    boolean isOnline;

    public ConnectivityReceiver(ConnectivityManager connectivityManager) {
        this.connectivityManager=connectivityManager;
    }

    public boolean isOnline() {
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        //should check null because in airplane mode it will be null
        isOnline= (activeNetwork != null && activeNetwork.isConnectedOrConnecting());
        return isOnline;

    }

}