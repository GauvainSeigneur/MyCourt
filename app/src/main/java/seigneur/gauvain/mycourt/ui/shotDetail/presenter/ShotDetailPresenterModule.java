package seigneur.gauvain.mycourt.ui.shotDetail.presenter;

import dagger.Binds;
import dagger.Module;
import seigneur.gauvain.mycourt.di.scope.PerActivity;

@Module
public abstract class ShotDetailPresenterModule {
    @Binds
    @PerActivity
    abstract ShotDetailPresenter shotDetailPresenter(ShotDetailPresenterImpl shotDetailPresenterImpl);
}
