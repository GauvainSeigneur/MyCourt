package seigneur.gauvain.mycourt.data.repository

import android.content.Context
import android.net.Uri
import java.util.ArrayList
import java.util.HashMap

import javax.inject.Inject

import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import seigneur.gauvain.mycourt.data.api.DribbbleService
import seigneur.gauvain.mycourt.data.model.Attachment
import seigneur.gauvain.mycourt.data.model.Draft
import seigneur.gauvain.mycourt.data.model.Shot
import seigneur.gauvain.mycourt.utils.HttpUtils
import timber.log.Timber

class ShotRepository @Inject
constructor() {

    @Inject
    lateinit var mDribbbleService: DribbbleService

    //get list of Shot from Dribbble
    fun getShots(applyResponseCache: Int, page: Long, perPage: Int): Flowable<List<Shot>> {
        return mDribbbleService.getShotAPI(applyResponseCache, page, perPage)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    //send an update to Dribbble
    fun updateShot(id: String,
                   title: String,
                   description: String,
                   tags: ArrayList<String>,
                   isLowProfile: Boolean
    ): Single<Shot> {
        return mDribbbleService.updateShot(id, title, description, tags, isLowProfile)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun publishANewShot(
            map: HashMap<String, RequestBody>,
            file: MultipartBody.Part,
            title: String?,
            description: String?,
            tags: ArrayList<String>?): Observable<Response<Void>> {
        return mDribbbleService.publishANewShot(map, file, title, description, tags)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun postAndDeleteAttachment(context: Context,
                                draft: Draft,
                                shotId: String,
                                lisAttachmentToDelete: List<Attachment>)
            : Observable<Response<Void>> {
        return Observable.concat(
                //1) first observable
                postAttachment(context,draft, shotId),
                //2) second observable
                deleteAttachment(draft,lisAttachmentToDelete)
        )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .materialize()
                .filter { concatNotif -> !concatNotif.isOnError }
                .dematerialize()



    }

    fun postAttachment(context: Context, draft: Draft, shotId: String): Observable<Response<Void>> {
        return Observable.just(draft.shot.attachment) //we create an Observable that emits a single array
                .flatMapIterable {it} //map the list to an Observable that emits every item as an observable
                .filter {it -> it.id==-1L } //send only item in the list which ids is -1L
                .flatMap {it -> //perform following operation on every item
                    val body = HttpUtils.createAttachmentFilePart(
                            context,
                            Uri.parse(it.uri),
                            it.contentType,
                            "file")
                    mDribbbleService.addAttachment(
                            shotId,
                            body)
                            .doOnNext{
                                response -> Timber.d("add attachment($it.id) next: $response")
                            }
                            .doOnError {
                                error -> Timber.d("add attachment error: $error")
                            }
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun deleteAttachment(draft: Draft, lisAttachmentToDelete: List<Attachment>): Observable<Response<Void>> {
        return Observable.just(lisAttachmentToDelete) //we create an Observable that emits a single array
                .flatMapIterable {it} //map the list to an Observable that emits every item as an observable    .filter {it -> it.id!=-1L } //send only item in the list which ids is -1L
                .flatMap { it ->
                    mDribbbleService.deleteAttachment(draft.shot.id!!, it.id)
                            .doOnNext {
                                resp -> Timber.d("delete attachment($it.id) next: $resp")
                            }
                            .doOnError {
                                error -> Timber.d("delete attachment($it.id) error: $error")
                            }
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }
}
