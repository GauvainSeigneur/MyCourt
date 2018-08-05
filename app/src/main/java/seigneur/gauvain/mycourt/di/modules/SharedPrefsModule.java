package seigneur.gauvain.mycourt.di.modules;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import seigneur.gauvain.mycourt.data.local.SharedPrefs;

@Module
public class SharedPrefsModule {

    private Context context;

    public SharedPrefsModule(Context context) {
        this.context = context;
    }

    @Singleton
    @Provides
    SharedPrefs providesSharedprefs() {
        SharedPrefs sharedPrefs = new SharedPrefs(context.getSharedPreferences("PrefName",Context.MODE_PRIVATE));
        return sharedPrefs;
    }
}
