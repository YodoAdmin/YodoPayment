package co.yodo.mobile.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
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

public class RegistrationBiometricActivity extends AppCompatActivity implements YodoRequest.RESTListener {
    /** The context object */
    private Context ac;

    /** Messages Handler */
    private static YodoHandler handlerMessages;

    /** AuthNumber from the registration */
    private String authNumber;

    /** Biometric identifier */
    private String biometricToken;

    /** Result Activities Identifiers */
    private static final int CAMERA_ACTIVITY = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppUtils.setLanguage( RegistrationBiometricActivity.this );
        setContentView(R.layout.activity_registration_biometric);

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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch( itemId ) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupGUI() {
        ac = RegistrationBiometricActivity.this;
        handlerMessages = new YodoHandler( RegistrationBiometricActivity.this );

        // Only used at creation
        Toolbar mActionBarToolbar = (Toolbar) findViewById( R.id.actionBar );

        setSupportActionBar( mActionBarToolbar );
        if( getSupportActionBar() != null )
            getSupportActionBar().setDisplayHomeAsUpEnabled( true );
    }

    private void updateData() {
        Bundle bundle = getIntent().getExtras();
        if( bundle != null ) {
            authNumber = bundle.getString( Intents.AUTH_NUMBER );
        }

        if( authNumber == null ) {
            ToastMaster.makeText( ac, R.string.error_registration, Toast.LENGTH_SHORT ).show();
            finish();
        }
    }

    /**
     * Starts the face biometric procedure
     * @param v View of the button, not used
     */
    public void faceBiometricClicked(View v) {
        Intent intent = new Intent( RegistrationBiometricActivity.this, CameraActivity.class );
        startActivityForResult(intent, CAMERA_ACTIVITY);
    }

    /**
     * Realize a registration request
     * @param v View of the button, not used
     */
    public void registrationClick(View v) {
        if( biometricToken != null ) {
            YodoRequest.getInstance().createProgressDialog(
                    RegistrationBiometricActivity.this ,
                    YodoRequest.ProgressDialogType.NORMAL
            );

            YodoRequest.getInstance().requestBiometricRegistration(
                    RegistrationBiometricActivity.this,
                    authNumber,
                    biometricToken
            );
        } else {
            Animation shake = AnimationUtils.loadAnimation( this, R.anim.shake );
            ToastMaster.makeText( ac, R.string.face_required , Toast.LENGTH_SHORT ).show();
            ImageView faceView = (ImageView) findViewById( R.id.faceView );
            faceView.startAnimation( shake );
        }
    }

    @Override
    public void onResponse(YodoRequest.RequestType type, ServerResponse response) {
        YodoRequest.getInstance().destroyProgressDialog();
        String code, message;

        switch( type ) {
            case ERROR_NO_INTERNET:
                handlerMessages.sendEmptyMessage( YodoHandler.NO_INTERNET );
                break;

            case ERROR_GENERAL:
                handlerMessages.sendEmptyMessage( YodoHandler.GENERAL_ERROR );
                break;

            case REG_BIO_REQUEST:
                code = response.getCode();

                if( code.equals( ServerResponse.AUTHORIZED ) ) {
                    AppUtils.saveAuthNumber( ac, "" );
                    String hardwareToken = AppUtils.getHardwareToken( ac );

                    // Start IntentService to register this application with GCM.
                    Intent intent = new Intent( this, RegistrationIntentService.class );
                    intent.putExtra( BroadcastMessage.EXTRA_HARDWARE_TOKEN, hardwareToken );
                    startService( intent );

                    /*Intent intent = new Intent( RegistrationBiometricActivity.this, MainActivity.class );
                    startActivity( intent );
                    finish();*/
                } else {
                    message  = response.getMessage();
                    AppUtils.sendMessage( handlerMessages, code, message );
                }

                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch( requestCode ) {
            case( CAMERA_ACTIVITY ) :
                if( resultCode == RESULT_OK ) {
                    biometricToken = data.getStringExtra( Intents.RESULT_FACE );
                    ToastMaster.makeText( ac, R.string.face_trained, Toast.LENGTH_LONG ).show();
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
                intent = new Intent( RegistrationBiometricActivity.this, MainActivity.class );
                startActivity( intent );
            } else {
                Toast.makeText( ac, R.string.error_gcm_registration, Toast.LENGTH_SHORT ).show();
            }
        }
    };
}
