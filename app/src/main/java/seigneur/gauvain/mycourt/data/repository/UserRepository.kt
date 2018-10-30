package seigneur.gauvain.mycourt.data.repository

import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import seigneur.gauvain.mycourt.data.api.DribbbleService
import seigneur.gauvain.mycourt.data.local.dao.PinDao
import seigneur.gauvain.mycourt.data.local.dao.UserDao
import seigneur.gauvain.mycourt.data.model.Pin
import seigneur.gauvain.mycourt.data.model.User
import timber.log.Timber

@Singleton
class UserRepository @Inject
constructor() {

    @Inject
    lateinit var mUserDao: UserDao

    @Inject
    lateinit var mPinDao: PinDao

    @Inject
    lateinit var mDribbbleService: DribbbleService

    //used to manage UI -- see presenter
    var isFetchFromDBSuccess = false
    var isFetchFromAPISuccess = false

    val userFromDB: Maybe<User>
        get() {
            Timber.d("getUserFromDB called")
            return mUserDao.user
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSuccess { _ -> isFetchFromDBSuccess = true }
        }

    val pin: Maybe<Pin>
        get() = mPinDao.pin
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())

    fun getUser(applyResponseCache: Boolean): Observable<User> {
        return Observable.concat(
                userFromDB
                        .toObservable()
                        .debounce(400, TimeUnit.MILLISECONDS),
                getUserFromAPI(applyResponseCache).toObservable()
        )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .materialize()
                .filter { userNotification -> !userNotification.isOnError }
                .dematerialize()
    }

    fun insertPin(pin: Pin): Completable {
        return Completable.fromAction { mPinDao.insertPIN(pin) }
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun updateUserPWD(pwd: String): Completable {
        return Completable.fromAction { mPinDao.updateCryptedPwd(pwd) }
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun getUserFromAPI(applyResponseCache: Boolean): Single<User> {
        Timber.d("getUserFromAPI called")
        return mDribbbleService.user
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess { _ ->
                    isFetchFromAPISuccess = true
                    //insertUser(user);
                }
    }

    fun insertUser(user: User): Completable {
        return Completable.fromAction { mUserDao.insertUser(user) }
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
    }


}
