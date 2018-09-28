package seigneur.gauvain.mycourt.ui.splash;

import android.arch.lifecycle.ViewModel;

import com.google.gson.Gson;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
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
import seigneur.gauvain.mycourt.utils.SingleLiveEvent;
import timber.log.Timber;


public class SplashViewModel extends ViewModel {

    @Inject
    Gson gson;

    @Inject
    OkHttpClient.Builder okHttpClient;

    @Inject
    TokenRepository mTokenRepository;

    @Inject
    UserRepository mUserRepository;

    private Retrofit authRetrofit;

    private AuthUtils.AuthService mAuthService;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private final SingleLiveEvent<Void> mSignInCommand = new SingleLiveEvent<>();
    private final SingleLiveEvent<Void> mGoToHomeCommand = new SingleLiveEvent<>();

    @Inject
    public SplashViewModel() {}

    @Override
    public void onCleared(){
        super.onCleared();
        compositeDisposable.dispose();
        Timber.d("viewmodel cleared");
    }


    public void init() {
        checkIfUserIsLoggedIn();
    }


    public SingleLiveEvent<Void> getSignInCommand() {
        return mSignInCommand;
    }

    public SingleLiveEvent<Void> getGoToHomeCommand() {
        return mGoToHomeCommand;
    }



    public void onSignInSuccess(String authCode) {
        fetchToken(authCode);
    }

    public void onSignInClicked() {
        mSignInCommand.call();
    }

    /**
     * User has been successfully saved in DB, go to home
     */
    private void onUserSaved() {
        mGoToHomeCommand.call();
    }

    /**
     * Check if user is LoggedIn : check if a token is present in DB
     */
    private void checkIfUserIsLoggedIn() {
        compositeDisposable.add(mTokenRepository.getAccessTokenFromDB()
                .subscribe(
                        token -> {
                                Timber.d("token found");
                                TokenRepository.accessToken = String.valueOf(token.getAccessToken());
                                mGoToHomeCommand.call();
                                //mSplashView.goToHome(); //todo - replace by single  event
                        },
                        t-> {
                            Timber.d(" error happened");
                            initAuthRetrofit();
                        },
                        () -> {
                            Timber.d(" no token found");
                            initAuthRetrofit();
                            //no token found

                        }
                )
        );
    }

    /**
     * Build Retrofit for authentication
     * because it use a different URL that the base url, I have to build this one
     * and not use the @NetworkModule injection
     */
    private void initAuthRetrofit() {
        //todo find a way to change base url in this case in order to not instantiate another retrofit
        authRetrofit = new Retrofit.Builder()
                .baseUrl(AuthUtils.URI_TOKEN_RETROFIT)
                .client(okHttpClient.build())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        mAuthService = authRetrofit.create(AuthUtils.AuthService.class);
    }


    /**
     * fetch Token from Dribbble after a successful authentication
     * @param authCode - authCode received from intent from WebView - See activity
     */
    private void fetchToken(String authCode) {
        compositeDisposable.add(
                mAuthService.getToken(
                        AuthUtils.CLIENT_ID,
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

    /**
     * After fetch token get the user in order to register it
     */
    private void getUserAndStoreIt() {
        Timber.d("get user called");
        compositeDisposable.add(
                mUserRepository.getUserFromAPI(false)
                        .subscribe(
                                this::saveUser,
                                error -> {
                                    Timber.e(error);
                                }
                        )
        );
    }

    /**
     * Save user in DB
     * @param user - User fetched
     */
    private void saveUser(User user) {
        compositeDisposable.add(mUserRepository.insertUser(user)
                .subscribe(
                        this::onUserSaved,
                        this::onUserSavingError
                )
        );
    }

    /**
     * An error happened during DB ope for save user
     * @param t - Throwable
     */
    private void onUserSavingError(Throwable t) {
        Timber.d(t);
    }

    /**
     * Insert Token in DB in order to use it for future request
     * @param token - token fetch after successful auth
     */
    private void insertToken(Token token) {
        compositeDisposable.add(mTokenRepository.insertToken(token)
                .subscribe(
                        ()-> {
                            Timber.d("insert completed");
                        },
                        t -> {
                            Timber.d(t);
                        }
                )
        );
    }






}
