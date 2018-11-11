package seigneur.gauvain.mycourt.data.repository

import android.content.Context
import android.net.Uri
import javax.inject.Inject
import javax.inject.Singleton
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import seigneur.gauvain.mycourt.data.local.dao.PostDao
import seigneur.gauvain.mycourt.data.model.Draft
import seigneur.gauvain.mycourt.utils.image.ImageUtils
import seigneur.gauvain.mycourt.utils.SingleLiveEvent

@Singleton
class ShotDraftRepository @Inject
constructor() {

    @Inject
    lateinit var postDao: PostDao

    //Notify subscribers that an operation has been done in DB and it has changed
    var onDraftDBChanged = SingleLiveEvent<Void>()

    /**
     * Check if a list of Draft exists in DB
     */
    val shotDraft: Maybe<List<Draft>>
        get() = postDao.allPost
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())

    /**
     * get a Draft by its ID
     */
    fun getShotDraftByShotId(ShotID: String): Maybe<Draft> {
        return postDao.getShotDraftByShotId(ShotID)
    }

    /**
     * Update a draft already stored in DB
     */
    fun updateShotDraft(shotDraft: Draft): Completable {
        return Completable.fromRunnable { postDao.updateDraft(shotDraft) }
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete {
                    //notify With Single event
                    onDraftDBChanged.call()
                }
    }

    /**
     * Store a draft in DB
     */
    fun storeShotDraft(shotDraft: Draft): Completable {
        return Completable.fromRunnable { postDao.insertPost(shotDraft) }
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete {
                    //notify With Single event
                    onDraftDBChanged.call()
                }
    }

    /**
     * delete a draft from DB thank to its ID
     */
    fun deleteDraft(id: Long): Completable {
        return Completable.fromRunnable {
            postDao.deletDraftByID(id.toInt()) }
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete {
                    //notify With Single event
                    onDraftDBChanged.call()
                }
    }

    /**
     * Store an image file in a dedicated folder in external storage and return its URI in order to display it
     * @param imageCroppedFormat
     * @param croppedFileUri
     * @param context
     */
    fun storeImageAndReturnItsUri(imageCroppedFormat: String,
                                  croppedFileUri: Uri,
                                  context: Context): Single<String> {
        return Single.fromCallable {
            ImageUtils.saveImageAndGetItsFinalUri(imageCroppedFormat, croppedFileUri, context) }
    }
}
