package seigneur.gauvain.mycourt.data.api;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import seigneur.gauvain.mycourt.data.repository.TokenRepository;

/**
 * Created by gauvain on 25/02/2018.
 */
public class HeaderInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request.Builder builder = chain.request().newBuilder();
        builder.addHeader("Authorization", "Bearer "+ TokenRepository.accessToken);
        return chain.proceed(builder.build());
    }

}

