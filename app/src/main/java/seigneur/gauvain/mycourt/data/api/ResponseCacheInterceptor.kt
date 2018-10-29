package seigneur.gauvain.mycourt.data.api

import java.io.IOException

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import timber.log.Timber

import seigneur.gauvain.mycourt.utils.Constants.RESPONSE_CACHE_DELAY

/**
 * Interceptor to cache data and maintain it for a delay defined inside the request.
 * the response is retrieved from cache.
 */
class ResponseCacheInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (request.header(RESPONSE_CACHE_DELAY) != null && Integer.valueOf(request.header(RESPONSE_CACHE_DELAY)) > 0) {
            Timber.i("Response cache applied")
            val originalResponse = chain.proceed(chain.request())
            return originalResponse.newBuilder()
                    .removeHeader(RESPONSE_CACHE_DELAY)
                    .header("Cache-Control", "public, max-age=" + request.header(RESPONSE_CACHE_DELAY)!!)
                    .build()
        } else {
            Timber.i("Response cache not applied")
        }
        return chain.proceed(chain.request())
    }
}
/*
public class ResponseCacheInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        if(request.header(RESPONSE_CACHE_DELAY)!=null && Integer.valueOf(request.header(RESPONSE_CACHE_DELAY))>0) {
            Timber.i("Response cache applied");
            Response originalResponse = chain.proceed(chain.request());
            return originalResponse.newBuilder()
                    .removeHeader(RESPONSE_CACHE_DELAY)
                    .header("Cache-Control", "public, max-age=" + request.header(RESPONSE_CACHE_DELAY))
                    .build();
        } else {
            Timber.i("Response cache not applied");
        }
        return chain.proceed(chain.request());
    }
}
*/