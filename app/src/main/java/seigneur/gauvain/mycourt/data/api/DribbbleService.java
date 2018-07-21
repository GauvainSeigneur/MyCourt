package seigneur.gauvain.mycourt.data.api;

import java.util.List;
import io.reactivex.Flowable;
import io.reactivex.Single;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
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
            @Query("?page=") int page
    );

    @GET("user/projects")
    Flowable<List<Project>> getUserProjects();

    @GET("user")
    Single<User> getUser();

    @PUT("shots/{id}")
    @FormUrlEncoded
    Single<Shot> updateShot(
            @Path(value = "id", encoded = true) String id,
            @Field("description") String description,
            @Field("low_profile") boolean isLowProfile,
            //@Field("scheduled_for") Date publishDate, //todo : to manage it for phase 2
            @Field("tags") String[] tags,
            // @Field("teamID") int teamID //todo : to mange it for phase 2
            @Field("title") String title
    );

    //todo : check this : https://medium.com/@adinugroho/upload-image-from-android-app-using-retrofit-2-ae6f922b184c
    @Multipart
    @POST("shots")
    Call<ResponseBody> postShot(
            @Part MultipartBody.Part image,
            @Part("title") RequestBody title
           // @Part("description") RequestBody description,
            //@Part("low_profile") RequestBody isLowProfile
    );

    //https://code.tutsplus.com/tutorials/sending-data-with-retrofit-2-http-client-for-android--cms-27845
    @POST("shots")
    @FormUrlEncoded
    Call<Project> postProject(
            @Field("title") String title,
            @Field("body") String body,
            @Field("userId") long userId
    );

}

