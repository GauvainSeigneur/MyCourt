package seigneur.gauvain.mycourt.data.repository

import com.google.gson.Gson

import javax.inject.Inject
import javax.inject.Singleton

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import seigneur.gauvain.mycourt.data.local.SharedPrefs
import seigneur.gauvain.mycourt.data.local.dao.TokenDao
import seigneur.gauvain.mycourt.data.local.dao.UserDao
import seigneur.gauvain.mycourt.data.model.Token

@Singleton
class TokenRepository @Inject
constructor() {

    @Inject
    lateinit var mTokenDao: TokenDao

    val accessTokenFromDB: Maybe<Token>
        get() = mTokenDao.token
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())

    /**
     * Store token in Database
     * @param token
     * @return
     */
    fun insertToken(token: Token): Completable {
        accessToken = token.accessToken
        return Completable.fromAction { mTokenDao.insertToken(token) }
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
    }

    companion object {
        //Access Token for API request
        var accessToken: String=""
    }

}
