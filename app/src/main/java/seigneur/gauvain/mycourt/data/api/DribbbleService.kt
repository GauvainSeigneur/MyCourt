package seigneur.gauvain.mycourt.data.api

import java.util.ArrayList

import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*
import seigneur.gauvain.mycourt.data.model.Project
import seigneur.gauvain.mycourt.data.model.Shot
import seigneur.gauvain.mycourt.data.model.User
import seigneur.gauvain.mycourt.utils.Constants.RESPONSE_CACHE_DELAY

/**
 * Created by Gauvain on 25/02/2018.
 */
interface DribbbleService {

    /*
    *********************************************************************************************
    * USER
    *********************************************************************************************/
    @get:GET("user/projects")
    val userProjects: Flowable<List<Project>>

    @get:GET("user")
    val user: Single<User>

    /*
    *********************************************************************************************
    * SHOT
    *********************************************************************************************/
    @GET("user/shots")
    fun getShotAPI(
            @Header(RESPONSE_CACHE_DELAY) responseCacheDelay: Int,
            @Query("page") page: Long,
            @Query("per_page") pagePage: Int
    ): Flowable<List<Shot>>

    @FormUrlEncoded
    @PUT("shots/{id}")
    fun updateShot(
            @Path(value = "id", encoded = true) id: String,
            @Field("title") title: String,
            @Field("description") description: String,
            @Field("tags[]") tags: ArrayList<String>,
            @Field("low_profile") isLowProfile: Boolean
            //@Field("scheduled_for") Date publishDate, //todo : to manage it for phase 2
            // @Field("teamID") int teamID //todo : to mange it for phase 2
    ): Single<Shot>

    @Multipart
    @POST("shots")
    fun publishANewShot(
            @PartMap partMap: Map<String, @JvmSuppressWildcards RequestBody>, //See : https://stackoverflow.com/a/40873297
            @Part file: MultipartBody.Part,
            @Part("title") title: String?,
            @Part("description") description: String?,
            @Part("tags[]") tags: List<String>?
    ): Observable<Response<Void>>

    /*
    *********************************************************************************************
    * ATTACHMENT
    *********************************************************************************************/
    @Multipart
    @POST("shots/{id}/attachments")
    fun addAttachment(
            //if it doesn't work, use the same method og PublishAnewShot : create a hashMap of Body Part
            @Path(value = "id", encoded = true) id: String,
            @Part file: MultipartBody.Part
    ): Observable<Response<Void>>

    @DELETE("shots/{shot}/attachments/{id}")
    fun deleteAttachment(
            @Path(value = "shot", encoded = true) shot: String,
            @Path(value = "id", encoded = true) id: Long
    ): Observable<Response<Void>>

    /*
    *********************************************************************************************
    * PROJECT
    *********************************************************************************************/
    @POST("shots")
    @FormUrlEncoded
    fun postProject(
            @Field("title") title: String,
            @Field("body") body: String,
            @Field("userId") userId: Long
    ): Call<Project>

}
