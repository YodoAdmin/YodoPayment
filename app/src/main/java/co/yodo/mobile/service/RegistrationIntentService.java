package co.yodo.mobile.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;

import co.yodo.mobile.R;
import co.yodo.mobile.broadcastreceiver.BroadcastMessage;
import co.yodo.mobile.network.model.ServerResponse;
import co.yodo.mobile.helper.AppConfig;
import co.yodo.mobile.helper.AppUtils;
import co.yodo.mobile.network.YodoRequest;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class RegistrationIntentService extends IntentService implements YodoRequest.RESTListener {
    /** The context object */
    private Context ac;

    @SuppressWarnings( "unused" )
    private static final String TAG = RegistrationIntentService.class.getSimpleName();
    private static final String[] TOPICS = { "global" };

    public RegistrationIntentService() {
        super( TAG );
    }

    @Override
    protected void onHandleIntent( Intent intent ) {
        // get the context
        ac = RegistrationIntentService.this;

        try {
            String hardwareToken = intent.getStringExtra( BroadcastMessage.EXTRA_HARDWARE_TOKEN );
            // Initially this call goes out to the network to retrieve the token, subsequent calls
            // are local.
            InstanceID instanceID = InstanceID.getInstance( this );
            String token = instanceID.getToken( getString( R.string.gcm_defaultSenderId ), GoogleCloudMessaging.INSTANCE_ID_SCOPE, null ) ;
            AppUtils.Logger( TAG, "GCM Registration Token: " + token );

            // Send the GCM token to the server
            sendRegistrationToServer( hardwareToken, token );
            // Subscribe to topic channels
            subscribeTopics( token );

            // You should store a boolean that indicates whether the generated token has been
            // sent to your server. If the boolean is false, send the token to your server,
            // otherwise your server should have already received the token.
            //AppUtils.saveIsTokenSent( this, true );
        } catch( Exception e ) {
            // If an exception happens while fetching the new token or updating our registration data
            // on a third-party server, this ensures that we'll attempt the update at a later time.
            e.printStackTrace();
            AppUtils.saveIsTokenSent( this, false );
            // Notify UI that registration has completed, so the progress indicator can be hidden.
            Intent registrationComplete = new Intent( AppConfig.REGISTRATION_COMPLETE );
            LocalBroadcastManager.getInstance( this ).sendBroadcast( registrationComplete );
        }
    }

    /**
     * Persist registration to third-party servers.
     *
     * Modify this method to associate the user's GCM registration token with any server-side account
     * maintained by your application.
     * @param hardwareToken The hardware token used in the server.
     * @param token The new token.
     */
    private void sendRegistrationToServer( String hardwareToken, String token ) throws IOException {
        YodoRequest.getInstance( ac ).setListener( this );
        YodoRequest.getInstance( ac ).requestGCMRegistration( this, hardwareToken, token );
    }

    /**
     * Subscribe to any GCM topics of interest, as defined by the TOPICS constant.
     *
     * @param token GCM token
     * @throws IOException if unable to reach the GCM PubSub service
     */
    private void subscribeTopics( String token ) throws IOException {
        GcmPubSub pubSub = GcmPubSub.getInstance( this );
        for( String topic : TOPICS ) {
            pubSub.subscribe( token, "/topics/" + topic, null );
        }
    }

    @Override
    public void onResponse( YodoRequest.RequestType type, ServerResponse response ) {
        switch( type ) {
            case ERROR_GENERAL:
            case REG_GCM_REQUEST:
                if( response != null ) {
                    String code = response.getCode();
                    if( code.equals( ServerResponse.AUTHORIZED ) )
                        AppUtils.saveIsTokenSent( this, true );
                }
                // Notify UI that registration has completed, so the progress indicator can be hidden.
                Intent registrationComplete = new Intent( AppConfig.REGISTRATION_COMPLETE );
                LocalBroadcastManager.getInstance( this ).sendBroadcast( registrationComplete );
                break;
        }
    }
}
