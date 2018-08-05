package seigneur.gauvain.mycourt.ui.splash.presenter;

import com.google.gson.Gson;

import java.util.Collections;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import seigneur.gauvain.mycourt.data.api.AuthUtils;
import seigneur.gauvain.mycourt.data.model.Token;
import seigneur.gauvain.mycourt.data.model.User;
import seigneur.gauvain.mycourt.data.repository.TokenRepository;
import seigneur.gauvain.mycourt.data.repository.UserRepository;
import seigneur.gauvain.mycourt.di.scope.PerActivity;
import seigneur.gauvain.mycourt.ui.splash.view.SplashView;
import timber.log.Timber;



@PerActivity
public class SplashPresenterImpl implements SplashPresenter {

    @Inject
    SplashView mSplashView;

    @Inject
    Gson gson;

    @Inject
    OkHttpClient.Builder okHttpClient;

    @Inject
    TokenRepository mTokenRepository;

    @Inject
    UserRepository mUserRepository;

    private Retrofit authRetrofit;

    private AuthUtils.AuthService authService;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Inject
    public SplashPresenterImpl() {
    }

    @Override
    public void onAttach() {
        checkIfUserIsLoggedIn();
        initAuthRetrofit();
    }

    @Override
    public void onDetach() {
        compositeDisposable.dispose();
        mSplashView=null;
    }

    @Override
    public void onSignInClicked() {
        if(mSplashView!=null) {
            mSplashView.goToAuthActivity();
        }
    }

    @Override
    public void onSignInSuccess(String authCode) {
        fetchToken(authCode);
    }

    private void checkIfUserIsLoggedIn() {
        compositeDisposable.add(mTokenRepository.getAccessTokenFromDB()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                      token -> {
                          if (mSplashView!=null) {
                              Timber.d("token found");
                              TokenRepository.accessToken = String.valueOf(token.getAccessToken());
                              mSplashView.goToHome();
                          }
                      },
                      c-> {
                          Timber.d("no token found");
                          //no access token
                          //todo - do something
                      }
                )
        );
    }

    private void fetchToken(String authCode) {
        compositeDisposable.add(
                authService.getToken(AuthUtils.CLIENT_ID,
                        AuthUtils.CLIENT_SECRET,
                        authCode,
                        AuthUtils.REDIRECT_URI)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                token -> {
                                    Timber.d("token fetched");
                                    insertToken(token);
                                    getUserAndStoreIt();
                                },
                                error -> {
                                    //todo - better management
                                    RxJavaPlugins.setErrorHandler(e -> {
                                        Timber.d(e);
                                    });
                                }
                        )
        );
    }

    private void getUserAndStoreIt() {
        Timber.d("get user called");
        compositeDisposable.add(
                mUserRepository.getUserFromAPI(false)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        user -> {
                            mUserRepository.insertUser(user);
                            mSplashView.goToHome();
                        },
                        error -> {
                            //todo make a better management of errors
                            Timber.e(error);
                        }
                )
        );
    }

    private void initAuthRetrofit() {
        //todo find a way to change base url in this case in order to not instantiate another retrofit
        authRetrofit = new Retrofit.Builder()
                .baseUrl(AuthUtils.URI_TOKEN_RETROFIT)
                .client(okHttpClient.build())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        authService = authRetrofit.create(AuthUtils.AuthService.class);
    }

    private void insertToken(Token token) {
        Timber.d("insertToken called");
        compositeDisposable.add(
                Observable.fromCallable(() ->{
                    return mTokenRepository.insertToken(token);
                })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                result -> {
                                    Timber.d(result.toString());
                                },
                                error -> {
                                    //todo make a better management of errors
                                    Timber.e(error);
                                }
                        )
        );
    }

}
