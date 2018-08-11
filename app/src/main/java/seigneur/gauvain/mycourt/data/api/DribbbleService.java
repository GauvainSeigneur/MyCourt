package seigneur.gauvain.mycourt.data.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Path;
import retrofit2.http.Query;
import seigneur.gauvain.mycourt.data.model.Project;
import seigneur.gauvain.mycourt.data.model.Shot;
import seigneur.gauvain.mycourt.data.model.User;
import static seigneur.gauvain.mycourt.utils.Constants.RESPONSE_CACHE_DELAY;

/**
 * Created by Gauvain on 25/02/2018.
 */
public interface DribbbleService {

    @GET("user/shots")
    Flowable<List<Shot>> getShots(
            @Header(RESPONSE_CACHE_DELAY) int responseCacheDelay,
            @Query("page") int page,
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
            @Field("description") String description,
            @Field("low_profile") boolean isLowProfile,
            //@Field("scheduled_for") Date publishDate, //todo : to manage it for phase 2
            @Field("tags[]") ArrayList<String> tags,
            // @Field("teamID") int teamID //todo : to mange it for phase 2
            @Field("title") String title
    );

    @Multipart
    @POST("shots")
    Observable<Response<Shot>> publishANewShot(
            @PartMap() Map<String, RequestBody> partMap, //See : https://stackoverflow.com/a/40873297
            @Part MultipartBody.Part file,
            @Part("title") String title,
            @Part("description") String description,
            @Part("items[]") List<String> tags
    );


    @POST("shots")
    @FormUrlEncoded
    Call<Project> postProject(
            @Field("title") String title,
            @Field("body") String body,
            @Field("userId") long userId
    );

}

