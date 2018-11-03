package seigneur.gauvain.mycourt.ui.shotDraft

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel

import javax.inject.Inject

import io.reactivex.Maybe
import io.reactivex.disposables.CompositeDisposable
import seigneur.gauvain.mycourt.data.model.Draft
import seigneur.gauvain.mycourt.data.repository.ShotDraftRepository
import seigneur.gauvain.mycourt.data.repository.TempDataRepository
import seigneur.gauvain.mycourt.utils.Constants
import seigneur.gauvain.mycourt.utils.SingleLiveEvent
import timber.log.Timber

class ShotDraftViewModel @Inject
constructor() : ViewModel() {

    @Inject
    lateinit var mShotDraftRepository: ShotDraftRepository

    @Inject
    lateinit var mTempDataRepository: TempDataRepository

    private val mCompositeDisposable = CompositeDisposable()
    private val mStopRefreshEvent = SingleLiveEvent<Void>()
    private val mShotDrafts = MutableLiveData<List<Draft>>()
    private var isRefreshing: Boolean = false

    val itemClickedEvent = SingleLiveEvent<Draft>()

    val drafts: LiveData<List<Draft>>
        get() = mShotDrafts

    public override fun onCleared() {
        super.onCleared()
        mCompositeDisposable.clear()
    }

    fun dbChanged(): SingleLiveEvent<Void> {
        return mShotDraftRepository.onDraftDBChanged
    }


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

    fun onRefresh(fromSwipeRefresh: Boolean) {
        if (fromSwipeRefresh) {
            isRefreshing = true
            mCompositeDisposable.add(fetchDrafts()
                    .subscribe(
                            this::doOnDraftFound,
                            this::doOnError,
                            this::doOnNothingFound
                    )
            )
        } else {
            Timber.d("not refreshing, nothing happened")
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

    /**
     * ShotDrafts being found in DB - do something with it
     * @param shotDrafts - list Found in DB
     */
    private fun doOnDraftFound(shotDrafts: List<Draft>) {
        Timber.d("list loaded" + shotDrafts.toString())
        //todo - live data
        mShotDrafts.value = shotDrafts

        /*if (!shotDrafts.isEmpty()) {
        } else {

        }*/

    }

    /**
     * When nothing found in DB, stop refreshing and set up a dedicated view
     */
    private fun doOnNothingFound() {
        if (isRefreshing) {
            mStopRefreshEvent.call()
            //mShotDraftView.stopRefresh(); //SINGLE EVENT ?
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


    fun onShotDraftClicked(shotDraft: Draft, position: Int) {
        mTempDataRepository.draftCallingSource = Constants.SOURCE_DRAFT
        mTempDataRepository.shotDraft = shotDraft
        itemClickedEvent.value = shotDraft

        // mShotDraftView.goToShotEdition(); //TODO -SINGLE EVENT

    }

    /*
     *********************************************************************************************
     * PUBLIC METHODS CALLED IN PRESENTER
     *********************************************************************************************/


}
