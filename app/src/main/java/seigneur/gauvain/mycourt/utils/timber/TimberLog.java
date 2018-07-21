package seigneur.gauvain.mycourt.utils.timber;

import seigneur.gauvain.mycourt.BuildConfig;
import timber.log.Timber;

public class TimberLog {

    public static void init() {
        if (BuildConfig.DEBUG)
            Timber.plant(new Timber.DebugTree());
        else
            Timber.plant(new TimberReleaseTree());
    }
}