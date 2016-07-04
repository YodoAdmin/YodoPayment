package co.yodo.mobile.manager;

import android.app.Activity;
import android.content.IntentSender;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.MessageListener;
import com.google.android.gms.nearby.messages.NearbyMessagesStatusCodes;
import com.google.android.gms.nearby.messages.Strategy;
import com.google.android.gms.nearby.messages.SubscribeCallback;
import com.google.android.gms.nearby.messages.SubscribeOptions;

import co.yodo.mobile.R;
import co.yodo.mobile.helper.PrefUtils;
import co.yodo.mobile.helper.SystemUtils;

/**
 * Created by hei on 15/06/16.
 * Handles the Nearby advertising
 */
public class PromotionManager {
    /** DEBUG */
    @SuppressWarnings( "unused" )
    private static final String TAG = PromotionManager.class.getSimpleName();

    /** The Activity object */
    private Activity ac;

    /** Implements all the GoogleApi callbacks */
    private IPromotionListener mActivity;

    /** Provides an entry point for Google Play services. */
    private GoogleApiClient mGoogleApiClient;

    /** Sets the time in seconds for a published message or a subscription to live */
    private static final Strategy PUB_SUB_STRATEGY = new Strategy.Builder()
            .setTtlSeconds( Strategy.TTL_SECONDS_INFINITE ).build();

    /** A {@link MessageListener} for processing messages from nearby devices. */
    private MessageListener mMessageListener;

    /**
     * Tracks if we are currently resolving an error related to Nearby permissions. Used to avoid
     * duplicate Nearby permission dialogs if the user initiates both subscription and publication
     * actions without having opted into Nearby.
     */
    private boolean mResolvingNearbyPermissionError = false;

    /** Request code to use when launching the resolution activity. */
    private int REQUEST_RESOLVE_ERROR = 1001;

    /** Listener for the advertise */
    public interface IPromotionListener extends
            GoogleApiClient.ConnectionCallbacks,
            GoogleApiClient.OnConnectionFailedListener {
    }

    public PromotionManager( IPromotionListener activity, MessageListener messageListener, int errorCode ) {
        if( !( activity instanceof Activity ) )
            throw new ExceptionInInitializerError( "The class has to be an activity" );

        if( messageListener == null )
            throw new NullPointerException( "The listener is null" );

        this.ac = (Activity) activity;
        this.mActivity = activity;
        this.mMessageListener = messageListener;
        this.REQUEST_RESOLVE_ERROR = errorCode;
    }

    public void startService() {
        // Connect to the service
        mGoogleApiClient = new GoogleApiClient.Builder( ac )
                .addConnectionCallbacks( mActivity )
                .addOnConnectionFailedListener( mActivity )
                .addApi( Nearby.MESSAGES_API )
                .build();
        mGoogleApiClient.connect();
    }

    public void stopService() {
        // Disconnect the api client if there is a connection
        if( mGoogleApiClient != null && mGoogleApiClient.isConnected() ) {
            PrefUtils.setSubscribing( ac, false );
            unsubscribe();
            mGoogleApiClient.disconnect();
        }
    }

    /**
     * Subscribes to messages from nearby devices. If not successful, attempts to resolve any error
     * related to Nearby permissions by displaying an opt-in dialog. Registers a callback which
     * updates state when the subscription expires.
     */
    public void subscribe() {
        SystemUtils.Logger( TAG, "trying to subscribe" );
        // Cannot proceed without a connected GoogleApiClient. Reconnect and execute the pending
        // task in onConnected().
        if( !mGoogleApiClient.isConnected() ) {
            if( !mGoogleApiClient.isConnecting() ) {
                mGoogleApiClient.connect();
            }
        } else {
            SubscribeOptions options = new SubscribeOptions.Builder()
                    .setStrategy( PUB_SUB_STRATEGY )
                    .setCallback( new SubscribeCallback() {
                        @Override
                        public void onExpired() {
                            super.onExpired();
                            SystemUtils.Logger( TAG, "no longer subscribing" );
                            PrefUtils.setSubscribing( ac, false );
                        }
                    }).build();

            Nearby.Messages.subscribe( mGoogleApiClient, mMessageListener, options )
                    .setResultCallback( new ResultCallback<Status>() {
                        @Override
                        public void onResult( @NonNull Status status ) {
                            if( status.isSuccess() ) {
                                SystemUtils.Logger( TAG, "subscribed successfully" );
                                PrefUtils.setSubscribing( ac, true );
                            } else {
                                SystemUtils.Logger( TAG, "could not subscribe" );
                                PrefUtils.setSubscribing( ac, false );
                                handleUnsuccessfulNearbyResult( status );
                            }
                        }
                    });
        }
    }

    /**
     * Ends the subscription to messages from nearby devices. If successful, resets state. If not
     * successful, attempts to resolve any error related to Nearby permissions by
     * displaying an opt-in dialog.
     */
    public void unsubscribe() {
        SystemUtils.Logger( TAG, "trying to unsubscribe" );
        // Cannot proceed without a connected GoogleApiClient. Reconnect and execute the pending
        // task in onConnected().
        if( !mGoogleApiClient.isConnected()  ) {
            if( !mGoogleApiClient.isConnecting() ) {
                mGoogleApiClient.connect();
            }
        } else {
            Nearby.Messages.unsubscribe( mGoogleApiClient, mMessageListener )
                    .setResultCallback( new ResultCallback<Status>() {
                        @Override
                        public void onResult( @NonNull Status status ) {
                            if( status.isSuccess() ) {
                                SystemUtils.Logger( TAG, "unsubscribed successfully" );
                                PrefUtils.setSubscribing( ac, false );
                            } else {
                                SystemUtils.Logger( TAG, "could not unsubscribe" );
                                PrefUtils.setSubscribing( ac, true );
                                handleUnsuccessfulNearbyResult( status );
                            }
                        }
                    });
        }
    }

    /**
     * Handles errors generated when performing a subscription or publication action. Uses
     * {@link Status#startResolutionForResult} to display an opt-in dialog to handle the case
     * where a device is not opted into using Nearby.
     */
    private void handleUnsuccessfulNearbyResult( Status status ) {
        SystemUtils.Logger( TAG, "processing error, status = " + status );
        if( status.getStatusCode() == NearbyMessagesStatusCodes.APP_NOT_OPTED_IN ) {
            if( !mResolvingNearbyPermissionError ) {
                try {
                    mResolvingNearbyPermissionError = true;
                    status.startResolutionForResult(
                            this.ac,
                            REQUEST_RESOLVE_ERROR
                    );
                } catch( IntentSender.SendIntentException e ) {
                    e.printStackTrace();
                }
            }
        } else {
            if( status.getStatusCode() == ConnectionResult.NETWORK_ERROR ) {
                Toast.makeText( ac, R.string.message_error_no_connectivity, Toast.LENGTH_LONG ).show();
            } else {
                // To keep things simple, pop a toast for all other error messages.
                Toast.makeText( ac, "Unsuccessful: " + status.getStatusMessage(), Toast.LENGTH_LONG ).show();
            }
        }
    }

    /**
     * Handles once the response for the problem has been called in the callback
     * onActivityResult
     */
    public void onResolutionResult() {
        mResolvingNearbyPermissionError = false;
    }
}
