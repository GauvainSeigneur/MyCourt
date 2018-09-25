package seigneur.gauvain.mycourt.ui.shotEdition.view;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.net.Uri;

import java.util.ArrayList;

import javax.inject.Inject;

import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import seigneur.gauvain.mycourt.data.model.Shot;
import seigneur.gauvain.mycourt.data.model.ShotDraft;
import seigneur.gauvain.mycourt.data.repository.ShotDraftRepository;
import seigneur.gauvain.mycourt.data.repository.ShotRepository;
import seigneur.gauvain.mycourt.data.repository.TempDataRepository;
import seigneur.gauvain.mycourt.utils.ConnectivityReceiver;
import seigneur.gauvain.mycourt.utils.Constants;
import seigneur.gauvain.mycourt.utils.rx.NetworkErrorHandler;
import timber.log.Timber;

public class ShotEditionViewModel extends ViewModel {

    private MutableLiveData<Uri> imageCroppedUri =new MutableLiveData<>();


    @Inject
    public ShotEditionViewModel() {}

    @Override
    public void onCleared(){
        super.onCleared();
        Timber.d("viewmodel cleared");
    }

    /*
     *************************************************************************
     * GETTER AND SETTER
     *************************************************************************/
    public MutableLiveData<Uri> mutableImageCroppedUri() {
        return imageCroppedUri;
    }

    public LiveData<Uri> getImageCroppedUri() {
        return imageCroppedUri;
    }

}
