package seigneur.gauvain.mycourt.ui.shotEdition.view;

import android.app.Activity;
import android.arch.lifecycle.LifecycleOwner;

import dagger.Binds;
import dagger.Module;
import seigneur.gauvain.mycourt.di.scope.PerActivity;

/**
 * Provides dependencies.
 */
@Module
public abstract class EditShotActivityModule {
    /**
     *
     * @param mEditShotActivity
     * @return impelemntation  of the activity
     */
    @Binds
    @PerActivity
    abstract Activity activity(EditShotActivity mEditShotActivity);

    /**
     *
     * @param mEditShotActivity
     * @return
     */
    @Binds
    @PerActivity
    abstract EditShotView createPostView(EditShotActivity mEditShotActivity);

    /**
     *
     * @param mEditShotActivity
     * @return
     */
   /* @Binds
    @PerActivity
    abstract LifecycleOwner lifecycleOwner(EditShotActivity mEditShotActivity);*/


}