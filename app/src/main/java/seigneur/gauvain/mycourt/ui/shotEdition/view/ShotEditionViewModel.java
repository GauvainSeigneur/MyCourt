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

    //Image data
    public MutableLiveData<Uri> imageCroppedUri =new MutableLiveData<>();
    public MutableLiveData<String> imagePickedFormat = new MutableLiveData<>();
    public MutableLiveData<Boolean> isImageChanged = new MutableLiveData<>();
    //Edition mode data
    public MutableLiveData<Integer> mSource = new MutableLiveData<>();


    @Inject
    public ShotEditionViewModel() {}

    @Override
    public void onCleared(){
        super.onCleared();
        Timber.d("viewmodel cleared");
    }

    /*
     *************************************************************************
     * GETTER for presenter
     *************************************************************************/
    public LiveData<String> getImagePickedFormat() {
        return imagePickedFormat;
    }

    public LiveData<Uri> getImageCroppedUri() {
        return imageCroppedUri;
    }

    public LiveData<Boolean> getImageChanged() {
        return isImageChanged;
    }

    public LiveData<Integer> getSource() {
        return mSource;
    }

}
