package co.yodo.mobile.ui;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import co.yodo.mobile.R;
import co.yodo.mobile.broadcastreceiver.BroadcastMessage;
import co.yodo.mobile.ui.notification.ToastMaster;
import co.yodo.mobile.ui.notification.YodoHandler;
import co.yodo.mobile.network.model.ServerResponse;
import co.yodo.mobile.helper.AppConfig;
import co.yodo.mobile.helper.AppUtils;
import co.yodo.mobile.helper.Intents;
import co.yodo.mobile.network.YodoRequest;
import co.yodo.mobile.service.RegistrationIntentService;

public class SplashActivity extends Activity implements YodoRequest.RESTListener {
    /** DEBUG */
    @SuppressWarnings( "unused" )
    private static final String TAG = SplashActivity.class.getSimpleName();

    /** The context object */
    private Context ac;

    /** Account identifier */
    private String hardwareToken;

    /** Messages Handler */
    private static YodoHandler handlerMessages;

    /** Manager for the server requests */
    private YodoRequest mRequestManager;

    /** Request for error Google Play Services */
    private static final int REQUEST_CODE_RECOVER_PLAY_SERVICES = 0;

    /** Request codes for the permissions */
    private static final int PERMISSIONS_REQUEST_READ_PHONE_STATE = 1;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        AppUtils.setLanguage( this );
        setContentView( R.layout.activity_splash );

        setupGUI();
        updateData();
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance( ac ).registerReceiver(
                mRegistrationBroadcastReceiver,
                new IntentFilter( AppConfig.REGISTRATION_COMPLETE )
        );
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance( ac ).unregisterReceiver(
                mRegistrationBroadcastReceiver
        );
    }

    /**
     * Configures the main GUI Controllers
     */
    private void setupGUI() {
        // Get the context
        ac = SplashActivity.this;
        handlerMessages = new YodoHandler( SplashActivity.this );
        mRequestManager = YodoRequest.getInstance( ac );
        mRequestManager.setListener( this );
    }

    /**
     * Sets the main permissions, and values
     */
    private void updateData() {
        // Get the main booleans
        boolean hasServices = AppUtils.isGooglePlayServicesAvailable(
                SplashActivity.this,
                REQUEST_CODE_RECOVER_PLAY_SERVICES
        );

        // Verify Google Play Services
        if( hasServices ) {
            hardwareToken = AppUtils.getHardwareToken( ac );
            if( hardwareToken == null ) {
                setupPermissions();
            } else {
                mRequestManager.requestClientAuth( hardwareToken );
            }
        }
    }

    /**
     * Request the necessary permissions for this activity
     */
    private void setupPermissions() {
        boolean phoneStatePermission = AppUtils.requestPermission(
                SplashActivity.this,
                R.string.message_permission_read_phone_state,
                Manifest.permission.READ_PHONE_STATE,
                PERMISSIONS_REQUEST_READ_PHONE_STATE
        );

        if( phoneStatePermission )
            authenticateUser();
    }

    /**
     * Generates the hardware token after we have the permission
     * and verifies if it is null or not. Null could be caused
     * if the bluetooth is off
     */
    private void authenticateUser() {
        // Gets the hardware token
        hardwareToken = AppUtils.generateHardwareToken( ac );
        if( hardwareToken == null ) {
            // The device doesn't have a hardware token
            ToastMaster.makeText( ac, R.string.message_no_hardware, Toast.LENGTH_LONG ).show();
            finish();
        } else {
            // We have the hardware token, now let's verify if the user exists
            AppUtils.saveHardwareToken( ac, hardwareToken );
            mRequestManager.requestClientAuth( hardwareToken );
        }
    }

    @Override
    public void onResponse( YodoRequest.RequestType type, ServerResponse response ) {
        String code, message;

        // Verify the type of the request
        switch( type ) {
            case AUTH_REQUEST:
                code = response.getCode();

                // Verify the response code
                switch( code ) {
                    // The user exists, let's verify the rest of the registration
                    case ServerResponse.AUTHORIZED:
                        final String authNumber   = AppUtils.getAuthNumber( ac );
                        final boolean isTokenSent = AppUtils.getIsTokenSent( ac );

                        // The authnumber exists, so the biometric token has not been registered
                        if( !authNumber.equals( "" ) ) {
                            Intent intent = new Intent( SplashActivity.this, RegistrationBiometricActivity.class );
                            intent.putExtra( Intents.AUTH_NUMBER, authNumber );
                            startActivity( intent );
                            finish();
                        }
                        // There is no token for GCM, let's try to register
                        else if( !isTokenSent ) {
                            // Start IntentService to register this application with GCM.
                            Intent intent = new Intent( this, RegistrationIntentService.class );
                            intent.putExtra( BroadcastMessage.EXTRA_HARDWARE_TOKEN, hardwareToken );
                            startService( intent );
                        }
                        // The token for GCM and biometric have been sent, we can continue
                        else {
                            Intent intent = new Intent( SplashActivity.this, MainActivity.class );
                            startActivity( intent );
                            finish();
                        }
                        break;

                    // Let's try to register
                    case ServerResponse.ERROR_FAILED:
                        Intent intent = new Intent( SplashActivity.this, RegistrationActivity.class );
                        startActivity( intent );
                        finish();
                        break;

                    // There was an error during the process
                    default:
                        message = response.getMessage();
                        AppUtils.sendMessage( YodoHandler.INIT_ERROR, handlerMessages, code, message );
                        break;
                }
                break;
        }
    }

    @Override
    protected void onActivityResult( int requestCode, int resultCode, Intent data ) {
        switch( requestCode ) {
            case REQUEST_CODE_RECOVER_PLAY_SERVICES:
                finish();
                if( resultCode == RESULT_OK ) {
                    Intent iSplash = new Intent( this, SplashActivity.class );
                    startActivity( iSplash );
                } else if( resultCode == RESULT_CANCELED ) {
                    Toast.makeText( ac, R.string.error_play_services, Toast.LENGTH_SHORT ).show();
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult( int requestCode, @NonNull String permissions[], @NonNull int[] grantResults ) {
        switch( requestCode ) {
            case PERMISSIONS_REQUEST_READ_PHONE_STATE:
                // If request is cancelled, the result arrays are empty.
                if( grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED ) {
                    // Permission Granted
                    authenticateUser();
                } else {
                    // Permission Denied
                    finish();
                }
                break;

            default:
                super.onRequestPermissionsResult( requestCode, permissions, grantResults );
        }
    }

    /**
     * Message received from the service that registers the gcm token
     */
    private BroadcastReceiver mRegistrationBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive( Context context, Intent intent ) {
            boolean sentToken = AppUtils.getIsTokenSent( context );
            finish();
            if( sentToken ) {
                intent = new Intent( SplashActivity.this, MainActivity.class );
                startActivity( intent );
            } else {
                Toast.makeText( ac, R.string.error_gcm_registration, Toast.LENGTH_SHORT ).show();
            }
        }
    };
}
