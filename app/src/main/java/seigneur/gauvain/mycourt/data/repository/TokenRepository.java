package seigneur.gauvain.mycourt.data.repository;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.Gson;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import seigneur.gauvain.mycourt.data.local.SharedPrefs;
import seigneur.gauvain.mycourt.data.local.dao.TokenDao;
import seigneur.gauvain.mycourt.data.local.dao.UserDao;
import seigneur.gauvain.mycourt.data.model.Token;

@Singleton
public class TokenRepository {

    @Inject
    TokenDao mTokenDao;

    //Access Token for API request
    public static String accessToken;

    @Inject
    public TokenRepository(){}

    /**
     * TODO - use cyphering to store a crypted version of the token...
     * @param token
     * @return
     */
    public Completable insertToken(@NonNull Token token)  {
        accessToken = String.valueOf(token.getAccessToken());
        return Completable.fromAction(() ->
                mTokenDao.insertToken(token)
        )
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Maybe<Token> getAccessTokenFromDB() {
        return mTokenDao.getToken()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread());
    }

}
