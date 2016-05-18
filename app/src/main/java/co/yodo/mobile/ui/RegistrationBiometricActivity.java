package co.yodo.mobile.ui;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
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
import co.yodo.mobile.ui.component.ProgressDialogHelper;
import co.yodo.mobile.ui.component.ToastMaster;
import co.yodo.mobile.ui.component.YodoHandler;
import co.yodo.mobile.network.model.ServerResponse;
import co.yodo.mobile.helper.AppConfig;
import co.yodo.mobile.helper.AppUtils;
import co.yodo.mobile.helper.Intents;
import co.yodo.mobile.network.YodoRequest;
import co.yodo.mobile.service.RegistrationIntentService;

public class RegistrationBiometricActivity extends AppCompatActivity implements YodoRequest.RESTListener {
    /** The context object */
    private Context ac;

    /** Account identifiers */
    private String authNumber;
    private String biometricToken;

    /** Messages Handler */
    private static YodoHandler handlerMessages;

    /** Manager for the server requests */
    private YodoRequest mRequestManager;

    /** GUI Controllers */
    private ImageView imFaceBiometric;

    /** The shake animation for wrong inputs */
    private Animation aShake;

    /** Result Activities Identifiers */
    private static final int CAMERA_ACTIVITY = 1;

    /** Request codes for the permissions */
    private static final int PERMISSIONS_REQUEST_CAMERA = 1;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        AppUtils.setLanguage( RegistrationBiometricActivity.this );
        setContentView( R.layout.activity_registration_biometric );

        setupGUI();
        updateData();
    }

    @Override
    public void onResume() {
        super.onResume();
        mRequestManager.setListener( this );
        LocalBroadcastManager.getInstance( this ).registerReceiver(
                mRegistrationBroadcastReceiver,
                new IntentFilter( AppConfig.REGISTRATION_COMPLETE )
        );
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance( this ).unregisterReceiver(
                mRegistrationBroadcastReceiver
        );
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item ) {
        int itemId = item.getItemId();
        switch( itemId ) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected( item );
    }

    /**
     * Configures the main GUI Controllers
     */
    private void setupGUI() {
        // Get the context
        ac = RegistrationBiometricActivity.this;
        handlerMessages = new YodoHandler( RegistrationBiometricActivity.this );
        mRequestManager = YodoRequest.getInstance( ac );

        // GUI Global components
        imFaceBiometric = (ImageView) findViewById( R.id.faceView );

        // Load the animation
        aShake = AnimationUtils.loadAnimation( this, R.anim.shake );

        // Only used at creation
        Toolbar mActionBarToolbar = (Toolbar) findViewById( R.id.actionBar );

        setSupportActionBar( mActionBarToolbar );
        ActionBar actionBar = getSupportActionBar();
        if( actionBar != null )
            actionBar.setDisplayHomeAsUpEnabled( true );
    }

    /**
     * Sets the main values
     */
    private void updateData() {
        Bundle bundle = getIntent().getExtras();
        if( bundle != null ) {
            authNumber = bundle.getString( Intents.AUTH_NUMBER );
        }

        // If we don't have an authNumber, then error registration
        if( authNumber == null ) {
            ToastMaster.makeText( ac, R.string.error_registration, Toast.LENGTH_SHORT ).show();
            finish();
        }
    }

    /**
     * Starts the face biometric procedure
     * @param v View of the button, not used
     */
    public void faceBiometricClicked( View v ) {
        boolean cameraPermission = AppUtils.requestPermission(
                RegistrationBiometricActivity.this,
                R.string.message_permission_camera,
                Manifest.permission.CAMERA,
                PERMISSIONS_REQUEST_CAMERA
        );

        if( cameraPermission )
            showCamera();
    }

    /**
     * Starts the face recognition activity
     */
    private void showCamera() {
        Intent intent = new Intent( RegistrationBiometricActivity.this, CameraActivity.class );
        startActivityForResult( intent, CAMERA_ACTIVITY );
    }

    /**
     * Realize a registration request
     * @param v View of the button, not used
     */
    public void registrationClick( View v ) {
        // We have a biometric token, we can proceed
        if( biometricToken != null ) {
            ProgressDialogHelper.getInstance().createProgressDialog( ac );
            mRequestManager.requestBiometricRegistration(
                    authNumber,
                    biometricToken
            );
        // The user needs to capture a biometric token first
        } else {
            ToastMaster.makeText( ac, R.string.face_required , Toast.LENGTH_SHORT ).show();
            imFaceBiometric.startAnimation( aShake );
        }
    }

    @Override
    public void onResponse( YodoRequest.RequestType type, ServerResponse response ) {
        ProgressDialogHelper.getInstance().destroyProgressDialog();
        String code, message;

        switch( type ) {
            case REG_BIO_REQUEST:
                code = response.getCode();

                // Successfully register the biometric token
                if( code.equals( ServerResponse.AUTHORIZED ) ) {
                    AppUtils.saveAuthNumber( ac, "" );
                    String hardwareToken = AppUtils.getHardwareToken( ac );

                    // Start IntentService to register this application with GCM.
                    Intent intent = new Intent( this, RegistrationIntentService.class );
                    intent.putExtra( BroadcastMessage.EXTRA_HARDWARE_TOKEN, hardwareToken );
                    startService( intent );
                // There was an error during the process
                } else {
                    message  = response.getMessage();
                    AppUtils.sendMessage( handlerMessages, code, message );
                }

                break;
        }
    }

    @Override
    public void onActivityResult( int requestCode, int resultCode, Intent data ) {
        super.onActivityResult( requestCode, resultCode, data );
        switch( requestCode ) {
            case( CAMERA_ACTIVITY ) :
                // Just trained the biometric recognition, let's save the token
                if( resultCode == RESULT_OK ) {
                    biometricToken = data.getStringExtra( Intents.RESULT_FACE );
                    ToastMaster.makeText( ac, R.string.face_trained, Toast.LENGTH_LONG ).show();
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult( int requestCode, @NonNull String permissions[], @NonNull int[] grantResults ) {
        switch( requestCode ) {
            // Permission for the camera
            case PERMISSIONS_REQUEST_CAMERA:
                // If request is cancelled, the result arrays are empty.
                if( grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED ) {
                    // Permission Granted
                    showCamera();
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
                intent = new Intent( RegistrationBiometricActivity.this, MainActivity.class );
                startActivity( intent );
            } else {
                Toast.makeText( ac, R.string.error_gcm_registration, Toast.LENGTH_SHORT ).show();
            }
        }
    };
}
