package seigneur.gauvain.mycourt.ui.shotEdition.tasks

import android.content.Context
import android.net.Uri

import java.util.ArrayList
import java.util.Date

import javax.inject.Inject

import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import seigneur.gauvain.mycourt.data.model.Draft
import seigneur.gauvain.mycourt.data.repository.ShotDraftRepository
import seigneur.gauvain.mycourt.utils.Constants
import timber.log.Timber

class StoreDraftTask(
        private val mCompositeDisposable: CompositeDisposable,
        private val mShotDraftRepository: ShotDraftRepository,
        private val mStoreRequestListener: StoreRequestListener) {

    /**
     * Store cropped image in external storage and get Uri of the this file to save it in DB
     * @param context - use Application context
     * @param imageCroppedFormat - manage cropping according to the format
     * @param croppedFileUri -  uri of the image after being cropped
     */
    fun storeDraftImage(context: Context,
                        imageCroppedFormat: String,
                        croppedFileUri: Uri) {
        mCompositeDisposable.add(
                mShotDraftRepository.storeImageAndReturnItsUri(imageCroppedFormat, croppedFileUri, context)
                        .onErrorResumeNext { t ->
                            if (t is NullPointerException)
                                Single.error(t)
                            else
                                Single.error(t)
                        } //todo : to comment this
                        .subscribe(
                                Consumer<String> {mStoreRequestListener.onSaveImageSuccess(it) },
                                Consumer<Throwable> { this.onDraftSavingError(it) }
                        )
        )
    }

    /**
     * Save Draft in DB
     * @param draft - draft created at the beginning of activity
     */
    fun save(draft: Draft) {
        mCompositeDisposable.add(
                mShotDraftRepository.storeShotDraft(draft)
                        .subscribe(
                                this::onDraftSaved,
                                this::onDraftSavingError
                        )
        )

    }


    /**
     * Update draft in db
     * @param draft - draft created at the beginning of activity
     */
    fun update(draft: Draft) {
        mCompositeDisposable.add(
                mShotDraftRepository.updateShotDraft(draft)
                        .subscribe(
                                this::onDraftSaved, //todo Listener
                                this::onDraftSavingError //todo Listener
                        )
        )

    }

    /**
     * Draft has been saved/updated in DB
     */
    fun onDraftSaved() {
        mStoreRequestListener.onStoreDraftSucceed()
        Timber.d("draft saved")
    }

    /**
     * An error occurred while trying to saved draft in db
     * @param t - throwable
     */
    fun onDraftSavingError(t: Throwable) {
        Timber.d(t)
        mStoreRequestListener.onFailed()
    }

    /**
     * CALLBACK FOR VIEWMODEL
     */
    interface StoreRequestListener {

        fun onSaveImageSuccess(uri: String)

        fun onStoreDraftSucceed()

        fun onFailed()
    }


}
