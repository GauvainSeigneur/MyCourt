package seigneur.gauvain.mycourt.ui.shots.presenter;

import dagger.Binds;
import dagger.Module;
import seigneur.gauvain.mycourt.di.scope.PerFragment;
import seigneur.gauvain.mycourt.ui.shots.view.ShotsView;

@Module
public abstract class ShotsPresenterModule {
    @Binds
    @PerFragment
    abstract ShotsPresenter<ShotsView> shotsPresenter(
            ShotsPresenterImpl<ShotsView>  shotsPresenterImpl);
}
