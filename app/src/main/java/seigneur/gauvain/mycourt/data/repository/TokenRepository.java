package seigneur.gauvain.mycourt.data.repository;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.Gson;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Maybe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import seigneur.gauvain.mycourt.data.local.SharedPrefs;
import seigneur.gauvain.mycourt.data.local.dao.TokenDao;
import seigneur.gauvain.mycourt.data.local.dao.UserDao;
import seigneur.gauvain.mycourt.data.model.Token;

@Singleton
public class TokenRepository {

    /*@Inject
    SharedPrefs sharedPrefs;*/

    @Inject
    TokenDao mTokenDao;

    //Access Token for API request
    public static String accessToken;

    @Inject
    public TokenRepository(){}

    public long insertToken(@NonNull Token token)  {
        accessToken = String.valueOf(token.getAccessToken());
        return mTokenDao.insertToken(token);
    }

    public Maybe<Token> getAccessTokenFromDB() {
        return mTokenDao.getToken()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread());
    }
    /************************************
     * Manage Access token
     * Saved in sharedPrefs
     ***********************************/
    /*public void saveAccessToken(@NonNull String token)  {
        accessToken = token;
        storeAccessToken(accessToken);
    }

    public void checkAccessToken() {
        accessToken = loadAccessToken();
    }

    public void storeAccessToken(@Nullable String token) {
        sharedPrefs.putString(SharedPrefs.kAccessToken, token);
    }

    public String loadAccessToken() {
        return sharedPrefs.getString(SharedPrefs.kAccessToken);
    }
    */
}
