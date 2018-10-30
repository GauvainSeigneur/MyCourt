package seigneur.gauvain.mycourt.data.api

import android.app.Activity
import android.content.Intent
import android.net.Uri
import io.reactivex.Single
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import seigneur.gauvain.mycourt.data.model.Token
import seigneur.gauvain.mycourt.ui.AuthActivity

class AuthUtils {

    interface AuthService {
        @POST(".")
        @FormUrlEncoded
        fun getToken(
                @Field(KEY_CLIENT_ID) clientID: String,
                @Field(KEY_CLIENT_SECRET) clientSecret: String,
                @Field(KEY_CODE) keyCode: String,
                @Field(KEY_REDIRECT_URI) redirectURI: String
        ): Single<Token>
    }

    companion object {
        //Constants
        const val KEY_CODE = "code"
        const val KEY_CLIENT_ID = "client_id"
        const val KEY_CLIENT_SECRET = "client_secret"
        const val KEY_REDIRECT_URI = "redirect_uri"

        const val CLIENT_ID = DribbbleClient.CLIENT_ID
        const val CLIENT_SECRET = DribbbleClient.CLIENT_SECRET
        const val REDIRECT_URI = "https://mycourt.com/path" //todo change it
        const val URI_TOKEN_RETROFIT = "https://dribbble.com/oauth/token/"
        const val REQ_CODE = 100

        private const val KEY_SCOPE = "scope"
        // see http://developer.dribbble.com/v2/oauth/#scopes
        private const val SCOPE = "public+upload"
        private const val URI_AUTHORIZE = "https://dribbble.com/oauth/authorize"

        // fix encode issue
        private val authorizeUrl: String
            get() {
                var url = Uri.parse(URI_AUTHORIZE)
                        .buildUpon()
                        .appendQueryParameter(KEY_CLIENT_ID, CLIENT_ID)
                        .build()
                        .toString()
                url += "&$KEY_REDIRECT_URI=$REDIRECT_URI"
                url += "&$KEY_SCOPE=$SCOPE"
                return url
            }

        fun openAuthActivity(activity: Activity) {
            val intent = Intent(activity, AuthActivity::class.java)
            intent.putExtra(AuthActivity.KEY_URL, authorizeUrl)
            activity.startActivityForResult(intent, REQ_CODE)
        }
    }

}
