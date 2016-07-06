package co.yodo.mobile.manager;

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
    private AppCompatActivity ac;

    /** Implements all the GoogleApi callbacks */
    private IPromotionListener mActivity;

    /** Provides an entry point for Google Play services. */
    private GoogleApiClient mGoogleApiClient;

    /** Sets the time in seconds for a published message or a subscription to live */
    private static final Strategy PUB_SUB_STRATEGY = new Strategy.Builder()
            .setTtlSeconds( Strategy.TTL_SECONDS_INFINITE ).build();

    /** A {@link MessageListener} for processing messages from nearby devices. */
    private MessageListener mMessageListener;

    /** Listener for the advertise */
    public interface IPromotionListener extends
            GoogleApiClient.ConnectionCallbacks,
            GoogleApiClient.OnConnectionFailedListener {
    }

    public PromotionManager( IPromotionListener activity, MessageListener messageListener ) {
        if( !( activity instanceof AppCompatActivity ) )
            throw new ExceptionInInitializerError( "The class has to be an AppCompatActivity" );

        if( messageListener == null )
            throw new NullPointerException( "The listener is null" );

        this.ac = (AppCompatActivity) activity;
        this.mActivity = activity;
        this.mMessageListener = messageListener;
    }

    public void startService() {
        // Connect to the service
        mGoogleApiClient = new GoogleApiClient.Builder( ac )
                .addApi( Nearby.MESSAGES_API )
                .addConnectionCallbacks( mActivity )
                .enableAutoManage( ac, mActivity )
                .build();
        mGoogleApiClient.connect();
    }

    /**
     * Subscribes to messages from nearby devices. If not successful, attempts to resolve any error
     * related to Nearby permissions by displaying an opt-in dialog. Registers a callback which
     * updates state when the subscription expires.
     */
    public void subscribe() {
        // If not connected, ignore
        if( !mGoogleApiClient.isConnected() )
            return;

        SystemUtils.Logger( TAG, "trying to subscribe" );
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
        if( !mGoogleApiClient.isConnected() )
            return;

        SystemUtils.Logger( TAG, "trying to unsubscribe" );
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
                        }
                    }
                });

    }
}
