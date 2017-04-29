package co.yodo.mobile.business;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.MessageListener;
import com.google.android.gms.nearby.messages.Strategy;
import com.google.android.gms.nearby.messages.SubscribeCallback;
import com.google.android.gms.nearby.messages.SubscribeOptions;

import co.yodo.mobile.helper.PrefUtils;
import timber.log.Timber;

/**
 * Created by hei on 15/06/16.
 * Handles the Nearby advertising
 */
public class PromotionManager {
    /** The Activity object */
    private AppCompatActivity ac;

    /** Implements all the GoogleApi callbacks */
    private IPromotionListener listener;

    /** Provides an entry point for Google Play services. */
    private GoogleApiClient googleApiClient;

    /** Sets the time in seconds for a published message or a subscription to live */
    private static final Strategy PUB_SUB_STRATEGY = new Strategy.Builder()
            .setTtlSeconds( Strategy.TTL_SECONDS_INFINITE ).build();

    /** A {@link MessageListener} for processing messages from nearby devices. */
    private MessageListener nearbyListener;

    /** Listener for the advertise */
    public interface IPromotionListener extends
            GoogleApiClient.ConnectionCallbacks,
            GoogleApiClient.OnConnectionFailedListener {
    }

    public PromotionManager( IPromotionListener activity, MessageListener nearbyListener ) {
        if( !( activity instanceof AppCompatActivity ) ) {
            throw new ExceptionInInitializerError( "The class has to be an AppCompatActivity" );
        }

        if( nearbyListener == null ) {
            throw new NullPointerException( "The listener is null" );
        }

        this.ac = (AppCompatActivity) activity;
        this.listener = activity;
        this.nearbyListener = nearbyListener;
    }

    public void start() {
        // Connect to the service
        googleApiClient = new GoogleApiClient.Builder( ac )
                .addApi( Nearby.MESSAGES_API )
                .addConnectionCallbacks( listener )
                .enableAutoManage( ac, listener )
                .build();
        googleApiClient.connect();
    }

    /**
     * Subscribes to messages from nearby devices. If not successful, attempts to resolve any error
     * related to Nearby permissions by displaying an opt-in dialog. Registers a callback which
     * updates state when the subscription expires.
     */
    public void subscribe() {
        // If not connected, ignore
        if( isNotAvailable() )
            return;

        SubscribeOptions options = new SubscribeOptions.Builder()
                .setStrategy( PUB_SUB_STRATEGY )
                .setCallback( new SubscribeCallback() {
                    @Override
                    public void onExpired() {
                        super.onExpired();
                    }
                }).build();

        Nearby.Messages.subscribe( googleApiClient, nearbyListener, options )
                .setResultCallback( new ResultCallback<Status>() {
                    @Override
                    public void onResult( @NonNull Status status ) {
                        if( status.isSuccess() ) {
                            Timber.i( "subscribed successfully" );
                        } else {
                            Timber.i( "could not subscribe" );
                            PrefUtils.setSubscribing( ac, false );
                        }
                    }
                });
    }

    /**
     * Ends the subscription to messages from nearby devices. If successful, resets state. If not
     * successful, attempts to resolve any error related to Nearby permissions by
     * displaying an opt-in dialog.
     */
    public void unsubscribe() {
        // If not connected, ignore
        if( isNotAvailable() )
            return;

        Nearby.Messages.unsubscribe( googleApiClient, nearbyListener )
                .setResultCallback( new ResultCallback<Status>() {
                    @Override
                    public void onResult( @NonNull Status status ) {
                        if( status.isSuccess() ) {
                            Timber.i( "unsubscribed successfully" );
                        } else {
                            Timber.i( "could not unsubscribe" );
                            PrefUtils.setSubscribing( ac, true );
                        }
                    }
                });
    }

    /**
     * Verifies that the GoogleApiClient is ready
     * @return true if it is ready, otherwise false
     */
    private boolean isNotAvailable() {
        return googleApiClient == null || !googleApiClient.isConnected() || googleApiClient.isConnecting();
    }
}
