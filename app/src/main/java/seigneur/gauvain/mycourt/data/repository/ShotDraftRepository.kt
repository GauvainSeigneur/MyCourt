package seigneur.gauvain.mycourt.data.repository

import android.arch.lifecycle.ViewModel
import android.content.Context
import android.net.Uri
import java.util.concurrent.Callable

import javax.inject.Inject
import javax.inject.Provider
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

    var onDraftDBChanged = SingleLiveEvent<Void>()

    val shotDraft: Maybe<List<Draft>>
        get() = postDao.allPost
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())

    fun getShotDraftByShotId(ShotID: String): Maybe<Draft> {
        return postDao.getShotDraftByShotId(ShotID)
    }


    fun updateShotDraft(shotDraft: Draft): Completable {
        return Completable.fromRunnable { postDao.updateDraft(shotDraft) }
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete {
                    //notifiy With Single event
                    onDraftDBChanged.call()
                }
    }

    fun storeShotDraft(shotDraft: Draft): Completable {
        return Completable.fromRunnable { postDao.insertPost(shotDraft) }
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete {
                    //notifiy With Single event
                    onDraftDBChanged.call()
                }
    }

    fun deleteDraft(id: Int): Completable {
        return Completable.fromRunnable { postDao.deletDraftByID(id) }
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete {
                    //notifiy With Single event
                    onDraftDBChanged.call()
                }
    }

    fun storeImageAndReturnItsUri(imageCroppedFormat: String, croppedFileUri: Uri, context: Context): Single<String> {
        return Single.fromCallable {
            ImageUtils.saveImageAndGetItsFinalUri(imageCroppedFormat, croppedFileUri, context) }
    }
}
