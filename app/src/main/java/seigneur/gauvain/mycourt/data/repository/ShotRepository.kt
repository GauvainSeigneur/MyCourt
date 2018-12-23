package seigneur.gauvain.mycourt.data.repository

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
import seigneur.gauvain.mycourt.data.model.Shot

class ShotRepository @Inject
constructor() {

    @Inject
    lateinit var mDribbbleService: DribbbleService

    //get list of Shot from Dribbble
    fun getShotsFromAPI(applyResponseCache: Int, page: Int, perPage: Int): Flowable<List<Shot>> {
        return mDribbbleService.getShots(applyResponseCache, page, perPage)
    }

    //get list of Shot from Dribbble
    fun getShotsFromAPItest(applyResponseCache: Int, page: Long, perPage: Int): Flowable<List<Shot>> {
        return mDribbbleService.getShotAPI(applyResponseCache, page, perPage)
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
            title: String,
            description: String?,
            tags: ArrayList<String>?): Observable<Response<Void>> {
        return mDribbbleService.publishANewShot(map, file, title, description, tags)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun addAttachment(id: String,
                      file: MultipartBody.Part): Observable<Response<Void>> {
        return mDribbbleService.addAttachment(id, file)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

}
