package co.yodo.mobile.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

import co.yodo.mobile.R;
import co.yodo.mobile.YodoApplication;
import co.yodo.mobile.business.network.ApiClient;
import co.yodo.mobile.business.network.model.ServerResponse;
import co.yodo.mobile.business.network.request.AuthenticateRequest;
import co.yodo.mobile.business.service.RegistrationIntentService;
import co.yodo.mobile.helper.PrefUtils;
import co.yodo.mobile.utils.SystemUtils;
import co.yodo.mobile.model.dtos.GCMResponse;
import co.yodo.mobile.ui.notification.ToastMaster;
import co.yodo.mobile.utils.ErrorUtils;

public class SplashActivity extends AppCompatActivity {
    /** The application context */
    @Inject
    Context context;

    /** Manager for the server requests */
    @Inject
    ApiClient requestManager;

    /** Account identifier */
    private String hardwareToken;

    /** Request for error Google Play Services */
    private static final int REQUEST_CODE_RECOVER_PLAY_SERVICES = 0;

    /** Request codes for the permissions */
    private static final int PERMISSIONS_REQUEST_READ_PHONE_STATE = 1;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        //GUIUtils.setLanguage( this );

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

    @Override
    protected void onActivityResult( int requestCode, int resultCode, Intent data ) {
        switch( requestCode ) {
            case REQUEST_CODE_RECOVER_PLAY_SERVICES:
                if( resultCode == RESULT_OK ) {
                    // Google play services installed
                    Intent iSplash = new Intent( context, SplashActivity.class );
                    startActivity( iSplash );
                } else if( resultCode == RESULT_CANCELED ) {
                    // Denied to install
                    Toast.makeText( context, R.string.error_play_services, Toast.LENGTH_SHORT ).show();
                }
                finish();
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
                    generateUserToken();
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
     * Configures the main GUI Controllers
     */
    private void setupGUI() {
        // Injection
        YodoApplication.getComponent().inject( this );
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
            hardwareToken = PrefUtils.getHardwareToken();
            if( hardwareToken != null ) {
                authenticateUser();
            } else {
                generateUserToken();
            }
        }
    }

    /**
     * Request the necessary permissions for this activity
     * and generates the hardware token
     */
    private void generateUserToken() {
        boolean phoneStatePermission = SystemUtils.requestPermission(
                SplashActivity.this,
                R.string.text_permission_read_phone_state,
                Manifest.permission.READ_PHONE_STATE,
                PERMISSIONS_REQUEST_READ_PHONE_STATE
        );

        if( phoneStatePermission ) {
            hardwareToken = PrefUtils.generateHardwareToken( context );
            if( hardwareToken == null ) {
                ToastMaster.makeText( context, R.string.error_hardware, Toast.LENGTH_LONG ).show();
                finish();
            } else {
                PrefUtils.saveHardwareToken( hardwareToken );
                authenticateUser();
            }
        }
    }

    /**
     * Generates the hardware token after we have the permission
     * and verifies if it is null or not. Null could be caused
     * if the bluetooth is off
     */
    private void authenticateUser() {
        requestManager.invoke(
                new AuthenticateRequest( hardwareToken ),
                new ApiClient.RequestCallback() {
                    @Override
                    public void onResponse( ServerResponse response ) {
                        // Get response code
                        final String code = response.getCode();

                        // Do the correct action
                        switch( code ) {
                            case ServerResponse.AUTHORIZED:
                                if( !PrefUtils.isGCMTokenSent() ) {
                                    // There is no token for GCM
                                    RegistrationIntentService.newInstance( context, hardwareToken );
                                } else {
                                    // Verify if the biometric token has been set
                                    finish();
                                    startNextActivity();
                                }
                                break;

                            case ServerResponse.ERROR_NOT_REGISTERED:
                                // We need to register first
                                Intent intent = new Intent( context, RegistrationActivity.class );
                                startActivity( intent );
                                finish();
                                break;

                            default:
                                ErrorUtils.handleError(
                                        SplashActivity.this,
                                        getString( R.string.error_server ),
                                        true
                                );
                                break;
                        }
                    }

                    @Override
                    public void onError( String message ) {
                        ErrorUtils.handleError(
                                SplashActivity.this,
                                message,
                                true
                        );
                    }
                }
        );
    }

    /**
     * Starts the main window of the YodoPayment
     * application, or the registration of the biometric token
     */
    private void startNextActivity() {
        if( PrefUtils.getAuthNumber() != null ) {
            // The authNumber exists, so the biometric token has not been registered
            Intent intent = new Intent( context, RegistrationActivity.class );
            startActivity( intent );
        }
        else {
            // The token biometric had already been sent, we can continue
            Intent intent = new Intent( context, MainActivity.class );
            startActivity( intent );
        }
    }

    /**
     * Message received from the service that registers the gcm token
     */
    @SuppressWarnings("unused") // receives GCM receipts
    @Subscribe( sticky = true, threadMode = ThreadMode.MAIN )
    public void onResponseEvent( GCMResponse response ) {
        EventBus.getDefault().removeStickyEvent( response );
        boolean sentToken = PrefUtils.isGCMTokenSent();
        if( sentToken ) {
            // The gcm token has been sent
            startNextActivity();
        } else {
            // Something failed
            ErrorUtils.handleError( SplashActivity.this, response.getMessage(), true );
        }
    }
}
