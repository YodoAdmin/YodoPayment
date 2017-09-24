package co.yodo.mobile.business.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import co.yodo.mobile.R;
import co.yodo.mobile.YodoApplication;
import co.yodo.mobile.business.network.model.ServerResponse;
import co.yodo.mobile.business.network.request.RegisterRequest;
import co.yodo.mobile.helper.PrefUtils;
import co.yodo.mobile.business.network.ApiClient;
import co.yodo.mobile.model.dtos.GCMResponse;
import timber.log.Timber;

/**
 * Handles the registration of the GCM token to the Google Server and
 * Yodo servers
 */
public class RegistrationIntentService extends IntentService {
    /** DEBUG */
    private static final String TAG = RegistrationIntentService.class.getSimpleName();

    /**
     * It is used to send the hardware token, to register the gcm_id
     * EXTRA - The name - String object.
     */
    public static final String BUNDLE_HARDWARE_TOKEN = "BUNDLE_UUID";

    /** Manager for the server requests */
    @Inject
    ApiClient requestManager;

    public static void newInstance( Context context, String hardwareToken ) {
        Intent intent = new Intent( context, RegistrationIntentService.class );
        intent.putExtra( BUNDLE_HARDWARE_TOKEN, hardwareToken );
        context.startService( intent );
    }

    public RegistrationIntentService() {
        super( TAG );
        YodoApplication.getComponent().inject( this );
    }

    @Override
    protected void onHandleIntent( Intent intent ) {
        try {
            String hardwareToken = intent.getStringExtra( BUNDLE_HARDWARE_TOKEN );
            // This call goes out to the network to retrieve the token
            InstanceID instanceID = InstanceID.getInstance( this );
            String token = instanceID.getToken( getString( R.string.gcm_defaultSenderId ), GoogleCloudMessaging.INSTANCE_ID_SCOPE, null ) ;
            Timber.i( "GCM Registration Token: " + token );

            // Send the GCM token to the server
            requestManager.invoke(
                    new RegisterRequest( hardwareToken, token, RegisterRequest.RegST.GCM ),
                    new ApiClient.RequestCallback() {
                        @Override
                        public void onResponse( ServerResponse response ) {
                            final String code = response.getCode();
                            if( code.equals( ServerResponse.AUTHORIZED ) ) {
                                // If the token was successfully sent to the server
                                PrefUtils.saveGCMTokenSent( true );

                                // Notify UI that registration has completed, so the progress indicator can be hidden.
                                GCMResponse notify = new GCMResponse( response.getMessage() );
                                EventBus.getDefault().postSticky( notify );
                            } else {
                                // If there was an error in the server
                                handleApiError( getString( R.string.error_server ) );
                            }
                        }

                        @Override
                        public void onError( String message ) {
                            handleApiError( message );
                        }
                    }
            );
        } catch( Exception e ) {
            // If an exception happens while fetching the new token or updating our registration data
            // on a third-party server, this ensures that we'll attempt the update at a later time.
            e.printStackTrace();
            handleApiError( getString( R.string.error_unknown ) );
        }
    }

    /**
     * Handles any error from the server/google/device at the GCM registration time
     * @param message The error message to be displayed
     */
    private void handleApiError( String message ) {
        PrefUtils.saveGCMTokenSent( false );

        // Notify UI that registration has completed, so the progress indicator can be hidden.
        GCMResponse notify = new GCMResponse( message );
        EventBus.getDefault().postSticky( notify );
    }
}
