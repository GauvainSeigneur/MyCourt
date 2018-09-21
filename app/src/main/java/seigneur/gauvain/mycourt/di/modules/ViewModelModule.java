package seigneur.gauvain.mycourt.di.modules;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;



import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;
import seigneur.gauvain.mycourt.data.viewModel.TestViewModel;
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
    @ViewModelKey(TestViewModel.class)
    abstract ViewModel bindUserProfileViewModel(TestViewModel repoViewModel);


    @Binds
    @IntoMap
    @ViewModelKey(UserViewModel.class)
    abstract ViewModel binduserViewModel(UserViewModel userViewModel);

    @Binds
    abstract ViewModelProvider.Factory bindViewModelFactory(FactoryViewModel factory);
}
