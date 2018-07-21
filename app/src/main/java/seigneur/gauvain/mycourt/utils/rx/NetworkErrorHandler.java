package seigneur.gauvain.mycourt.utils.rx;

public interface NetworkErrorHandler {

        interface onRXErrorListener {
            /**
             * Unexpected error
             * todo : must be handled in another place ? like GeneralErrorHandler ?
             */
            void onUnexpectedException(Throwable throwable);

            /**
             * a network exception (e.g Connection lost during request)
             */
            void onNetworkException(Throwable throwable);

            /**
             * A non-200 HTTP status code was received from the server.
             */
            void onHttpException(Throwable throwable);

        }

        void handleNetworkErrors(Throwable error, onRXErrorListener listener);

}
