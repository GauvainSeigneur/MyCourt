package seigneur.gauvain.mycourt.data.api;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import io.reactivex.Single;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import seigneur.gauvain.mycourt.data.model.Token;
import seigneur.gauvain.mycourt.ui.AuthActivity;

public class AuthUtils {

    public static final int REQ_CODE = 100;
    private static final String KEY_CODE = "code";
    private static final String KEY_CLIENT_ID = "client_id";
    private static final String KEY_CLIENT_SECRET = "client_secret";
    private static final String KEY_REDIRECT_URI = "redirect_uri";
    private static final String KEY_SCOPE = "scope";
    public static final String CLIENT_ID =  DribbbleClient.CLIENT_ID;
    public static final String CLIENT_SECRET = DribbbleClient.CLIENT_SECRET;
    // see http://developer.dribbble.com/v2/oauth/#scopes
    private static final String SCOPE = "public+upload";
    private static final String URI_AUTHORIZE = "https://dribbble.com/oauth/authorize";
    //private static final String URI_TOKEN = "https://dribbble.com/oauth/token";
    public static final String URI_TOKEN_RETROFIT = "https://dribbble.com/oauth/token/";
    public static final String REDIRECT_URI = "https://mycourt.com/path"; //todo change it
    //public static final String REDIRECT_URI = "https://mycourt.com/path";

    public static void openAuthActivity(@NonNull Activity activity) {
        Intent intent = new Intent(activity, AuthActivity.class);
        intent.putExtra(AuthActivity.KEY_URL, getAuthorizeUrl());
        activity.startActivityForResult(intent, REQ_CODE);
    }

    private static String getAuthorizeUrl() {
        String url = Uri.parse(URI_AUTHORIZE)
                .buildUpon()
                .appendQueryParameter(KEY_CLIENT_ID, CLIENT_ID)
                .build()
                .toString();
        // fix encode issue
        url += "&" + KEY_REDIRECT_URI + "=" + REDIRECT_URI;
        url += "&" + KEY_SCOPE + "=" + SCOPE;
        return url;
    }

    public interface AuthService {
        @POST(".")
        @FormUrlEncoded
        Single<Token> getToken(
                @Field(KEY_CLIENT_ID) String clientID,
                @Field(KEY_CLIENT_SECRET) String clientSecret,
                @Field(KEY_CODE) String keyCode,
                @Field(KEY_REDIRECT_URI) String redirectURI
        );
    }

}
