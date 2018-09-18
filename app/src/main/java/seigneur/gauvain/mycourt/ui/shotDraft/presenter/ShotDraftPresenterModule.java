package seigneur.gauvain.mycourt.ui.shotDraft.presenter;

import dagger.Binds;
import dagger.Module;
import seigneur.gauvain.mycourt.di.scope.PerFragment;
import seigneur.gauvain.mycourt.ui.shotDraft.view.ShotDraftView;

@Module
public abstract class ShotDraftPresenterModule {
    @Binds
    @PerFragment
    abstract ShotDraftPresenter<ShotDraftView> postPresenter(
            ShotDraftPresenterImpl<ShotDraftView> postPresenterImpl);
}
