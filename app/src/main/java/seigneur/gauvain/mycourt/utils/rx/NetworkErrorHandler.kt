package seigneur.gauvain.mycourt.utils.rx

/**
 * Interface which allows to treat all IO errors in one place when injected in Presenter
 */
interface NetworkErrorHandler {

    interface onRXErrorListener {
        /**
         * Unexpected error
         * todo : must be handled in another place ? like GeneralErrorHandler ?
         */
        fun onUnexpectedException(throwable: Throwable)

        /**
         * a network exception (e.g Connection lost during request)
         */
        fun onNetworkException(throwable: Throwable)

        /**
         * A non-200 HTTP status code was received from the server.
         */
        fun onHttpException(throwable: Throwable)

    }

    fun handleNetworkErrors(error: Throwable, eventID: Int, listener: onRXErrorListener)

}
