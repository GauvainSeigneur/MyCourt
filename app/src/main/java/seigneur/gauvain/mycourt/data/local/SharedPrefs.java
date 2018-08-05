package seigneur.gauvain.mycourt.data.local;

import android.content.SharedPreferences;
import javax.inject.Inject;
import javax.inject.Singleton;

import timber.log.Timber;

/**
 * Created by gauvain on 11/03/2018.
 */
@Singleton
public class SharedPrefs {
    private SharedPreferences mSharedPreferences;
    private static String filename="Keys";
    //Values Saved
    public static final String kAccessToken	                    = "accesToken";

    @Inject
    public SharedPrefs(SharedPreferences sharedPreferences) {
        this.mSharedPreferences=sharedPreferences;
    }

    public void putString(String key, String value) {
        SharedPreferences.Editor editor;
        editor = mSharedPreferences.edit();
        editor.putString(key, value);
        //editor.commit(); // stop everything and save
        editor.apply();//Keep going and save when you are not busy - Available only in APIs 9 and above.  This is the preferred way of saving.
        Timber.d("PUT" +key+" "+value);
    }

    //get simply String with only key
    public String getString(String key) {
        return mSharedPreferences.getString(key, null);
    }

}

