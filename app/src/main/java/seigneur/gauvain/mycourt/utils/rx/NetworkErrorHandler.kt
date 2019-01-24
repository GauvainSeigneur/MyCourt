package seigneur.gauvain.mycourt.utils.rx

/**
 * Interface which allows to treat all IO errors in one place when injected in Presenter
 * Note :
 * this do not mange NullPointerException, IllegalArgumentException, IllegalStateException
 * or UndeliverableException
 * those errors must be managed bu a general RxJavaPlugin Error Handler
 */
interface NetworkErrorHandler {

    interface onErrorListener {

        /**
         * a network exception (e.g Connection lost during request)
         */
        fun onNetworkException(throwable: Throwable)

        /**
         * A non-200 HTTP status code was received from the server.
         */
        fun onHttpException(throwable: Throwable)

    }

    fun handleNetworkErrors(error: Throwable, eventID: Int, listener: onErrorListener)

}
