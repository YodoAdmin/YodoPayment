package co.yodo.mobile.main;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
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
    /** The context object */
    private Context ac;

    /** Account identifier */
    private String hardwareToken;

    /** Messages Handler */
    private static YodoHandler handlerMessages;

    /** Request for error Google Play Services */
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );

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
        LocalBroadcastManager.getInstance( this ).unregisterReceiver( mRegistrationBroadcastReceiver );
        super.onPause();
    }

    private void setupGUI() {
        ac = SplashActivity.this;
        handlerMessages = new YodoHandler( SplashActivity.this );
    }

    private void updateData() {
        hardwareToken = AppUtils.getHardwareToken( ac );
        if( hardwareToken == null ) {
            ToastMaster.makeText( ac, R.string.no_hardware, Toast.LENGTH_LONG ).show();
            finish();
        } else if( AppUtils.checkPlayServices( this, PLAY_SERVICES_RESOLUTION_REQUEST ) ) {
            YodoRequest.getInstance().requestAuthentication( SplashActivity.this, hardwareToken );
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

                    if( !authNumber.equals( "" ) ) {
                        Intent intent = new Intent( SplashActivity.this, RegistrationBiometricActivity.class );
                        intent.putExtra( Intents.AUTH_NUMBER, authNumber );
                        startActivity( intent );
                        finish();
                    } else if( isTokenSent ) {
                        Intent intent = new Intent( SplashActivity.this, MainActivity.class );
                        startActivity( intent );
                        finish();
                    } else {
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
            case PLAY_SERVICES_RESOLUTION_REQUEST:
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
