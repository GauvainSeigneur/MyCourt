package seigneur.gauvain.mycourt.ui.splash

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

import com.google.gson.Gson

import javax.inject.Inject

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import seigneur.gauvain.mycourt.data.api.AuthUtils
import seigneur.gauvain.mycourt.data.model.Shot
import seigneur.gauvain.mycourt.data.model.Token
import seigneur.gauvain.mycourt.data.model.User
import seigneur.gauvain.mycourt.data.repository.TokenRepository
import seigneur.gauvain.mycourt.data.repository.UserRepository
import seigneur.gauvain.mycourt.utils.SingleLiveEvent
import timber.log.Timber


class SplashViewModel @Inject
constructor() : ViewModel() {

    @Inject
    lateinit var gson: Gson

    @Inject
    lateinit var okHttpClient: OkHttpClient.Builder

    @Inject
    lateinit var mTokenRepository: TokenRepository

    @Inject
    lateinit var mUserRepository: UserRepository

    private lateinit var authRetrofit: Retrofit

    private lateinit var mAuthService: AuthUtils.AuthService

    private val compositeDisposable = CompositeDisposable()

    val signInCommand = SingleLiveEvent<Void>()
    val goToHomeCommand = SingleLiveEvent<Void>()
    private val mConnected = MutableLiveData<Boolean>()

    public override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
        Timber.d("viewmodel cleared")
    }


    fun init() {
        checkIfUserIsLoggedIn()
    }

    fun onSignInSuccess(authCode: String) {
        fetchToken(authCode)
    }

    fun onSignInClicked() {
        signInCommand.call()
    }

    val isConnected: LiveData<Boolean>
        get() = mConnected //custom accessors,

    /**
     * User has been successfully saved in DB, go to home
     */
    private fun onUserSaved() {
        goToHomeCommand.call()
    }

    /**
     * Check if user is LoggedIn : check if a token is present in DB
     */
    private fun checkIfUserIsLoggedIn() {
        compositeDisposable.add(mTokenRepository.accessTokenFromDB
                .subscribe(
                        { (_, accessToken) ->
                            Timber.d("token found")
                            TokenRepository.accessToken = accessToken
                            mConnected.value = true
                            goToHomeCommand.call()
                        },
                        { t ->
                            Timber.d("error happened: "+t)
                            initAuthRetrofit()
                            mConnected.value = false
                        },
                        {
                            Timber.d(" no token found")
                            initAuthRetrofit()
                            mConnected.value =false
                        }
                )
        )
    }

    /**
     * Build Retrofit for authentication
     * because it use a different URL that the base url, I have to build this one
     * and not use the @NetworkModule injection
     */
    private fun initAuthRetrofit() {
        //todo find a way to change base url in this case in order to not instantiate another retrofit
        authRetrofit = Retrofit.Builder()
                .baseUrl(AuthUtils.URI_TOKEN_RETROFIT)
                .client(okHttpClient.build())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
        mAuthService = authRetrofit.create(AuthUtils.AuthService::class.java)
    }


    /**
     * fetch Token from Dribbble after a successful authentication
     * @param authCode - authCode received from intent from WebView - See activity
     */
    private fun fetchToken(authCode: String) {
        compositeDisposable.add(
                mAuthService.getToken(
                        AuthUtils.CLIENT_ID,
                        AuthUtils.CLIENT_SECRET,
                        authCode,
                        AuthUtils.REDIRECT_URI)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                { token ->
                                    Timber.d("token fetched")
                                    insertToken(token)
                                    getUserAndStoreIt()
                                },
                                { error ->
                                    //todo - better management
                                    RxJavaPlugins.setErrorHandler { e -> Timber.d(e) }
                                }
                        )
        )
    }

    /**
     * After fetch token get the user in order to register it
     */
    private fun getUserAndStoreIt() {
        Timber.d("get user called")
        compositeDisposable.add(
                mUserRepository.getUserFromAPI(false)
                        .subscribe(
                                { goToHomeCommand.call()},      //this::saveUser,
                                this::onError
                        )
        )
    }

    /**
     * Save user in DB
     * @param user - User fetched
     */
    private fun saveUser(user: User) {
        compositeDisposable.add(mUserRepository.insertUser(user)
                .subscribe(
                        this::onUserSaved,
                        this::onError
                )
        )
    }

    /**
     * An error happened during DB ope for save user
     * @param t - Throwable
     */
    private fun onError(t: Throwable) {
        Timber.d(t)
    }

    /**
     * Insert Token in DB in order to use it for future request
     * @param token - token fetch after successful auth
     */
    private fun insertToken(token: Token) {
        compositeDisposable.add(mTokenRepository.insertToken(token)
                .subscribe(
                        { Timber.d("insert completed") },
                        { t -> Timber.d(t) }
                )
        )
    }


}
