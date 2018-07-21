package seigneur.gauvain.mycourt.di.modules;

import android.app.Application;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import seigneur.gauvain.mycourt.data.api.DribbbleService;
import seigneur.gauvain.mycourt.data.api.HeaderInterceptor;
import seigneur.gauvain.mycourt.data.api.ResponseCacheInterceptor;

/**
 * Created by gauvain on 20/03/2018.
 */
@Module
public class NetworkModule {

    private static String BASE_URL = "https://api.dribbble.com/v2/";
    private static final String CACHE_DIR = "httpCache";

    @Provides
    @Singleton
    HttpLoggingInterceptor provideOkhhtpLogging(){
        return new HttpLoggingInterceptor();
    }

    @Provides
    @Singleton
    Cache provideOkHttpCache(Application application) {
        int cacheSize = 200 * 1024 * 1024; // 200 MiB
        File httpCacheDirectory = new File(application.getCacheDir(), CACHE_DIR);
        Cache cache = new Cache(httpCacheDirectory, cacheSize);
        return cache;
    }

    @Singleton
    @Provides
    OkHttpClient.Builder provideHttpClientBuilder(Cache cache, HttpLoggingInterceptor httpLoggingInterceptor) {
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient.Builder  httpClient = new OkHttpClient.Builder()
                //enable cache strategy
                .cache(cache)
                .addNetworkInterceptor(new HeaderInterceptor())
                // Enable response caching
                .addNetworkInterceptor(new ResponseCacheInterceptor())
                //log interceptor
                //.addInterceptor(new LoggingInterceptor());
                .addInterceptor(httpLoggingInterceptor);
        return httpClient;
    }

    @Singleton
    @Provides
    Gson provideGson() { return new GsonBuilder().create(); }

    @Singleton
    @Provides
    Retrofit provideRetrofit(Gson gson, OkHttpClient.Builder clientBuilder) {
        Retrofit retrofit = new Retrofit.Builder()
                .client(clientBuilder.build())
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create()) //basic
                //.addCallAdapterFactory(RxErrorHandlingCallAdapterFactory.create()) //extension to manage error during communication
                .baseUrl(BASE_URL)
                .build();
        return retrofit;
    }

    @Singleton
    @Provides
    DribbbleService provideApiWebservice(Retrofit restAdapter) {
        return restAdapter.create(DribbbleService.class);
    }


}
