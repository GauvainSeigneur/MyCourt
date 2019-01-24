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
 * Implementation of NetworkErrorHandler like RxJavaPlugins to catch IO errors
 * and not stop application
 * see : https://github.com/ReactiveX/RxJava/wiki/What%27s-different-in-2.0
 */

class NetworkErrorHandlerImpl : NetworkErrorHandler {
    override fun handleNetworkErrors(error: Throwable, eventID: Int, listener: NetworkErrorHandler.onErrorListener) {
        Timber.d("NetworkErrorHandlerImpl called ")
        when(error) {
            is IOException -> listener.onNetworkException(error)
            is SocketException ->  listener.onNetworkException(error)
            is UnknownHostException -> listener.onNetworkException(error)
            is InterruptedException -> listener.onNetworkException(error)
            is HttpException -> listener.onHttpException(error)
        }
    }

}
