package co.yodo.mobile.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import co.yodo.mobile.R;
import co.yodo.mobile.broadcastreceiver.BroadcastMessage;
import co.yodo.mobile.component.Intents;
import co.yodo.mobile.helper.GUIUtils;
import co.yodo.mobile.helper.PrefUtils;
import co.yodo.mobile.helper.SystemUtils;
import co.yodo.mobile.network.YodoRequest;
import co.yodo.mobile.network.model.ServerResponse;
import co.yodo.mobile.network.request.AuthenticateRequest;
import co.yodo.mobile.service.RegistrationIntentService;
import co.yodo.mobile.service.model.GCMResponse;
import co.yodo.mobile.ui.notification.ToastMaster;
import co.yodo.mobile.ui.notification.YodoHandler;

public class SplashActivity extends Activity implements YodoRequest.RESTListener {
    /** DEBUG */
    @SuppressWarnings( "unused" )
    private static final String TAG = SplashActivity.class.getSimpleName();

    /** The context object */
    private Context ac;

    /** Account identifier */
    private String hardwareToken;

    /** Messages Handler */
    private YodoHandler handlerMessages;

    /** Manager for the server requests */
    private YodoRequest mRequestManager;

    /** Request for error Google Play Services */
    private static final int REQUEST_CODE_RECOVER_PLAY_SERVICES = 0;

    /** Request codes for the permissions */
    private static final int PERMISSIONS_REQUEST_READ_PHONE_STATE = 1;

    /** Response codes for the server requests */
    private static final int AUTH_REQ = 0x00;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        GUIUtils.setLanguage( this );
        setContentView( R.layout.activity_splash );

        setupGUI();
        updateData();
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register( this );
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister( this );
        super.onStop();
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
        boolean hasServices = SystemUtils.isGooglePlayServicesAvailable(
                SplashActivity.this,
                REQUEST_CODE_RECOVER_PLAY_SERVICES
        );

        // Verify Google Play Services
        if( hasServices ) {
            hardwareToken = PrefUtils.getHardwareToken( ac );
            if( hardwareToken == null ) {
                setupPermissions();
            } else {
                mRequestManager.invoke( new AuthenticateRequest(
                        AUTH_REQ,
                        hardwareToken
                ) );
            }
        }
    }

    /**
     * Request the necessary permissions for this activity
     */
    private void setupPermissions() {
        boolean phoneStatePermission = SystemUtils.requestPermission(
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
        hardwareToken = PrefUtils.generateHardwareToken( ac );
        if( hardwareToken == null ) {
            // The device doesn't have a hardware token
            ToastMaster.makeText( ac, R.string.message_no_hardware, Toast.LENGTH_LONG ).show();
            finish();
        } else {
            // We have the hardware token, now let's verify if the user exists
            PrefUtils.saveHardwareToken( ac, hardwareToken );
            mRequestManager.invoke( new AuthenticateRequest(
                    AUTH_REQ,
                    hardwareToken
            ) );
        }
    }

    /**
     * Starts the main window of the YodoPayment
     * application, or the registration of the biometric token
     */
    private void startNextActivity() {
        final String authNumber = PrefUtils.getAuthNumber( ac );
        // The authnumber exists, so the biometric token has not been registered
        if( !authNumber.equals( "" ) ) {
            Intent intent = new Intent( ac, RegistrationBiometricActivity.class );
            intent.putExtra( Intents.AUTH_NUMBER, authNumber );
            startActivity( intent );
        }
        // The token biometric had already been sent, we can continue
        else {
            Intent intent = new Intent( ac, MainActivity.class );
            startActivity( intent );
        }
    }

    @Override
    public void onPrepare() {
    }

    @Override
    public void onResponse( int responseCode, ServerResponse response ) {
        String code, message;

        // Verify the type of the request
        switch( responseCode ) {
            case AUTH_REQ:
                code = response.getCode();

                // Verify the response code
                switch( code ) {
                    // The user exists, let's verify the rest of the registration
                    case ServerResponse.AUTHORIZED:
                        final boolean isTokenSent = PrefUtils.isGCMTokenSent( ac );
                        // There is no token for GCM, let's try to register
                        if( !isTokenSent ) {
                            Intent intent = new Intent( this, RegistrationIntentService.class );
                            intent.putExtra( BroadcastMessage.EXTRA_HARDWARE_TOKEN, hardwareToken );
                            startService( intent );
                        } else {
                            finish();
                            startNextActivity();
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
                        YodoHandler.sendMessage( YodoHandler.INIT_ERROR, handlerMessages, code, message );
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
                    Intent iSplash = new Intent( ac, SplashActivity.class );
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
    @SuppressWarnings("unused") // receives GCM receipts
    @Subscribe( sticky = true, threadMode = ThreadMode.MAIN )
    public void onResponseEvent( GCMResponse response ) {
        EventBus.getDefault().removeStickyEvent( response );
        boolean sentToken = PrefUtils.isGCMTokenSent( ac );
        finish();
        if( sentToken ) {
            startNextActivity();
        } else {
            Toast.makeText( ac, R.string.error_gcm_registration, Toast.LENGTH_SHORT ).show();
        }
    }
}
