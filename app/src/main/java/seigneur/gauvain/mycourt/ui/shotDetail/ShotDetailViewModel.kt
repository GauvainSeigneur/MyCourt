package seigneur.gauvain.mycourt.ui.shotDetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

import javax.inject.Inject

import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import seigneur.gauvain.mycourt.data.model.Draft
import seigneur.gauvain.mycourt.data.model.Shot
import seigneur.gauvain.mycourt.data.repository.ShotDraftRepository
import seigneur.gauvain.mycourt.data.repository.TempDataRepository
import seigneur.gauvain.mycourt.utils.Constants
import seigneur.gauvain.mycourt.utils.SingleLiveEvent
import timber.log.Timber

class ShotDetailViewModel

@Inject
constructor() : ViewModel() {

    @Inject
    lateinit var mTempDataRepository: TempDataRepository

    @Inject
    lateinit var mShotDraftRepository: ShotDraftRepository

    private val mCompositeDisposable = CompositeDisposable()
    private val mShot = MutableLiveData<Shot>()
    val shot: LiveData<Shot>
        get() = mShot //custom accessors,

    val editClickedEvent = SingleLiveEvent<Void>()

    public override fun onCleared() {
        super.onCleared()
        mCompositeDisposable.clear()
    }

    /*
     *********************************************************************************************
     * PUBLIC METHODS CALLED IN ACTIVITY
     *********************************************************************************************/
    fun init() {
        //Current user is at least fetch from API, so don't request it again unless a refresh
        // is called from user (isUserDirty)
        if (mShot.value != null) {
            Timber.d("do not fetch shot again")
            return
        }
        Timber.d("fetch shot")
        fetchShot()
    }

    fun onEditClicked() {
        checkDraftAndGoToEdition()
    }

    /*
     **************************************************************************
     * Get Shot clicked
     *************************************************************************/
    private fun fetchShot() {
        mCompositeDisposable.add(Single.just(mTempDataRepository.shot)
                .subscribe(
                        this::doOnShotRetrieve, //success
                        this::doOnShotError //error
                )
        )
    }

    /**
     * Manage when shot is retrieved
     * @param shot - shot retrieve from TempRepository
     */
    private fun doOnShotRetrieve(shot: Shot) {
        mShot.value = shot
    }

    /**
     * Manage error
     * @param error - Throwable
     */
    private fun doOnShotError(error: Throwable) {
        Timber.d(error)
        //todo - single live event to finish activity ?
    }

    /**
     * Check if the shot has already a draft saved in DB. if it has,
     * call its draft, if not, just go to Edition
     */
    private fun checkDraftAndGoToEdition() {
        if (mShot.value != null) {
            mCompositeDisposable.add(
                    mShotDraftRepository.getShotDraftByShotId(mShot.value!!.id!!)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    this::doIfDraftFoundInDB, //maybe onNext: shot draft found
                                    { t -> Timber.e(t) }, //error
                                    this::doIfNoDraftFoundInDB      //maybe onComplete without onNext called : nothing found
                            ))
        }
    }

    /**
     * Manage if ShotDraft is found in db
     * @param shotDraft - shotDRaft found in db
     */
    private fun doIfDraftFoundInDB(shotDraft: Draft) {
        Timber.d("Draft already exists")
        mTempDataRepository.draftCallingSource = Constants.SOURCE_DRAFT  //TODO - PUBLISH OPERATOR R
        mTempDataRepository.shotDraft = shotDraft                        //TODO - PUBLISH OPERATOR RX
        editClickedEvent.call()
    }

    /**
     * Manage if no ShotDraft found in db
     */
    private fun doIfNoDraftFoundInDB() {
        Timber.d("no draft for this shot")
        mTempDataRepository.draftCallingSource = Constants.SOURCE_SHOT   //TODO - PUBLISH OPERATOR RX
        mTempDataRepository.shot = mShot.value!!                    //TODO - PUBLISH OPERATOR RX
        editClickedEvent.call()

    }


}
