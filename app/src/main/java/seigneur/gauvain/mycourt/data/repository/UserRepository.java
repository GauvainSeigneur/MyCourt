package seigneur.gauvain.mycourt.data.repository;


import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;


import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
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
                .materialize()
                .filter(userNotification -> {
                    return !userNotification.isOnError();
                })
                .dematerialize();
    }

    public Maybe<User> getUserFromDB() {
        Timber.d("getUserFromDB called");
        return mUserDao.getUser()
                .doOnSuccess(new Consumer<User>() {
                              @Override
                              public void accept(User user) throws Exception {
                                  Timber.d("userObservableFromDB accepted: "+ user.getName());
                                  isFetchFromDBSuccess=true;
                              }
                          })
                .subscribeOn(Schedulers.io()); // db operation
    }

    public Single<User> getUserFromAPI(boolean applyResponseCache) {
        Timber.d("getUserFromAPI called");
        return mDribbbleService.getUser()
                .subscribeOn(Schedulers.io())
                .doOnSuccess(new Consumer<User>() {
                    @Override
                    public void accept(User user) throws Exception {
                        Timber.d("userObservableFromAPI: "+user.toString());
                        insertUser(user); //inser user in database
                        isFetchFromAPISuccess=true;
                    }
                });
    }

    //todo - use completable instead Observable
    /*public Completable storeUser(User user) {
        return Completable.fromAction(user->
        );
    }*/

    public void insertUser(final User user) {
        Observable.fromCallable(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                return mUserDao.insertUser(user);
            }
        })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {
                    }

                    @Override
                    public void onNext(@io.reactivex.annotations.NonNull Long o) {
                        Timber.d(o.toString());
                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

}
