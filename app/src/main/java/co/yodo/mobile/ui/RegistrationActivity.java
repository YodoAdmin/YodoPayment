package co.yodo.mobile.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.yodo.mobile.R;
import co.yodo.mobile.YodoApplication;
import co.yodo.mobile.broadcastreceiver.BroadcastMessage;
import co.yodo.mobile.component.Intents;
import co.yodo.mobile.helper.EulaUtils;
import co.yodo.mobile.helper.GUIUtils;
import co.yodo.mobile.helper.PrefUtils;
import co.yodo.mobile.network.ApiClient;
import co.yodo.mobile.network.model.ServerResponse;
import co.yodo.mobile.network.request.RegisterRequest;
import co.yodo.mobile.service.RegistrationIntentService;
import co.yodo.mobile.service.model.GCMResponse;
import co.yodo.mobile.ui.notification.ProgressDialogHelper;
import co.yodo.mobile.ui.notification.ToastMaster;
import co.yodo.mobile.ui.notification.YodoHandler;
import co.yodo.mobile.ui.validator.PIPValidator;

public class RegistrationActivity extends AppCompatActivity implements EulaUtils.OnEulaAgreedTo, ApiClient.RequestsListener {
    /** DEBUG */
    @SuppressWarnings( "unused" )
    private static final String TAG = RegistrationActivity.class.getSimpleName();

    /** The context object */
    private Context ac;

    /** Account identifier */
    private String mHardwareToken;

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
    PIPValidator mPipValidator;

    /** GUI Controllers */
    @BindView( R.id.etPip )
    EditText etPip;

    @BindView( R.id.etConfirmPip )
    EditText etConfirmPip;

    @BindView( R.id.rlRegistration )
    RelativeLayout rlRegistration;

    /** Response codes for the server requests */
    private static final int REG_REQ = 0x00;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        GUIUtils.setLanguage( RegistrationActivity.this );
        setContentView( R.layout.activity_registration );

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
        ac = RegistrationActivity.this;
        mHandlerMessages = new YodoHandler( this );

        // Injection
        ButterKnife.bind( this );
        YodoApplication.getComponent().inject( this );

        // Register listener for requests
        mRequestManager.setListener( this );

        // Setup the toolbar
        GUIUtils.setActionBar( this, R.string.title_activity_registration );

        // Show the terms to the user
        if( EulaUtils.show( this ) )
            rlRegistration.setVisibility( View.GONE );
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
     * @param v, the view of the checkbox
     */
    public void showPasswordClick( View v ) {
        GUIUtils.showPassword( (CheckBox) v, etPip );
        GUIUtils.showPassword( (CheckBox) v, etConfirmPip );
    }

    /**
     * Realize a registration request
     * @param v View of the button, not used
     */
    public void registrationClick( View v ) {
        // Validates the PIP and its confirmation
        try {
            if( !mPipValidator.validate( etPip ) || !mPipValidator.validate( etConfirmPip ) )
                return;

            if( mPipValidator.validate( etPip, etConfirmPip ) ) {
                // Request an authentication
                final String pip = etPip.getText().toString();

                mProgressManager.createProgressDialog( ac );
                mRequestManager.invoke( new RegisterRequest(
                        REG_REQ,
                        mHardwareToken,
                        pip
                ) );
            }
        } catch( NoSuchFieldException e ) {
            e.printStackTrace();
        }
    }

    @Override
    public void onEulaAgreedTo() {
        rlRegistration.setVisibility( View.VISIBLE );
    }

    @Override
    public void onPrepare() {
    }

    @Override
    public void onResponse( int responseCode, ServerResponse response ) {
        mProgressManager.destroyProgressDialog();
        String code, message;

        switch( responseCode ) {
            case REG_REQ:
                code = response.getCode();

                // If the auth is correct, let's continue with the registration
                if( code.equals( ServerResponse.AUTHORIZED_REGISTRATION ) ) {
                    // Save the authnumber for later use (i.e. biometric registration)
                    PrefUtils.saveAuthNumber( ac, response.getAuthNumber() );

                    // let's register the gcm id
                    Intent intent = new Intent( ac, RegistrationIntentService.class );
                    intent.putExtra( BroadcastMessage.EXTRA_HARDWARE_TOKEN, mHardwareToken );
                    startService( intent );
                }
                // There was an error during the process
                else {
                    message = response.getMessage();
                    YodoHandler.sendMessage( mHandlerMessages, code, message );
                }

                break;
        }
    }

    /**
     * Message received from the service that registers the gcm token
     */
    @SuppressWarnings("unused")
    @Subscribe( sticky = true, threadMode = ThreadMode.MAIN )
    public void onResponseEvent( GCMResponse response ) {
        EventBus.getDefault().removeStickyEvent( response );
        boolean sentToken = PrefUtils.isGCMTokenSent( ac );
        finish();
        // The gcm token has been sent
        if( sentToken ) {
            // Now we have to register the biometric token
            final String authNumber = PrefUtils.getAuthNumber( ac );
            Intent intent = new Intent( ac, RegistrationBiometricActivity.class );
            intent.putExtra( Intents.AUTH_NUMBER, authNumber );
            startActivity( intent );
        }
        // Something failed
        else {
            Toast.makeText( ac, R.string.error_gcm_registration, Toast.LENGTH_SHORT ).show();
        }
    }
}
