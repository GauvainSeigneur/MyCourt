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

    /*
    *********************************************************************************************
    * GET SHOT
    ********************************************************************************************/
    //get list of Shot from Dribbble
    fun getShots(applyResponseCache: Int, page: Long, perPage: Int): Flowable<List<Shot>> {
        return mDribbbleService.getShotAPI(applyResponseCache, page, perPage)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    /*
    *********************************************************************************************
    * UPDATE A SHOT
    ********************************************************************************************/
    /**
     * update a published shot
     */
    fun updateShot(draft: Draft, profile: Boolean): Observable<Response<Void>>{
        return mDribbbleService.updateShot(
                draft.shot.id!!,
                draft.shot.title!!,
                draft.shot.description!!,
                draft.shot.tagList!!,
                profile)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun updateShotAndPostAttachment(draft: Draft, profile: Boolean, context: Context): Observable<Response<Void>>{
        val shotId = draft.shot.id
        return mDribbbleService.updateShot(
                draft.shot.id!!,
                draft.shot.title!!,
                draft.shot.description!!,
                draft.shot.tagList!!,
                profile)
                .flatMap { _ ->
                    postAttachment(context, draft, shotId!!)
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())

    }

    fun updateShotAndDeleteAttachment(draft: Draft, profile: Boolean,
                                      lisAttachmentToDelete: List<Attachment>): Observable<Response<Void>> {
        return mDribbbleService.updateShot(
                draft.shot.id!!,
                draft.shot.title!!,
                draft.shot.description!!,
                draft.shot.tagList!!,
                profile)
                .flatMap { _ ->
                    deleteAttachment(draft, lisAttachmentToDelete)
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())

    }

    /**
     * WOW  - so much operations
     */
    fun updateShotAndUploadAndDeleteAttachment(draft: Draft, profile: Boolean, context: Context,
                                      lisAttachmentToDelete: List<Attachment>): Observable<Response<Void>> {
        val shotId = draft.shot.id
        return mDribbbleService.updateShot(
                draft.shot.id!!,
                draft.shot.title!!,
                draft.shot.description!!,
                draft.shot.tagList!!,
                profile)
                .flatMap { _ ->
                    postAndDeleteAttachment(context, draft, shotId!!,  lisAttachmentToDelete)
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    /*
    *********************************************************************************************
    * PUBLISH SHOT
    ********************************************************************************************/
    /**
     * create a shot on dribbble
     */
    fun publishANewShot(draft: Draft, context: Context): Observable<Response<Void>> {
        //Create bodyPart for posting image in MultiPart
        val body = HttpUtils.createShotFilPart(
                context,
                draft.croppedImgDimen!!,
                Uri.parse(draft.imageUri),
                draft.imageFormat, "image")
        //add to HashMap key and RequestBody
        val map = HashMap<String, RequestBody>()
        return mDribbbleService.publishANewShot(
                map,
                body,
                draft.shot.title,
                draft.shot.description,
                draft.shot.tagList)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    /**
     * Concat Post a shot and POST attachment  operation
     *
     * Dribbble's API doesn't allow to publish a shot with attachments.
     * We can only provide attachments to an existing shot
     * Also, we can send only one attachment per POST.
     * So for first publication with attachment, we must concat it in two request
     */
    fun postShotAndAttachment(draft: Draft, context: Context): Observable<Response<Void>> {
        //Create bodyPart for posting image in MultiPart
        val body = HttpUtils.createShotFilPart(
                context,
                draft.croppedImgDimen!!,
                Uri.parse(draft.imageUri),
                draft.imageFormat, "image")
        //add to HashMap key and RequestBody
        val map = HashMap<String, RequestBody>()
        return  mDribbbleService.publishANewShot(
                map,
                body,
                draft.shot.title,
                draft.shot.description,
                draft.shot.tagList)
                .flatMap {it ->
                    //get the id of the published shot from response header
                    val headers = it.headers()
                    val location = headers.get("location")
                    location?.let {
                        val shotId:String
                        val locationTrunkAfter= location.substringAfterLast("/",location ) //todo - this variable must be defined into Firebase for fast update
                        shotId=locationTrunkAfter.substringBefore("-",locationTrunkAfter) //todo - this variable must be defined into Firebase for fast update
                        //perform post attachment
                        postAttachment(context, draft, shotId)
                    }

                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    /*
     *********************************************************************************************
     * ATTACHMENT OPERATION
     ********************************************************************************************/
    /**
     * Concat Post and delete operation
     */
    private fun postAndDeleteAttachment(context: Context,
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
                /*.materialize()
                .filter { concatNotif -> !concatNotif.isOnError }
                .dematerialize()*/
    }

    /**
     * Post several attachment
     */
    private fun postAttachment(context: Context, draft: Draft, shotId: String): Observable<Response<Void>> {
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

    /**
     * delete several attachment
     */
    private fun deleteAttachment(draft: Draft, lisAttachmentToDelete: List<Attachment>): Observable<Response<Void>> {
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
