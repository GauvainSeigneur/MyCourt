package seigneur.gauvain.mycourt.ui.shots.presenter;

import dagger.Binds;
import dagger.Module;
import seigneur.gauvain.mycourt.di.scope.PerFragment;

@Module
public abstract class ShotsPresenterModule {
    @Binds
    @PerFragment
    abstract ShotsPresenter shotsPresenter(ShotsPresenterImpl shotsPresenterImpl);
}
