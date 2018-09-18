package seigneur.gauvain.mycourt.ui.shotEdition.presenter;

import dagger.Binds;
import dagger.Module;
import seigneur.gauvain.mycourt.di.scope.PerActivity;
import seigneur.gauvain.mycourt.ui.shotEdition.view.EditShotView;

@Module
public abstract class EditShotPresenterModule {
    @Binds
    @PerActivity
    abstract EditShotPresenter<EditShotView> createPostPresenter(EditShotPresenterImpl<EditShotView> createPostPresenter);
}
