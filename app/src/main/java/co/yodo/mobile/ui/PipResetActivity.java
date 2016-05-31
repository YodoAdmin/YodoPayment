package co.yodo.mobile.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import co.yodo.mobile.R;
import co.yodo.mobile.ui.notification.ProgressDialogHelper;
import co.yodo.mobile.ui.notification.ToastMaster;
import co.yodo.mobile.ui.notification.YodoHandler;
import co.yodo.mobile.network.model.ServerResponse;
import co.yodo.mobile.helper.AppConfig;
import co.yodo.mobile.helper.AppUtils;
import co.yodo.mobile.helper.Intents;
import co.yodo.mobile.network.YodoRequest;

public class PipResetActivity extends AppCompatActivity implements YodoRequest.RESTListener {
    /** DEBUG */
    @SuppressWarnings( "unused" )
    private static final String TAG = PipResetActivity.class.getSimpleName();

    /** The context object */
    private Context ac;

    /** Account identifiers */
    private String hardwareToken;
    private String authNumber;

    /** Messages Handler */
    private static YodoHandler handlerMessages;

    /** Manager for the server requests */
    private YodoRequest mRequestManager;

    /** GUI Controllers */
    private EditText etCurrentPip;
    private EditText etNewPip;
    private EditText etConfirmPip;

    /** The shake animation for wrong inputs */
    private Animation aShake;

    /** Activity Result */
    private static final int REQUEST_FACE_ACTIVITY = 0;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        AppUtils.setLanguage( this );
        setContentView( R.layout.activity_pip_reset );

        setupGUI();
        updateData();

