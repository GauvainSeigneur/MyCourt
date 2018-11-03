package seigneur.gauvain.mycourt.utils

import android.net.ConnectivityManager
import android.net.NetworkInfo

class ConnectivityReceiver(private var connectivityManager: ConnectivityManager) {
    private var isOnline: Boolean = false

    fun isOnline(): Boolean {
        val activeNetwork = connectivityManager.activeNetworkInfo
        //should check null because in airplane mode it will be null
        isOnline = activeNetwork != null && activeNetwork.isConnectedOrConnecting
        return isOnline

    }

}