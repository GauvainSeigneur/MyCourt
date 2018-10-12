package seigneur.gauvain.mycourt.ui.shotDetail;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import javax.inject.Inject;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import seigneur.gauvain.mycourt.data.model.Draft;
import seigneur.gauvain.mycourt.data.model.Shot;
import seigneur.gauvain.mycourt.data.model.ShotDraft;
import seigneur.gauvain.mycourt.data.repository.ShotDraftRepository;
import seigneur.gauvain.mycourt.data.repository.TempDataRepository;
import seigneur.gauvain.mycourt.utils.Constants;
import seigneur.gauvain.mycourt.utils.SingleLiveEvent;
import timber.log.Timber;

public class ShotDetailViewModel extends ViewModel {

    @Inject
    TempDataRepository mTempDataRepository;

    @Inject
    ShotDraftRepository mShotDraftRepository;

    private CompositeDisposable mCompositeDisposable    = new CompositeDisposable();
    private MutableLiveData<Shot> mShot = new MutableLiveData<>();
    private final SingleLiveEvent<Void> mEditClicked = new SingleLiveEvent<>();

    @Inject
    public ShotDetailViewModel() {}

    @Override
    public void onCleared(){
        super.onCleared();
        mCompositeDisposable.clear();
    }

    /*
     *********************************************************************************************
     * PUBLIC METHODS CALLED IN ACTIVITY
     *********************************************************************************************/
    public void init() {
        //Current user is at least fetch from API, so don't request it again unless a refresh
        // is called from user (isUserDirty)
        if (mShot.getValue()!=null) {
            Timber.d("do not fetch shot again");
            return;
        }
        Timber.d("fetch shot");
        fetchShot();
    }

    public LiveData<Shot> getShot() {
        return mShot;
    }

    public void onEditClicked() {
        checkDraftAndGoToEdition();
    }

    public SingleLiveEvent<Void> getEditClickedEvent() {
        return mEditClicked;
    }


    /*
     *********************************************************************************************
     * PRIVATE METHODS
     *********************************************************************************************/

    /*
     **************************************************************************
     * Get Shot clicked
     *************************************************************************/
    private void fetchShot() {
        mCompositeDisposable.add(Single.just(mTempDataRepository.getShot())
                .subscribe(
                        this::doOnShotRetrieve, //success
                        this::doOnShotError //error
                )
        );
    }

    /**
     * Manage when shot is retrieved
     * @param shot - shot retrieve from TempRepository
     */
    private void doOnShotRetrieve(Shot shot) {
        mShot.setValue(shot);
    }

    /**
     * Manage error
     * @param error - Throwable
     */
    private void doOnShotError(Throwable error) {
        Timber.d(error);
        //todo - single live event to finish activity ?
    }

    /**************************************************************************
     * Check if the shot has already a draft saved in DB. if it has,
     * call its draft, if not, just go to Edition
     *************************************************************************/
    private void checkDraftAndGoToEdition() {
        if (mShot.getValue()!=null){
            mCompositeDisposable.add(
                    mShotDraftRepository.getShotDraftByShotId(mShot.getValue().getId())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    this::doIfDraftFoundInDB,       //maybe onNext: shot draft found
                                    t -> Timber.e(t),               //error
                                    this::doIfNoDraftFoundInDB      //maybe onComplete without onNext called : nothing found
                            ));
        }
    }

    /**
     * Manage if ShotDraft is found in db
     * @param shotDraft - shotDRaft found in db
     */
    private void doIfDraftFoundInDB(Draft shotDraft) {
        Timber.d("Draft already exists");
        mTempDataRepository.setDraftCallingSource(Constants.SOURCE_DRAFT);  //TODO - PUBLISH OPERATOR RX
        mTempDataRepository.setShotDraft(shotDraft);                        //TODO - PUBLISH OPERATOR RX
        mEditClicked.call();
    }

    /**
     * Manage if no ShotDraft found in db
     */
    private void doIfNoDraftFoundInDB() {
        Timber.d("no draft for this shot");
        mTempDataRepository.setDraftCallingSource(Constants.SOURCE_SHOT);   //TODO - PUBLISH OPERATOR RX
        mTempDataRepository.setShot(mShot.getValue());                     //TODO - PUBLISH OPERATOR RX
        mEditClicked.call();

    }



}
