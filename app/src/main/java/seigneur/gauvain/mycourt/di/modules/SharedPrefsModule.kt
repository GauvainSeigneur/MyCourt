package seigneur.gauvain.mycourt.di.modules

import android.content.Context

import javax.inject.Singleton

import dagger.Module
import dagger.Provides
import seigneur.gauvain.mycourt.data.local.SharedPrefs

@Module
class SharedPrefsModule(private val context: Context) {

    @Singleton
    @Provides
    internal fun providesSharedprefs(): SharedPrefs {
        return SharedPrefs(context.getSharedPreferences("PrefName", Context.MODE_PRIVATE))
    }

}
