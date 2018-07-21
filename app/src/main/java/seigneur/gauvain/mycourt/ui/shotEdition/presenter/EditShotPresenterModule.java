package seigneur.gauvain.mycourt.ui.shotEdition.presenter;

import dagger.Binds;
import dagger.Module;
import seigneur.gauvain.mycourt.di.scope.PerActivity;

@Module
public abstract class EditShotPresenterModule {
    @Binds
    @PerActivity
    abstract EditShotPresenter createPostPresenter(EditShotPresenterImpl createPostPresenter);
}
