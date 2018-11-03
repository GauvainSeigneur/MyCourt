package seigneur.gauvain.mycourt.di.modules

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import seigneur.gauvain.mycourt.ui.main.MainViewModel
import seigneur.gauvain.mycourt.ui.pin.PinViewModel
import seigneur.gauvain.mycourt.ui.shotDraft.ShotDraftViewModel
import seigneur.gauvain.mycourt.ui.shotDetail.ShotDetailViewModel
import seigneur.gauvain.mycourt.ui.shotEdition.ShotEditionViewModel
import seigneur.gauvain.mycourt.ui.shots.ShotsViewModel
import seigneur.gauvain.mycourt.ui.user.UserViewModel
import seigneur.gauvain.mycourt.di.scope.ViewModelKey
import seigneur.gauvain.mycourt.data.viewModel.FactoryViewModel
import seigneur.gauvain.mycourt.ui.splash.SplashViewModel

@Module
abstract class ViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(PinViewModel::class)
    internal abstract fun bindPinViewModel(pinViewModel: PinViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MainViewModel::class)
    internal abstract fun bindMainViewModel(mainViewModel: MainViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ShotsViewModel::class)
    internal abstract fun bindShotsViewModel(shotsViewModel: ShotsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(UserViewModel::class)
    internal abstract fun binduserViewModel(userViewModel: UserViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ShotDraftViewModel::class)
    internal abstract fun bindShotDraftViewModel(shotDraftViewModel: ShotDraftViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ShotEditionViewModel::class)
    internal abstract fun bibndShotEditionViewModel(shotEditionViewModel: ShotEditionViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SplashViewModel::class)
    internal abstract fun bindSplashVieModel(splashViewModel: SplashViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ShotDetailViewModel::class)
    internal abstract fun bindShotDetailViewModel(shotDetailViewModel: ShotDetailViewModel): ViewModel

    @Binds
    internal abstract fun bindViewModelFactory(factory: FactoryViewModel): ViewModelProvider.Factory
}
