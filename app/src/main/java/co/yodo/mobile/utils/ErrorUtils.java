package co.yodo.mobile.utils;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuthException;

import java.io.IOException;

import co.yodo.mobile.R;
import co.yodo.mobile.business.network.ApiClient;
import co.yodo.mobile.helper.AlertDialogHelper;
import co.yodo.mobile.model.dtos.FirebaseAuthError;
import timber.log.Timber;

/**
 * Created by hei on 03/03/17.
 * Handle errors and exceptions
 */

public class ErrorUtils {
    /**
     * Handle general errors
     * @param activity The activity
     * @param message The message id
     * @param close If the dialog should close the app
     */
    public static void handleError(final Activity activity, String message, boolean close) {
        DialogInterface.OnClickListener onClick;

        if(close) {
            onClick = new DialogInterface.OnClickListener() {
                @Override
                public void onClick( DialogInterface dialog, int which ) {
                    activity.finish();
                }
            };
        } else {
            onClick = null;
        }

        AlertDialogHelper.show(
                activity,
                message,
                onClick
        );
    }

    /**
     * Handles the requests errors
     * @param context The application context
     * @param callback Return the message to the observer
     * @param error The error type
     */
    public static void handleApiError(Context context, Throwable error, ApiClient.RequestCallback callback) {
        error.printStackTrace();

        // Unknown error
        int message = R.string.error_unknown;

        if (error instanceof IOException) {
            // Network error
            message = R.string.error_network;
        } else if (error instanceof NullPointerException) {
            // Server error
            message = R.string.error_server;
        }

        callback.onError(context.getString(message));
    }

    /**
     * Handles errors related to the phone authentication
     * @param activity The activity that generated the error
     * @param ex The exception
     */
    public static void handleFirebaseError(Activity activity, FirebaseException ex) {
        Timber.e(ex);
        FirebaseAuthError error = FirebaseAuthError.fromException((FirebaseAuthException) ex);
        String message;

        switch (error) {
            case ERROR_INVALID_PHONE_NUMBER:
                message = activity.getString(R.string.error_phone_invalid);
                break;

            case ERROR_TOO_MANY_REQUESTS:
                message = activity.getString(R.string.error_phone_attempts);
                break;

            case ERROR_QUOTA_EXCEEDED:
                message = activity.getString(R.string.error_phone_quota);
                break;

            default:
                message = error.getDescription();
                break;
        }
        handleError(activity, message, false);
    }
}
