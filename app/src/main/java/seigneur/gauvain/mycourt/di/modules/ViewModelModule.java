package seigneur.gauvain.mycourt.di.modules;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;
import seigneur.gauvain.mycourt.ui.main.MainViewModel;
import seigneur.gauvain.mycourt.ui.pin.PinViewModel;
import seigneur.gauvain.mycourt.ui.shotDraft.ShotDraftViewModel;
import seigneur.gauvain.mycourt.ui.shotDetail.ShotDetailViewModel;
import seigneur.gauvain.mycourt.ui.shotEdition.view.ShotEditionViewModel;
import seigneur.gauvain.mycourt.ui.shots.ShotsViewModel;
import seigneur.gauvain.mycourt.ui.user.UserViewModel;
import seigneur.gauvain.mycourt.di.scope.ViewModelKey;
import seigneur.gauvain.mycourt.data.viewModel.FactoryViewModel;
import seigneur.gauvain.mycourt.ui.splash.SplashViewModel;

@Module
public abstract class ViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(PinViewModel.class)
    abstract ViewModel bindPinViewModel(PinViewModel pinViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(MainViewModel.class)
    abstract ViewModel bindMainViewModel(MainViewModel mainViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(ShotsViewModel.class)
    abstract ViewModel bindShotsViewModel(ShotsViewModel shotsViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(UserViewModel.class)
    abstract ViewModel binduserViewModel(UserViewModel userViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(ShotDraftViewModel.class)
    abstract ViewModel bindShotDraftViewModel(ShotDraftViewModel shotDraftViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(ShotEditionViewModel.class)
    abstract ViewModel bibndShotEditionViewModel(ShotEditionViewModel shotEditionViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(SplashViewModel.class)
    abstract ViewModel bindSplashVieModel(SplashViewModel splashViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(ShotDetailViewModel.class)
    abstract ViewModel bindShotDetailViewModel(ShotDetailViewModel shotDetailViewModel);


    @Binds
    abstract ViewModelProvider.Factory bindViewModelFactory(FactoryViewModel factory);
}
