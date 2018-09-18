package seigneur.gauvain.mycourt.ui.shotDetail.presenter;

import dagger.Binds;
import dagger.Module;
import seigneur.gauvain.mycourt.di.scope.PerActivity;
import seigneur.gauvain.mycourt.ui.shotDetail.view.ShotDetailView;

@Module
public abstract class ShotDetailPresenterModule {
    @Binds
    @PerActivity
    abstract ShotDetailPresenter<ShotDetailView> shotDetailPresenter(
            ShotDetailPresenterImpl<ShotDetailView>  shotDetailPresenterImpl);
}
