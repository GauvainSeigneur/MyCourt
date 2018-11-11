package seigneur.gauvain.mycourt.ui.shotDraft

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import io.reactivex.Completable

import javax.inject.Inject

import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import seigneur.gauvain.mycourt.data.model.Draft
import seigneur.gauvain.mycourt.data.repository.ShotDraftRepository
import seigneur.gauvain.mycourt.data.repository.TempDataRepository
import seigneur.gauvain.mycourt.utils.Constants
import seigneur.gauvain.mycourt.utils.SingleLiveEvent
import timber.log.Timber

class ShotDraftViewModel

@Inject
constructor() : ViewModel() {

    @Inject
    lateinit var mShotDraftRepository: ShotDraftRepository

    @Inject
    lateinit var mTempDataRepository: TempDataRepository

    private val mCompositeDisposable = CompositeDisposable()
    private val mStopRefreshEvent = SingleLiveEvent<Void>()
    private val mShotDrafts = MutableLiveData<List<Draft>>()
    private var isRefreshing: Boolean = false
    private val mDeleteOpeDone = SingleLiveEvent<Int>()


    public override fun onCleared() {
        super.onCleared()
        mCompositeDisposable.clear()
    }

    /*
    ************************************************************************************
    *  Live data and and events to be subscribed
    ************************************************************************************/
    fun dbChanged(): SingleLiveEvent<Void> {
        return mShotDraftRepository.onDraftDBChanged
    }

    fun deleteCickEvent(): SingleLiveEvent<Void> {
        return mTempDataRepository.deleteSelecteListCmd
    }

    val editMode: LiveData<Int>
        get() = mTempDataRepository.editMode

    val itemClickedEvent = SingleLiveEvent<Draft>()

    val drafts: LiveData<List<Draft>>
        get() = mShotDrafts

    fun getDeleteOpeResult(): SingleLiveEvent<Int> {
        return mDeleteOpeDone
    }
    /*
    ************************************************************************************
    *  Public functions called bu subscribers
    ************************************************************************************/
    /**
     * fetch draft list from DB
     */
    fun fetchShotDrafts() {
        isRefreshing = false
        mCompositeDisposable.add(fetchDrafts()
                .subscribe(
                        this::doOnDraftFound,
                        this::doOnError,
                        this::doOnNothingFound
                )
        )
    }

    /**
     * User has clicked on Draft in the list, deal with it
     * @param shotDraft - draft object clicked
     * @param position - position of the item in the list
     */
    fun onShotDraftClicked(shotDraft: Draft, position: Int) {
        mTempDataRepository.draftCallingSource = Constants.SOURCE_DRAFT
        mTempDataRepository.shotDraft = shotDraft
        itemClickedEvent.value = shotDraft
    }

    fun deleteSelectDrafts(ids: ArrayList<Long>) {
        for ((i, element) in ids.withIndex()) {
            val idToDelete = ids[i]
            mCompositeDisposable.add(deleteDrafts(idToDelete)
                    .subscribe(
                            this::onDeleteSucceed,
                            this::onDeleteError
                    )
            )

        }

    }

    /**
     * get ShotDrafts list from DB - Use mayBe because the list will be small
     * @return - List of ShotDraft
     */
    private fun fetchDrafts(): Maybe<List<Draft>> {
        Timber.d("getPostFromDB called")
        return mShotDraftRepository.shotDraft
    }

    private fun deleteDrafts(id:Long): Completable {
        return mShotDraftRepository.deleteDraft(id)
    }

    /**
     * ShotDrafts being found in DB - do something with it
     * @param shotDrafts - list Found in DB
     */
    private fun doOnDraftFound(shotDrafts: List<Draft>) {
        mShotDrafts.value = shotDrafts
    }

    /**
     * When nothing found in DB, stop refreshing and set up a dedicated view
     */
    private fun doOnNothingFound() {
        if (isRefreshing) {
            mStopRefreshEvent.call()
        }

    }

    /**
     * Error happened during shotDraft fetching
     * @param throwable - error
     */
    private fun doOnError(throwable: Throwable) {
        Timber.e(throwable)
        //TODO -SINGLE EVENT?
    }

    private fun onDeleteSucceed() {
       mDeleteOpeDone.value=0
       Timber.d("delete succeed")
    }

    private fun onDeleteError(throwable: Throwable) {
        Timber.e(throwable)
        mDeleteOpeDone.value=-1
    }

}
