package seigneur.gauvain.mycourt.data.api

import java.util.ArrayList

import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.PartMap
import retrofit2.http.Path
import retrofit2.http.Query
import seigneur.gauvain.mycourt.data.model.Project
import seigneur.gauvain.mycourt.data.model.Shot
import seigneur.gauvain.mycourt.data.model.User
import seigneur.gauvain.mycourt.utils.Constants.RESPONSE_CACHE_DELAY

/**
 * Created by Gauvain on 25/02/2018.
 */
interface DribbbleService {

    @get:GET("user/projects")
    val userProjects: Flowable<List<Project>>

    @get:GET("user")
    val user: Single<User>

    @GET("user/shots")
    fun getShots(
            @Header(RESPONSE_CACHE_DELAY) responseCacheDelay: Int,
            @Query("page") page: Int,
            @Query("per_page") pagePage: Int
    ): Flowable<List<Shot>>

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
            @PartMap partMap: Map<String, RequestBody>, //See : https://stackoverflow.com/a/40873297
            @Part file: MultipartBody.Part,
            @Part("title") title: String,
            @Part("description") description: String,
            @Part("tags[]") tags: List<String>
    ): Observable<Response<Void>>


    @POST("shots")
    @FormUrlEncoded
    fun postProject(
            @Field("title") title: String,
            @Field("body") body: String,
            @Field("userId") userId: Long
    ): Call<Project>

}

/*
public interface DribbbleService {

    @GET("user/shots")
    Flowable<List<Shot>> getShots(
    @Header(RESPONSE_CACHE_DELAY) int responseCacheDelay,
    @Query("page") int page,
    @Query("per_page") int pagePage
    );

    @GET("user/shots")
    Flowable<List<Shot>> getShotAPI(
    @Header(RESPONSE_CACHE_DELAY) int responseCacheDelay,
    @Query("page") long page,
    @Query("per_page") int pagePage
    );

    @GET("user/projects")
    Flowable<List<Project>> getUserProjects();

    @GET("user")
    Single<User> getUser();

    @FormUrlEncoded
    @PUT("shots/{id}")
    Single<Shot> updateShot(
    @Path(value = "id", encoded = true) String id,
    @Field("title") String title,
    @Field("description") String description,
    @Field("tags[]") ArrayList<String> tags,
    @Field("low_profile") boolean isLowProfile
    //@Field("scheduled_for") Date publishDate, //todo : to manage it for phase 2
    // @Field("teamID") int teamID //todo : to mange it for phase 2
    );

    @Multipart
    @POST("shots")
    Observable<Response<Void>> publishANewShot(
    @PartMap() Map<String, RequestBody> partMap, //See : https://stackoverflow.com/a/40873297
    @Part MultipartBody.Part file,
    @Part("title") String title,
    @Part("description") String description,
    @Part("tags[]") List<String> tags
    );


    @POST("shots")
    @FormUrlEncoded
    Call<Project> postProject(
    @Field("title") String title,
    @Field("body") String body,
    @Field("userId") long userId
    );

}
*/
