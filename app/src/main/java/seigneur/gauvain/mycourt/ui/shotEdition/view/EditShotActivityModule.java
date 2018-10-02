package seigneur.gauvain.mycourt.ui.shotEdition.view;

import android.app.Activity;
import android.arch.lifecycle.LifecycleOwner;

import dagger.Binds;
import dagger.Module;
import seigneur.gauvain.mycourt.di.scope.PerActivity;
import seigneur.gauvain.mycourt.ui.shotEdition.presenter.EditShotPresenterModule;

/**
 * Provides dependencies.
 */
@Module(includes = EditShotPresenterModule.class)
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




}