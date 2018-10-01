package seigneur.gauvain.mycourt.ui.shots;

import android.support.v4.app.Fragment;

import dagger.Binds;
import dagger.Module;
import seigneur.gauvain.mycourt.di.scope.PerFragment;

@Module()
public abstract class ShotsFragmentModule {
    @Binds
    @PerFragment
    abstract Fragment fragment(ShotsFragment shotsFragment);

}