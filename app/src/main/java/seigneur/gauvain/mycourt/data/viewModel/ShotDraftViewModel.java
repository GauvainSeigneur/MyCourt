package seigneur.gauvain.mycourt.data.viewModel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.LiveDataReactiveStreams;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import seigneur.gauvain.mycourt.data.model.ShotDraft;
import seigneur.gauvain.mycourt.data.model.User;
import seigneur.gauvain.mycourt.data.repository.ShotDraftRepository;
import seigneur.gauvain.mycourt.data.repository.UserRepository;
import seigneur.gauvain.mycourt.utils.ConnectivityReceiver;
import timber.log.Timber;

public class ShotDraftViewModel extends ViewModel {

    @Inject
    ShotDraftRepository mShotDraftRepository;

    private final CompositeDisposable disposables = new CompositeDisposable();

    @Inject
    public ShotDraftViewModel() {}

    @Override
    public void onCleared(){
        super.onCleared();
        disposables.clear();
    }

    /*
     *********************************************************************************************
     * PUBLIC METHODS CALLED IN PRESENTER
     *********************************************************************************************/
    public void init() {
        if (getShotDrafts() !=null)
            return;
    }

    public LiveData<List<ShotDraft>> getShotDrafts() {
        return LiveDataReactiveStreams.fromPublisher(
                mShotDraftRepository.getShotDraft().toFlowable());
    }

}
