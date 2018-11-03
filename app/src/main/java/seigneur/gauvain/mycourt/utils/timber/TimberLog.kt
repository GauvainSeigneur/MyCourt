package seigneur.gauvain.mycourt.utils.timber

import seigneur.gauvain.mycourt.BuildConfig
import timber.log.Timber

object TimberLog {

    fun init() {
        if (BuildConfig.DEBUG)
            Timber.plant(Timber.DebugTree())
        else
            Timber.plant(TimberReleaseTree())
    }
}