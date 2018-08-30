package seigneur.gauvain.mycourt.data.repository;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;


import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import seigneur.gauvain.mycourt.data.api.DribbbleService;
import seigneur.gauvain.mycourt.data.local.dao.UserDao;
import seigneur.gauvain.mycourt.data.model.User;
import timber.log.Timber;

@Singleton
public class UserRepository {

    @Inject
    UserDao mUserDao;

    @Inject
    DribbbleService mDribbbleService;

    //used to manage UI -- see presenter
    public boolean isFetchFromDBSuccess=false;
    public boolean isFetchFromAPISuccess=false;

    @Inject
    public UserRepository(){}

    public Observable<User> getUser(boolean applyResponseCache) {
        return Observable.concat(
                getUserFromDB()
                        .toObservable()
                        .debounce(400, TimeUnit.MILLISECONDS),
                getUserFromAPI(applyResponseCache).
                        toObservable()
        )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .materialize()
                .filter(userNotification -> {
                    return !userNotification.isOnError();
                })
                .dematerialize();
    }

    public Completable updateUserPWD(String pwd) {
        return Completable.fromAction(() ->
                mUserDao.updateCryptedPwd(pwd)
        )
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Maybe<User> getUserFromDB() {
        Timber.d("getUserFromDB called");
        return mUserDao.getUser()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess(user -> {
                    isFetchFromDBSuccess = true;
                });
    }

    public Single<User> getUserFromAPI(boolean applyResponseCache) {
        Timber.d("getUserFromAPI called");
        return mDribbbleService.getUser()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess(user -> {
                    isFetchFromAPISuccess=true;
                });
    }

    public Completable insertUser(User user) {
        return Completable.fromAction(() ->
                mUserDao.insertUser(user)
        )
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread());
    }


}
