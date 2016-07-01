package co.yodo.mobile.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import org.greenrobot.eventbus.EventBus;

import co.yodo.mobile.R;
import co.yodo.mobile.broadcastreceiver.BroadcastMessage;
import co.yodo.mobile.helper.SystemUtils;
import co.yodo.mobile.network.model.ServerResponse;
import co.yodo.mobile.helper.PrefUtils;
import co.yodo.mobile.network.YodoRequest;
import co.yodo.mobile.network.request.RegisterRequest;
import co.yodo.mobile.service.model.GCMResponse;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class RegistrationIntentService extends IntentService implements YodoRequest.RESTListener {
    /** DEBUG */
    @SuppressWarnings( "unused" )
    private static final String TAG = RegistrationIntentService.class.getSimpleName();

    /** The context object */
    private Context ac;

    /** Manager for the server requests */
    private YodoRequest mRequestManager;

    /** Response codes for the server requests */
    private static final int REG_REQ = 0x00;

    public RegistrationIntentService() {
        super( TAG );
        mRequestManager = YodoRequest.getInstance( ac );
        mRequestManager.setListener( this );
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
            SystemUtils.Logger( TAG, "GCM Registration Token: " + token );

            // Send the GCM token to the server
            mRequestManager.invoke( new RegisterRequest(
                    REG_REQ,
                    hardwareToken,
                    token,
                    RegisterRequest.RegST.GCM
            ) );
        } catch( Exception e ) {
            // If an exception happens while fetching the new token or updating our registration data
            // on a third-party server, this ensures that we'll attempt the update at a later time.
            e.printStackTrace();
            PrefUtils.saveGCMTokenSent( this, false );
            // Notify UI that registration has completed, so the progress indicator can be hidden.
            GCMResponse notify = new GCMResponse( e );
            EventBus.getDefault().postSticky( notify );
        }
    }

    @Override
    public void onResponse( int responseCode, ServerResponse response ) {
        switch( responseCode ) {
            case REG_REQ:
                String code = response.getCode();
                // If the token was successfully sent to the server
                if( code.equals( ServerResponse.AUTHORIZED ) )
                    PrefUtils.saveGCMTokenSent( this, true );

                // Notify UI that registration has completed, so the progress indicator can be hidden.
                GCMResponse notify = new GCMResponse( response );
                EventBus.getDefault().postSticky( notify );
                break;
        }
    }
}
