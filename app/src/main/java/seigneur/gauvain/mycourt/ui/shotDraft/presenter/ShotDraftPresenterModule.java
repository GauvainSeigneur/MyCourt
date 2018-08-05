package seigneur.gauvain.mycourt.ui.shotDraft.presenter;

import dagger.Binds;
import dagger.Module;
import seigneur.gauvain.mycourt.di.scope.PerFragment;

@Module
public abstract class ShotDraftPresenterModule {
    @Binds
    @PerFragment
    abstract ShotDraftPresenter postPresenter(ShotDraftPresenterImpl postPresenterImpl);
}
