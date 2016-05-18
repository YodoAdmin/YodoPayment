package co.yodo.mobile.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import co.yodo.mobile.R;
import co.yodo.mobile.ui.component.ProgressDialogHelper;
import co.yodo.mobile.ui.component.ToastMaster;
import co.yodo.mobile.ui.component.YodoHandler;
import co.yodo.mobile.network.model.ServerResponse;
import co.yodo.mobile.helper.AppConfig;
import co.yodo.mobile.helper.AppEula;
import co.yodo.mobile.helper.AppUtils;
import co.yodo.mobile.helper.Intents;
import co.yodo.mobile.network.YodoRequest;

public class RegistrationActivity extends AppCompatActivity implements AppEula.OnEulaAgreedTo, YodoRequest.RESTListener {
    /** DEBUG */
    @SuppressWarnings( "unused" )
    private static final String TAG = RegistrationActivity.class.getSimpleName();

    /** The context object */
    private Context ac;

    /** Account identifier */
    private String hardwareToken;

    /** Messages Handler */
    private static YodoHandler handlerMessages;

    /** Manager for the server requests */
    private YodoRequest mRequestManager;

    /** GUI Controllers */
    private EditText etPip;
    private EditText etConfirmPip;
    private RelativeLayout rlRegistration;

    /** The shake animation for wrong inputs */
    private Animation aShake;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        AppUtils.setLanguage( RegistrationActivity.this );
        setContentView(R.layout.activity_registration);

        setupGUI();
        updateData();
    }

    @Override
    public void onResume() {
        super.onResume();
        mRequestManager.setListener( this );
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
        handlerMessages = new YodoHandler( RegistrationActivity.this );
        mRequestManager = YodoRequest.getInstance( ac );

        // GUI global components
        etPip          = (EditText) findViewById( R.id.pipText );
        etConfirmPip   = (EditText) findViewById( R.id.confirmationPipText );
        rlRegistration = (RelativeLayout) findViewById( R.id.registration_layout );

        // Load the animation
        aShake = AnimationUtils.loadAnimation( this, R.anim.shake );

        // Only used at creation
        Toolbar mActionBarToolbar = (Toolbar) findViewById( R.id.actionBar );

        // Setup the toolbar
        setSupportActionBar( mActionBarToolbar );
        if( getSupportActionBar() != null )
            getSupportActionBar().setDisplayHomeAsUpEnabled( true );

        // Show the terms to the user
        if( AppEula.show( this ) )
            rlRegistration.setVisibility( View.GONE );
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
     * @param v, the view of the checkbox
     */
    public void showPasswordClick( View v ) {
        AppUtils.showPassword( (CheckBox) v, etPip );
        AppUtils.showPassword( (CheckBox) v, etConfirmPip );
    }

    /**
     * Realize a registration request
     * @param v View of the button, not used
     */
    public void registrationClick( View v ) {
        final String pip        = etPip.getText().toString();
        final String confirmPip = etConfirmPip.getText().toString();

        if( pip.length() < AppConfig.MIN_PIP_LENGTH ) {
            ToastMaster.makeText( ac, R.string.pip_short, Toast.LENGTH_SHORT ).show();
            etPip.startAnimation( aShake );
        }
        else if( !pip.equals( confirmPip ) ) {
            ToastMaster.makeText( ac, R.string.pip_different, Toast.LENGTH_SHORT ).show();
            etConfirmPip.startAnimation( aShake );
        } else {
            AppUtils.hideSoftKeyboard( this );

            ProgressDialogHelper.getInstance().createProgressDialog( ac );
            mRequestManager.requestRegistration(
                    hardwareToken,
                    pip
            );
        }
    }

    @Override
    public void onEulaAgreedTo() {
        rlRegistration.setVisibility( View.VISIBLE );
    }

    @Override
    public void onResponse( YodoRequest.RequestType type, ServerResponse response ) {
        ProgressDialogHelper.getInstance().destroyProgressDialog();
        String code, message;

        switch( type ) {
            case REG_CLIENT_REQUEST:
                code = response.getCode();

                // If the auth is correct, let's continue with the registration
                if( code.equals( ServerResponse.AUTHORIZED_REGISTRATION ) ) {
                    AppUtils.saveAuthNumber( ac, response.getAuthNumber() );

                    Intent intent = new Intent( RegistrationActivity.this, RegistrationBiometricActivity.class );
                    intent.putExtra( Intents.AUTH_NUMBER, response.getAuthNumber() );
                    startActivity( intent );
                    finish();
                // There was an error during the process
                } else {
                    message  = response.getMessage();
                    AppUtils.sendMessage( handlerMessages, code, message );
                }

                break;
        }
    }
}
