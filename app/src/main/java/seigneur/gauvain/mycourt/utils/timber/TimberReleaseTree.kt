package seigneur.gauvain.mycourt.utils.timber

import timber.log.Timber

class TimberReleaseTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        //Use CrashLytics...
        /*if (priority == ERROR || priority == WARNING) {
            //Crashlytics.log(int priority, String tag, String msg);
        } else {
            //bitch please, is nothing!
        }*/

    }
}