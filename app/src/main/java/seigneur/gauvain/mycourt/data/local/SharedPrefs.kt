package seigneur.gauvain.mycourt.data.local

import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

import timber.log.Timber

/**
 * Created by gauvain on 11/03/2018.
 */
@Singleton
class SharedPrefs @Inject
constructor(private val mSharedPreferences: SharedPreferences) {



    fun putBoolean(key: String, value: Boolean) {
        val editor: SharedPreferences.Editor
        editor = mSharedPreferences.edit()
        editor.putBoolean(key, value)
        //editor.commit(); // stop everything and save
        editor.apply()//Keep going and save when you are not busy - Available only in APIs 9 and above.  This is the preferred way of saving.
        Timber.d("PUT$key $value")
    }

    //get simply String with only key
    fun getBoolean(key: String): Boolean? {
        return mSharedPreferences.getBoolean(key, false)
    }


    fun putString(key: String, value: String) {
        val editor: SharedPreferences.Editor
        editor = mSharedPreferences.edit()
        editor.putString(key, value)
        //editor.commit(); // stop everything and save
        editor.apply()//Keep going and save when you are not busy - Available only in APIs 9 and above.  This is the preferred way of saving.
        Timber.d("PUT$key $value")
    }

    //get simply String with only key
    fun getString(key: String): String? {
        return mSharedPreferences.getString(key, null)
    }

    companion object {
        private val filename = "Keys"
        //Values Saved
        val kAccessToken = "accesToken"
        //Values Saved
        val kNightMode= "nightMode"
    }

}

