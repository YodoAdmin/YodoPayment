package co.yodo.mobile.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import co.yodo.mobile.component.Intents;
import co.yodo.mobile.helper.GUIUtils;
import co.yodo.mobile.helper.PrefUtils;
import co.yodo.mobile.helper.SystemUtils;
import co.yodo.mobile.network.YodoRequest;
import co.yodo.mobile.network.model.ServerResponse;
import co.yodo.mobile.network.request.RegisterRequest;
import co.yodo.mobile.ui.notification.ProgressDialogHelper;
import co.yodo.mobile.ui.notification.ToastMaster;
import co.yodo.mobile.ui.notification.YodoHandler;

public class RegistrationBiometricActivity extends AppCompatActivity implements YodoRequest.RESTListener {
    /** The context object */
    private Context ac;

    /** Account identifiers */
    private String authNumber;
    private String biometricToken;

    /** Messages Handler */
    private YodoHandler handlerMessages;

    /** Manager for the server requests */
    private YodoRequest mRequestManager;

    /** GUI Controllers */
    private ImageView imFaceBiometric;

    /** The shake animation for wrong inputs */
    private Animation aShake;

    /** Result Activities Identifiers */
    private static final int RESULT_CAMERA_ACTIVITY = 1;

    /** Request codes for the permissions */
    private static final int PERMISSIONS_REQUEST_CAMERA = 1;

    /** Response codes for the server requests */
    private static final int REG_REQ = 0x00;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        GUIUtils.setLanguage( RegistrationBiometricActivity.this );
        setContentView( R.layout.activity_registration_biometric );

        setupGUI();
        updateData();
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

        // Sets the messages handler and the request manager
        handlerMessages = new YodoHandler( RegistrationBiometricActivity.this );
        mRequestManager = YodoRequest.getInstance( ac );
        mRequestManager.setListener( this );

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
        boolean cameraPermission = SystemUtils.requestPermission(
                RegistrationBiometricActivity.this,
                R.string.message_permission_camera,
                Manifest.permission.CAMERA,
                PERMISSIONS_REQUEST_CAMERA
        );

        if( cameraPermission )
            showCamera();
    }

    /**
     * Realize a registration request
     * @param v View of the button, not used
     */
    public void registrationClick( View v ) {
        // We have a biometric token, we can proceed
        if( biometricToken != null ) {
            ProgressDialogHelper.getInstance().createProgressDialog( ac );
            mRequestManager.invoke( new RegisterRequest(
                    REG_REQ,
                    authNumber,
                    biometricToken,
                    RegisterRequest.RegST.BIOMETRIC
            ) );
        // The user needs to capture a biometric token first
        } else {
            ToastMaster.makeText( ac, R.string.face_required , Toast.LENGTH_SHORT ).show();
            imFaceBiometric.startAnimation( aShake );
        }
    }

    /**
     * Starts the face recognition activity
     */
    private void showCamera() {
        Intent intent = new Intent( ac, CameraActivity.class );
        startActivityForResult( intent, RESULT_CAMERA_ACTIVITY );
    }

    @Override
    public void onPrepare() {
    }

    @Override
    public void onResponse( int requestCode, ServerResponse response ) {
        ProgressDialogHelper.getInstance().destroyProgressDialog();
        String code, message;

        switch( requestCode ) {
            case REG_REQ:
                code = response.getCode();

                // Successfully register the biometric token
                if( code.equals( ServerResponse.AUTHORIZED ) ) {
                    PrefUtils.saveAuthNumber( ac, "" );

                    Intent intent = new Intent( ac, MainActivity.class );
                    startActivity( intent );
                    finish();
                }
                // There was an error during the process
                else {
                    message  = response.getMessage();
                    YodoHandler.sendMessage( handlerMessages, code, message );
                }

                break;
        }
    }

    @Override
    public void onActivityResult( int requestCode, int resultCode, Intent data ) {
        super.onActivityResult( requestCode, resultCode, data );
        switch( requestCode ) {
            case( RESULT_CAMERA_ACTIVITY ) :
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
}
