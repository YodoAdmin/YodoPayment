package co.yodo.mobile.main;

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
import co.yodo.mobile.component.ToastMaster;
import co.yodo.mobile.component.YodoHandler;
import co.yodo.mobile.data.ServerResponse;
import co.yodo.mobile.helper.AppConfig;
import co.yodo.mobile.helper.AppUtils;
import co.yodo.mobile.helper.Intents;
import co.yodo.mobile.net.YodoRequest;
import co.yodo.mobile.service.RegistrationIntentService;

public class SplashActivity extends Activity implements YodoRequest.RESTListener {
    /** DEBUG */
    @SuppressWarnings( "unused" )
    private static final String TAG = MainActivity.class.getSimpleName();

    /** The context object */
    private Context ac;

    /** Account identifier */
    private String hardwareToken;

    /** Messages Handler */
    private static YodoHandler handlerMessages;

    /** Request for error Google Play Services */
    private static final int REQUEST_CODE_RECOVER_PLAY_SERVICES = 0;

    /** Request codes for the permissions */
    private static final int PERMISSIONS_REQUEST_READ_PHONE_STATE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        AppUtils.setLanguage( this );

        setupGUI();
        updateData();
    }

    @Override
    public void onResume() {
        super.onResume();
        YodoRequest.getInstance().setListener( this );
        LocalBroadcastManager.getInstance( this ).registerReceiver( mRegistrationBroadcastReceiver, new IntentFilter( AppConfig.REGISTRATION_COMPLETE ) );
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance( this ).unregisterReceiver( mRegistrationBroadcastReceiver );
    }

    private void setupGUI() {
        // Get the context and handler for the messages
        ac = SplashActivity.this;
        handlerMessages = new YodoHandler( SplashActivity.this );
    }

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
                YodoRequest.getInstance().requestAuthentication( ac, hardwareToken );
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
        hardwareToken = AppUtils.generateHardwareToken( ac );
        if( hardwareToken == null ) {
            ToastMaster.makeText( ac, R.string.message_no_hardware, Toast.LENGTH_LONG ).show();
            finish();
        } else {
            AppUtils.saveHardwareToken( ac, hardwareToken );
            YodoRequest.getInstance().requestAuthentication( ac, hardwareToken );
        }
    }

    @Override
    public void onResponse( YodoRequest.RequestType type, ServerResponse response ) {
        switch( type ) {
            case ERROR_NO_INTERNET:
                handlerMessages.sendEmptyMessage( YodoHandler.NO_INTERNET );
                finish();
                break;

            case ERROR_GENERAL:
                handlerMessages.sendEmptyMessage( YodoHandler.GENERAL_ERROR );
                finish();
                break;

            case AUTH_REQUEST:
                String code = response.getCode();

                if( code.equals( ServerResponse.AUTHORIZED ) ) {
                    final String authNumber   = AppUtils.getAuthNumber( ac );
                    final boolean isTokenSent = AppUtils.getIsTokenSent( ac );

                    // The authnumber exists, so the biometric token has not been registered
                    if( !authNumber.equals( "" ) ) {
                        Intent intent = new Intent( SplashActivity.this, RegistrationBiometricActivity.class );
                        intent.putExtra( Intents.AUTH_NUMBER, authNumber );
                        startActivity( intent );
                        finish();
                    }
                    // The token for GCM has been sent, we can continue
                    else if( isTokenSent ) {
                        Intent intent = new Intent( SplashActivity.this, MainActivity.class );
                        startActivity( intent );
                        finish();
                    }
                    // There is no token for GCM, let's try to register
                    else {
                        // Start IntentService to register this application with GCM.
                        Intent intent = new Intent( this, RegistrationIntentService.class );
                        intent.putExtra( BroadcastMessage.EXTRA_HARDWARE_TOKEN, hardwareToken );
                        startService( intent );
                    }
                } else if( code.equals( ServerResponse.ERROR_FAILED ) ) {
                    Intent intent = new Intent( SplashActivity.this, RegistrationActivity.class);
                    startActivity( intent );
                    finish();
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
