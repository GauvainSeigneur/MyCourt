package seigneur.gauvain.mycourt.utils.rx

import java.io.IOException
import java.net.SocketException

import javax.inject.Inject

import io.reactivex.exceptions.UndeliverableException
import io.reactivex.plugins.RxJavaPlugins
import retrofit2.HttpException
import timber.log.Timber
import java.net.UnknownHostException

/**
 * Implementation of NetworkErrorHandler using RxJavaPlugins to catch IO errors
 * and not stop application
 * see : https://github.com/ReactiveX/RxJava/wiki/What%27s-different-in-2.0
 */
class NetworkErrorHandlerImpl
@Inject
constructor() : NetworkErrorHandler {

    override fun handleNetworkErrors(error: Throwable, eventID: Int, listener: NetworkErrorHandler.onRXErrorListener) {
        RxJavaPlugins.setErrorHandler { e ->
            when(error) {
                is UndeliverableException -> listener.onUnexpectedException(e)
                is IOException -> listener.onNetworkException(e)
                is SocketException ->  listener.onNetworkException(e)
                is HttpException -> listener.onHttpException(e)
                else -> listener.onUnexpectedException(e)
            }
        }
    }

}

/*

public class NetworkErrorHandlerImpl implements NetworkErrorHandler {

    @Inject
    public NetworkErrorHandlerImpl() {}

    @Override
    public void handleNetworkErrors(final Throwable error, int eventID, NetworkErrorHandler.onRXErrorListener listener) {
        RxJavaPlugins.setErrorHandler(e ->{
            if (error instanceof UndeliverableException) {
                //Unknown error, rx cannot attribute this error to a class
                Timber.e(error.getMessage()+error.getCause());
                listener.onUnexpectedException(e);
                return;
            }
            if ((error instanceof SocketException) || (error instanceof IOException)) {
                // fine, irrelevant network problem or API that throws on cancellation
                Timber.e(error.getMessage()+error.getCause());
                listener.onNetworkException(e);
                return;
            }
            if ((error instanceof HttpException)) {
                //non-2xx HTTP response
                Timber.e(error.getMessage()+error.getCause());
                listener.onHttpException(e);
            }
        });
    }

}

 */