        if( savedInstanceState != null && savedInstanceState.getBoolean( AppConfig.IS_SHOWING ) ) {
            ProgressDialogHelper.getInstance().createProgressDialog( ac );
        }
    }

    @Override
    public void onSaveInstanceState( Bundle outState ) {
        super.onSaveInstanceState( outState );
        outState.putBoolean(
                AppConfig.IS_SHOWING,
                ProgressDialogHelper.getInstance().isProgressDialogShowing()
        );
    }

    @Override
    public void onResume() {
        super.onResume();
        mRequestManager.setListener( this );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ProgressDialogHelper.getInstance().destroyProgressDialog();
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
        ac = PipResetActivity.this;
        handlerMessages = new YodoHandler( PipResetActivity.this );
        mRequestManager = YodoRequest.getInstance( ac );

        // GUI Global components
        etCurrentPip = (EditText) findViewById( R.id.currentPipText );
        etNewPip     = (EditText) findViewById( R.id.newPipText );
        etConfirmPip = (EditText) findViewById( R.id.confirmPipText );

        // Load the animation
        aShake = AnimationUtils.loadAnimation( this, R.anim.shake );

        // Only used at creation
        Toolbar toolbar = (Toolbar) findViewById( R.id.actionBar );

        // Setup the toolbar
        setSupportActionBar( toolbar );
        ActionBar actionBar = getSupportActionBar();
        if( actionBar != null )
            actionBar.setDisplayHomeAsUpEnabled( true );
    }

    /**
     * Sets the main values
     */
    private void updateData() {
        // Gets the hardware token - account identifier
        hardwareToken = AppUtils.getHardwareToken( ac );
        if( hardwareToken == null ) {
            ToastMaster.makeText( ac, R.string.message_no_hardware, Toast.LENGTH_LONG ).show();
            finish();
        }
    }

    /**
     * Show the passwords
     * @param v The view of the checkbox
     */
    public void showPasswordClick( View v ) {
        AppUtils.showPassword( (CheckBox) v, etCurrentPip);
        AppUtils.showPassword( (CheckBox) v, etNewPip );
        AppUtils.showPassword( (CheckBox) v, etConfirmPip );
    }

    /**
     * Action that validates the input of the PIP,
     * and starts the reset PIP process
     * @param v The view of the button
     */
    public void resetPipClick( View v ) {
        final String currentPip = etCurrentPip.getText().toString();
        final String newPip     = etNewPip.getText().toString();
        final String confirmPip = etConfirmPip.getText().toString();

        if( currentPip.length() < AppConfig.MIN_PIP_LENGTH ) {
            ToastMaster.makeText( ac, R.string.pip_short, Toast.LENGTH_SHORT ).show();
            etCurrentPip.startAnimation( aShake );
        }
        else if( newPip.length() < AppConfig.MIN_PIP_LENGTH ) {
            ToastMaster.makeText( ac, R.string.pip_short, Toast.LENGTH_SHORT ).show();
            etNewPip.startAnimation( aShake );
        }
        else if( !newPip.equals( confirmPip ) ) {
            ToastMaster.makeText( ac, R.string.pip_different, Toast.LENGTH_SHORT ).show();
            etConfirmPip.startAnimation( aShake );
        } else {
            AppUtils.hideSoftKeyboard( this );

            ProgressDialogHelper.getInstance().createProgressDialog( ac );
            mRequestManager.requestClientAuth(
                    hardwareToken,
                    currentPip
            );
        }
    }

    /**
     * Action in case the user forgot the PIP,
     * here the biometric token is used
     * @param v The view of the button
     */
    public void forgotPipClick( View v ) {
        final String newPip     = etNewPip.getText().toString();
        final String confirmPip = etConfirmPip.getText().toString();

        if( newPip.length() < AppConfig.MIN_PIP_LENGTH ) {
            ToastMaster.makeText( ac, R.string.pip_short, Toast.LENGTH_SHORT ).show();
            etNewPip.startAnimation( aShake );
        }
        else if( !newPip.equals( confirmPip ) ) {
            ToastMaster.makeText( ac, R.string.pip_different, Toast.LENGTH_SHORT ).show();
            etConfirmPip.startAnimation( aShake );
        } else {
            AppUtils.hideSoftKeyboard( this );

            ProgressDialogHelper.getInstance().createProgressDialog( ac );
            mRequestManager.requestBiometricToken(
                    hardwareToken
            );
        }
    }

    @Override
    public void onResponse( YodoRequest.RequestType type, ServerResponse response ) {
        ProgressDialogHelper.getInstance().destroyProgressDialog();
        String code, message;

        switch( type ) {
             case AUTH_PIP_REQUEST:
                 code = response.getCode();

                 // If the auth is correct, let's change the password
                 if( code.equals( ServerResponse.AUTHORIZED ) ) {
                     final String currentPip = etCurrentPip.getText().toString();
                     final String newPip     = etNewPip.getText().toString();

                     ProgressDialogHelper.getInstance().createProgressDialog( ac );
                     mRequestManager.requestPIPReset(
                             hardwareToken,
                             currentPip,
                             newPip
                     );
                 // There was an error during the process
                 } else {
                     message  = response.getMessage();
                     AppUtils.sendMessage( handlerMessages, code, message );
                 }
                 break;

            case RESET_BIO_PIP_REQUEST:
            case RESET_PIP_REQUEST:
                code = response.getCode();

                // The PIP was successfully changed, let's return to the main activity
                if( code.equals( ServerResponse.AUTHORIZED ) ) {
                    ToastMaster.makeText( ac, R.string.change_successful, Toast.LENGTH_LONG ).show();
                    finish();
                // There was an error during the process
                } else {
                    message  = response.getMessage();
                    AppUtils.sendMessage( handlerMessages, code, message );
                }

                break;

            case QUERY_BIO_REQUEST:
                code = response.getCode();

                // We received the biometric token from the server
                if( code.equals( ServerResponse.AUTHORIZED ) ) {
                    String biometricToken = response.getParam( ServerResponse.BIOMETRIC );
                    // The user has a biometric token
                    if( !biometricToken.equals( AppConfig.YODO_BIOMETRIC ) ) {
                        authNumber = response.getAuthNumber();
                        // Start the recognition activity
                        Intent intent = new Intent( PipResetActivity.this, CameraActivity.class );
                        intent.putExtra( Intents.BIOMETRIC_TOKEN, biometricToken );
                        startActivityForResult( intent, REQUEST_FACE_ACTIVITY );
                    // The user doesn't have a biometric token
                    } else {
                        AppUtils.sendMessage(
                                handlerMessages,
                                ServerResponse.ERROR_FAILED,
                                getString( R.string.no_biometric )
                        );
                    }
                // There was an error during the process
                } else {
                    message  = response.getMessage();
                    AppUtils.sendMessage( handlerMessages, code, message );
                }

                break;
        }
    }

    @Override
    protected void onActivityResult( int requestCode, int resultCode, Intent data ) {
        switch( requestCode ) {
            case REQUEST_FACE_ACTIVITY:
                // Successful recognition, let's change the PIP
                if( resultCode == RESULT_OK ) {
                    final String newPip = etNewPip.getText().toString();

                    ProgressDialogHelper.getInstance().createProgressDialog( ac );
                    mRequestManager.requestBiometricPIPReset(
                            authNumber,
                            hardwareToken,
                            newPip
                    );
                }
                break;
        }
    }
}
