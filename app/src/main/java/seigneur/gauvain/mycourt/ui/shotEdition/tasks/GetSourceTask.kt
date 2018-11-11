package seigneur.gauvain.mycourt.ui.shotEdition.tasks

import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import seigneur.gauvain.mycourt.data.model.Draft
import seigneur.gauvain.mycourt.data.model.Shot
import seigneur.gauvain.mycourt.data.repository.TempDataRepository
import seigneur.gauvain.mycourt.utils.Constants
import timber.log.Timber

class GetSourceTask(private val mTempDataRepository: TempDataRepository,
                    private val mCompositeDisposable: CompositeDisposable,
                    private val mSourceCallback: SourceCallback) {

    /*
    *********************************************************************************************
    * Manage source
    *********************************************************************************************/
    /**
     * Check whether if the activity is opened from draft registered in database or other
     */
    fun getOriginOfEditRequest() {
        mCompositeDisposable
                .add(Single.just(mTempDataRepository.draftCallingSource)
                        .subscribe(
                                this::manageSource,
                                this::manageSourceTypeError)
                )
    }

    /**
     * get shot edition info according to source of the intent which opens the activity
     * @param source - can be draft, shot, or fab
     */
    private fun manageSource(source: Int) {
        when (source) {
            //user wishes to continue edit a stored draft
            Constants.SOURCE_DRAFT -> getShotDraft() //edition mode stored in draft, get draft to know edition mode
            //User wishes to update a published shot
            Constants.SOURCE_SHOT -> getShot()
            //User wishes to create a shot
            Constants.SOURCE_FAB -> {
                val shot = Shot("","","",null) //create an empty shot
                val draft = Draft(
                        0, Constants.EDIT_MODE_NEW_SHOT,
                        null, null, null, shot)
                mSourceCallback.setUpTempDraft(draft)
                mSourceCallback.dataForUIReady()
            }
        }
    }

    /**
     * Manage error during manageSource operation
     * @param throwable - throwable
     */
    private fun manageSourceTypeError(throwable: Throwable) {
        Timber.d(throwable)
    }

    /*
    *********************************************************************************************
    * Get shot object if source is Constants.SOURCE_SHOT
    *********************************************************************************************/
    private fun getShot() {
        mCompositeDisposable.add(Single.just(mTempDataRepository.shot)
                .subscribe(
                        this::manageShotInfo,
                        this::onGetShotError)
        )
    }

    private fun manageShotInfo(shot: Shot) {
        val draft = Draft(
                0,
                Constants.EDIT_MODE_UPDATE_SHOT,
                shot.imageHidpi, null, null,
                shot)
        mSourceCallback.setUpTempDraft(draft)
        mSourceCallback.dataForUIReady()
    }

    private fun onGetShotError(throwable: Throwable) {
        Timber.d(throwable)
    }

    /*
    *********************************************************************************************
    * Get ShotDraft object if source is Constants.SOURCE_SHOT
    *********************************************************************************************/
    private fun getShotDraft() {
        mCompositeDisposable.add(Single.just(mTempDataRepository.shotDraft)
                .subscribe(
                        this::manageShotDraftInfo,
                        this::onGetShotDraftError
                )
        )
    }

    private fun manageShotDraftInfo(shotDraft: Draft) {
        mSourceCallback.setUpTempDraft(shotDraft)
        mSourceCallback.dataForUIReady()
        Timber.d("typeofDraft : " + shotDraft.typeOfDraft)
    }

    private fun onGetShotDraftError(throwable: Throwable) {
        Timber.d(throwable)
    }


    interface SourceCallback {

        fun setUpTempDraft(draft: Draft)

        fun dataForUIReady()


    }


}
