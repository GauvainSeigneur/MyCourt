package seigneur.gauvain.mycourt.di.modules;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;
import seigneur.gauvain.mycourt.data.viewModel.ShotDraftViewModel;
import seigneur.gauvain.mycourt.ui.shotEdition.view.ShotEditionViewModel;
import seigneur.gauvain.mycourt.data.viewModel.UserViewModel;
import seigneur.gauvain.mycourt.di.scope.ViewModelKey;
import seigneur.gauvain.mycourt.data.viewModel.FactoryViewModel;

/**
 * Created by Philippe on 02/03/2018.
 */

@Module
public abstract class ViewModelModule {

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
    abstract ViewModelProvider.Factory bindViewModelFactory(FactoryViewModel factory);
}
