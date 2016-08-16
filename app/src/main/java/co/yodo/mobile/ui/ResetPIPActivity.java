package co.yodo.mobile.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.yodo.mobile.R;
import co.yodo.mobile.YodoApplication;
import co.yodo.mobile.component.Intents;
import co.yodo.mobile.helper.AppConfig;
import co.yodo.mobile.helper.GUIUtils;
import co.yodo.mobile.helper.PrefUtils;
import co.yodo.mobile.network.ApiClient;
import co.yodo.mobile.network.model.ServerResponse;
import co.yodo.mobile.network.request.QueryRequest;
import co.yodo.mobile.network.request.ResetPIPRequest;
import co.yodo.mobile.ui.notification.ProgressDialogHelper;
import co.yodo.mobile.ui.notification.ToastMaster;
import co.yodo.mobile.ui.notification.YodoHandler;
import co.yodo.mobile.ui.option.ResetPIPOption;
import co.yodo.mobile.ui.validator.PIPValidator;

public class ResetPIPActivity extends AppCompatActivity implements ApiClient.RequestsListener {
    /** DEBUG */
    @SuppressWarnings( "unused" )
    private static final String TAG = ResetPIPActivity.class.getSimpleName();

    /** The context object */
    private Context ac;

    /** Account identifiers */
    private String mHardwareToken;
    private String mAuthNumber;

    /** Messages Handler */
    private YodoHandler mHandlerMessages;

    /** Manager for the server requests */
    @Inject
    ApiClient mRequestManager;

    /** Progress dialog for the requests */
    @Inject
    ProgressDialogHelper mProgressManager;

    /** PIP validator */
    @Inject
    protected PIPValidator mPipValidator;

    /** GUI Controllers */
    @BindView( R.id.etNewPip )
    EditText etNewPip;

    @BindView( R.id.etConfirmPip )
    EditText etConfirmPip;

    /** Options that executes a request */
    private ResetPIPOption mResetOption;

    /** Activity Result */
    private static final int REQUEST_FACE_ACTIVITY = 0;

    /** Response codes for the server requests */
    private static final int RESET_REQ = 0x00;
    private static final int QUERY_REQ = 0x01;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        GUIUtils.setLanguage( this );
        setContentView( R.layout.activity_pip_reset );

        setupGUI();
        updateData();

        if( savedInstanceState != null && savedInstanceState.getBoolean( AppConfig.IS_SHOWING ) ) {
            mProgressManager.createProgressDialog( ac );
        }
    }

    @Override
    public void onSaveInstanceState( Bundle outState ) {
        super.onSaveInstanceState( outState );
        outState.putBoolean(
                AppConfig.IS_SHOWING,
                mProgressManager.isProgressDialogShowing()
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
        mProgressManager.destroyProgressDialog();
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
        ac = ResetPIPActivity.this;
        mHandlerMessages = new YodoHandler( this );

        // Injection
        ButterKnife.bind( this );
        YodoApplication.getComponent().inject( this );

        // Request options
        mResetOption = new ResetPIPOption( this, mHandlerMessages );

        // Setup the toolbar
        GUIUtils.setActionBar( this, R.string.title_activity_pip_reset );
    }

    /**
     * Sets the main values
     */
    private void updateData() {
        // Gets the hardware token - account identifier
        mHardwareToken = PrefUtils.getHardwareToken( ac );
        if( mHardwareToken == null ) {
            ToastMaster.makeText( ac, R.string.message_no_hardware, Toast.LENGTH_LONG ).show();
            finish();
        }
    }

    /**
     * Show the passwords
     * @param v The view of the checkbox
     */
    public void showPasswordClick( View v ) {
        GUIUtils.showPassword( (CheckBox) v, etNewPip );
        GUIUtils.showPassword( (CheckBox) v, etConfirmPip );
    }

    /**
     * Verifies that both pips are correct
     * @return Boolean True or false, if correct or not
     */
    private boolean verifyPIPs() throws NoSuchFieldException {
        return !( !mPipValidator.validate( etNewPip ) || !mPipValidator.validate( etConfirmPip ) )
                && mPipValidator.validate( etNewPip, etConfirmPip );
    }

    /**
     * Action that validates the input of the PIP,
     * and starts the reset PIP process
     * @param v The view of the button
     */
    public void resetPipClick( View v ) {
        try {
            if( verifyPIPs() )
                mResetOption.execute();
        } catch( NoSuchFieldException e ) {
            e.printStackTrace();
        }
    }

    public void doReset( String pip ) {
        final String newPip = etNewPip.getText().toString();

        mProgressManager.createProgressDialog( ac );
        mRequestManager.invoke( new ResetPIPRequest(
                RESET_REQ,
                mHardwareToken,
                pip,
                newPip
        ) );
    }

    /**
     * Action in case the user forgot the PIP,
     * here the biometric token is used
     * @param v The view of the button
     */
    public void forgotPipClick( View v ) {
        // Validates the new PIP and its confirmation
        try {
            if( verifyPIPs() ) {
                // Request the biometric token
                mProgressManager.createProgressDialog( ac );
                mRequestManager.invoke( new QueryRequest(
                        QUERY_REQ,
                        mHardwareToken,
                        QueryRequest.Record.BIOMETRIC
                ) );
            }
        } catch( NoSuchFieldException e ) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPrepare() {
    }

    @Override
    public void onResponse( int requestCode, ServerResponse response ) {
        mProgressManager.destroyProgressDialog();
        String code, message;

        switch( requestCode ) {
            case RESET_REQ:
                code = response.getCode();

                // The PIP was successfully changed, let's return to the main activity
                if( code.equals( ServerResponse.AUTHORIZED ) ) {
                    ToastMaster.makeText( ac, R.string.change_successful, Toast.LENGTH_LONG ).show();
                    finish();
                // There was an error during the process
                } else {
                    message = response.getMessage();
                    YodoHandler.sendMessage( mHandlerMessages, code, message );
                }

                break;

            case QUERY_REQ:
                code = response.getCode();

                // We received the biometric token from the server
                if( code.equals( ServerResponse.AUTHORIZED ) ) {
                    String biometricToken = response.getParam( ServerResponse.BIOMETRIC );
                    // The user has a biometric token
                    if( !biometricToken.equals( AppConfig.YODO_BIOMETRIC ) ) {
                        mAuthNumber = response.getAuthNumber();
                        // Start the recognition activity
                        Intent intent = new Intent( ac, CameraActivity.class );
                        intent.putExtra( Intents.BIOMETRIC_TOKEN, biometricToken );
                        startActivityForResult( intent, REQUEST_FACE_ACTIVITY );
                    // The user doesn't have a biometric token
                    } else {
                        YodoHandler.sendMessage(
                                mHandlerMessages,
                                ServerResponse.ERROR_FAILED,
                                getString( R.string.no_biometric )
                        );
                    }
                // There was an error during the process
                } else {
                    message = response.getMessage();
                    YodoHandler.sendMessage( mHandlerMessages, code, message );
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

                    mProgressManager.createProgressDialog( ac );
                    mRequestManager.invoke( new ResetPIPRequest(
                            RESET_REQ,
                            mHardwareToken,
                            mAuthNumber,
                            newPip,
                            ResetPIPRequest.ResetST.PIP_BIO
                    ) );
                }
                break;
        }
    }
}
