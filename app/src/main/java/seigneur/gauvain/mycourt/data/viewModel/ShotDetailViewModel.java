package seigneur.gauvain.mycourt.data.viewModel;

import android.arch.lifecycle.ViewModel;
import javax.inject.Inject;
import io.reactivex.disposables.CompositeDisposable;

public class ShotDetailViewModel extends ViewModel {

    private final CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    @Inject
    public ShotDetailViewModel() {}

    @Override
    public void onCleared(){
        super.onCleared();
        mCompositeDisposable.clear();
    }


}
