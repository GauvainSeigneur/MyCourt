package seigneur.gauvain.mycourt.utils.timber;
import timber.log.Timber;


public class TimberReleaseTree extends Timber.Tree {
    @Override
    protected void log(int priority, String tag, String message, Throwable t) {
        //Use CrashLytics...
        /*if (priority == ERROR || priority == WARNING) {
            //Crashlytics.log(int priority, String tag, String msg);
        } else {
            //bitch please, is nothing!
        }*/

    }
}