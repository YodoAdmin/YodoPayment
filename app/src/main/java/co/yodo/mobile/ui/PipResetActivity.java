package co.yodo.mobile.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import co.yodo.mobile.R;
import co.yodo.mobile.helper.GUIUtils;
import co.yodo.mobile.network.request.AuthenticateRequest;
import co.yodo.mobile.network.request.QueryRequest;
import co.yodo.mobile.network.request.ResetPIPRequest;
import co.yodo.mobile.ui.notification.ProgressDialogHelper;
import co.yodo.mobile.ui.notification.ToastMaster;
import co.yodo.mobile.ui.notification.YodoHandler;
import co.yodo.mobile.network.model.ServerResponse;
import co.yodo.mobile.helper.AppConfig;
import co.yodo.mobile.helper.PrefUtils;
import co.yodo.mobile.component.Intents;
import co.yodo.mobile.network.YodoRequest;
import co.yodo.mobile.ui.validator.PIPValidator;
import co.yodo.mobile.ui.validator.ValidatorFactory;

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
    private YodoHandler handlerMessages;

    /** Manager for the server requests */
    private YodoRequest mRequestManager;

    /** GUI Controllers */
    private EditText etCurrentPip;
    private EditText etNewPip;
    private EditText etConfirmPip;

    /** Activity Result */
    private static final int REQUEST_FACE_ACTIVITY = 0;

    /** Response codes for the server requests */
    private static final int AUTH_REQ  = 0x00;
    private static final int RESET_REQ = 0x01;
    private static final int QUERY_REQ = 0x02;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        GUIUtils.setLanguage( this );
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
        hardwareToken = PrefUtils.getHardwareToken( ac );
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
        GUIUtils.showPassword( (CheckBox) v, etCurrentPip);
        GUIUtils.showPassword( (CheckBox) v, etNewPip );
        GUIUtils.showPassword( (CheckBox) v, etConfirmPip );
    }

    /**
     * Action that validates the input of the PIP,
     * and starts the reset PIP process
     * @param v The view of the button
     */
    public void resetPipClick( View v ) {
        GUIUtils.hideSoftKeyboard( this );

        // Validates the current PIP format
        PIPValidator validator = ValidatorFactory.getValidator( etCurrentPip );
        if( validator.validate() ) {
            // Validates the new PIP and its confirmation
            validator = ValidatorFactory.getValidator( etNewPip, etConfirmPip );
            if( validator.validate() ) {
                // Request an authentication
                final String currentPip = etCurrentPip.getText().toString();

                ProgressDialogHelper.getInstance().createProgressDialog( ac );
                mRequestManager.invoke( new AuthenticateRequest(
                        AUTH_REQ,
                        hardwareToken,
                        currentPip
                ) );
            }
        }
    }

    /**
     * Action in case the user forgot the PIP,
     * here the biometric token is used
     * @param v The view of the button
     */
    public void forgotPipClick( View v ) {
        GUIUtils.hideSoftKeyboard( this );

        // Validates the new PIP and its confirmation
        PIPValidator validator = ValidatorFactory.getValidator( etNewPip, etConfirmPip );
        if( validator.validate() ) {
            // Request the biometric token
            ProgressDialogHelper.getInstance().createProgressDialog( ac );
            mRequestManager.invoke( new QueryRequest(
                    QUERY_REQ,
                    hardwareToken,
                    QueryRequest.Record.BIOMETRIC
            ) );
        }
    }

    @Override
    public void onResponse( int requestCode, ServerResponse response ) {
        ProgressDialogHelper.getInstance().destroyProgressDialog();
        String code, message;

        switch( requestCode ) {
             case AUTH_REQ:
                 code = response.getCode();

                 // If the auth is correct, let's change the password
                 if( code.equals( ServerResponse.AUTHORIZED ) ) {
                     final String currentPip = etCurrentPip.getText().toString();
                     final String newPip     = etNewPip.getText().toString();

                     ProgressDialogHelper.getInstance().createProgressDialog( ac );
                     mRequestManager.invoke( new ResetPIPRequest(
                             RESET_REQ,
                             hardwareToken,
                             currentPip,
                             newPip
                     ) );
                 // There was an error during the process
                 } else {
                     message = response.getMessage();
                     YodoHandler.sendMessage( handlerMessages, code, message );
                 }
                 break;

            case RESET_REQ:
                code = response.getCode();

                // The PIP was successfully changed, let's return to the main activity
                if( code.equals( ServerResponse.AUTHORIZED ) ) {
                    ToastMaster.makeText( ac, R.string.change_successful, Toast.LENGTH_LONG ).show();
                    finish();
                // There was an error during the process
                } else {
                    message = response.getMessage();
                    YodoHandler.sendMessage( handlerMessages, code, message );
                }

                break;

            case QUERY_REQ:
                code = response.getCode();

                // We received the biometric token from the server
                if( code.equals( ServerResponse.AUTHORIZED ) ) {
                    String biometricToken = response.getParam( ServerResponse.BIOMETRIC );
                    // The user has a biometric token
                    if( !biometricToken.equals( AppConfig.YODO_BIOMETRIC ) ) {
                        authNumber = response.getAuthNumber();
                        // Start the recognition activity
                        Intent intent = new Intent( ac, CameraActivity.class );
                        intent.putExtra( Intents.BIOMETRIC_TOKEN, biometricToken );
                        startActivityForResult( intent, REQUEST_FACE_ACTIVITY );
                    // The user doesn't have a biometric token
                    } else {
                        YodoHandler.sendMessage(
                                handlerMessages,
                                ServerResponse.ERROR_FAILED,
                                getString( R.string.no_biometric )
                        );
                    }
                // There was an error during the process
                } else {
                    message = response.getMessage();
                    YodoHandler.sendMessage( handlerMessages, code, message );
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
                    mRequestManager.invoke( new ResetPIPRequest(
                            RESET_REQ,
                            hardwareToken,
                            authNumber,
                            newPip,
                            ResetPIPRequest.ResetST.PIP_BIO
                    ) );
                }
                break;
        }
    }
}
