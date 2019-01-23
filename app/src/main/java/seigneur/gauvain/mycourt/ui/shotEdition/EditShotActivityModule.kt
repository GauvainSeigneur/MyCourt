package seigneur.gauvain.mycourt.ui.shotEdition

import android.app.Activity

import dagger.Binds
import dagger.Module
import seigneur.gauvain.mycourt.di.scope.PerActivity

/**
 * Provides dependencies.
 */
@Module
abstract class EditShotActivityModule {
    /**
     *
     * @param mEditShotActivity
     * @return implementation  of the activity
     */
    @Binds
    @PerActivity
    internal abstract fun activity(mEditShotActivity: EditShotActivity): Activity

}