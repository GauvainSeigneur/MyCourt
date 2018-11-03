package seigneur.gauvain.mycourt.di.modules

import android.app.Application

import com.google.gson.Gson
import com.google.gson.GsonBuilder

import java.io.File

import javax.inject.Singleton

import dagger.Module
import dagger.Provides
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import seigneur.gauvain.mycourt.data.api.DribbbleService
import seigneur.gauvain.mycourt.data.api.HeaderInterceptor
import seigneur.gauvain.mycourt.data.api.ResponseCacheInterceptor
import seigneur.gauvain.mycourt.di.modules.NetworkModule.Companion.BASE_URL

/**
 * Created by gauvain on 20/03/2018.
 */
@Module
class NetworkModule {

    @Provides
    @Singleton
    internal fun provideOkhhtpLogging(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor()
    }

    @Provides
    @Singleton
    internal fun provideOkHttpCache(application: Application): Cache {
        val cacheSize = 200 * 1024 * 1024 // 200 MiB
        val httpCacheDirectory = File(application.cacheDir, CACHE_DIR)
        return Cache(httpCacheDirectory, cacheSize.toLong())
    }

    @Singleton
    @Provides
    internal fun provideHttpClientBuilder(cache: Cache, httpLoggingInterceptor: HttpLoggingInterceptor): OkHttpClient.Builder {
        httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        return OkHttpClient.Builder()
                //enable cache strategy
                .cache(cache)
                .addNetworkInterceptor(HeaderInterceptor())
                // Enable response caching
                .addNetworkInterceptor(ResponseCacheInterceptor())
                //log interceptor
                //.addInterceptor(new LoggingInterceptor());
                .addInterceptor(httpLoggingInterceptor)
    }

    @Singleton
    @Provides
    internal fun provideGson(): Gson {
        return GsonBuilder().create()
    }


    @Singleton
    @Provides
    internal fun provideRetrofit(gson: Gson, clientBuilder: OkHttpClient.Builder): Retrofit {
        return Retrofit.Builder()
                .client(clientBuilder.build())
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create()) //basic
                //.addCallAdapterFactory(RxErrorHandlingCallAdapterFactory.create()) //extension to manage error during communication
                .baseUrl(BASE_URL)
                .build()
    }

    @Singleton
    @Provides
    internal fun provideApiWebservice(restAdapter: Retrofit): DribbbleService {
        return restAdapter.create(DribbbleService::class.java)
    }

    companion object {

        private val BASE_URL = "https://api.dribbble.com/v2/"
        private val CACHE_DIR = "httpCache"
    }


}
