package seigneur.gauvain.mycourt.utils.rx;

import java.io.IOException;
import java.net.SocketException;

import javax.inject.Inject;

import io.reactivex.exceptions.UndeliverableException;
import io.reactivex.plugins.RxJavaPlugins;
import retrofit2.HttpException;
import timber.log.Timber;

public class NetworkErrorHandlerImpl implements NetworkErrorHandler {

    @Inject
    public NetworkErrorHandlerImpl() {}

    @Override
    public void handleNetworkErrors(final Throwable error, NetworkErrorHandler.onRXErrorListener listener) {
        RxJavaPlugins.setErrorHandler(e ->{
            if (error instanceof UndeliverableException) {
                Timber.e(error.getMessage()+error.getCause());
                listener.onUnexpectedException(error);
            }
            if ((error instanceof IOException) || (error instanceof SocketException)) {
                Timber.e(error.getMessage()+error.getCause());
                listener.onNetworkException(error);
                // fine, irrelevant network problem or API that throws on cancellation
                return;
            }
            if ((error instanceof HttpException)) {
                Timber.e(error.getMessage()+error.getCause());
                // fine, irrelevant network problem or API that throws on cancellation
                listener.onHttpException(error);
                return;
            }
            //todo - manage it and reactivate for relase with firebase log
            // for now I don't want to manage it for debugging...
            /*if (error instanceof UndeliverableException) {
                Timber.e(error.getMessage()+error.getCause());
                listener.onUnexpectedException(error);
            }
            if (error instanceof InterruptedException) {
                // fine, some blocking code was interrupted by a dispose call
                return;
            }
            if ((error instanceof NullPointerException) || (error instanceof IllegalArgumentException)) {
                // that's likely a bug in the application
                //Thread.currentThread().getUncaughtExceptionHandler().handleException(Thread.currentThread(), e);
                return;
            }
            if (error instanceof IllegalStateException) {
                // that's a bug in RxJava or in a custom operator
                //Thread.currentThread().getUncaughtExceptionHandler().handleException(Thread.currentThread(), e);
                return;
            }*/
            //Timber.w("Undeliverable exception received, not sure what to do", e);
            }
        );

    }

}
