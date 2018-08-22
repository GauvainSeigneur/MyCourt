package seigneur.gauvain.mycourt.ui.splash.presenter;

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

    private AuthUtils.AuthService mAuthService;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Inject
    public SplashPresenterImpl() {
    }

    @Override
    public void onAttach() {
        checkIfUserIsLoggedIn();
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

    /**
     * Check if user is LoggedIn : check if a token is present in DB
     * Todo - check api if we need user too to perfrom request
     */
    private void checkIfUserIsLoggedIn() {
        compositeDisposable.add(mTokenRepository.getAccessTokenFromDB()
                .subscribe(
                        token -> {
                            if (mSplashView!=null) {
                                Timber.tag("jonh").d("token found");
                                TokenRepository.accessToken = String.valueOf(token.getAccessToken());
                                mSplashView.goToHome();
                            }
                          },
                        t-> {
                            Timber.tag("jonh").d(" error happened");
                            initAuthRetrofit();
                      },
                        () -> {
                            Timber.tag("jonh").d(" no token found");
                            initAuthRetrofit();
                            //no token found - todo - may be we need to  perform another authentication!

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
                mAuthService.getToken(AuthUtils.CLIENT_ID,
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
                            //todo make a better management of errors
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
       compositeDisposable.add(
               mUserRepository.insertUser(user)
               .subscribe(
                       this::onUserSaved,
                       this::onUserSavingError
               )
       );
    }

    /**
     * User has been successfully saved in DB, go to home
     */
    private void onUserSaved() {
        mSplashView.goToHome();
    }

    /**
     * An error happened during DB ope for save user
     * @param t - Throwable
     */
    private void onUserSavingError(Throwable t) {
        //todo - keep a ref of this error somewhere to retry to get user after
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
