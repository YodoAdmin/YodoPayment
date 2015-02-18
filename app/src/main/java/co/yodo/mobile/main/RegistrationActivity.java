package co.yodo.mobile.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
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
import co.yodo.mobile.component.ToastMaster;
import co.yodo.mobile.component.YodoHandler;
import co.yodo.mobile.data.ServerResponse;
import co.yodo.mobile.helper.AppConfig;
import co.yodo.mobile.helper.AppEula;
import co.yodo.mobile.helper.AppUtils;
import co.yodo.mobile.helper.Intents;
import co.yodo.mobile.net.YodoRequest;

public class RegistrationActivity extends ActionBarActivity implements AppEula.OnEulaAgreedTo, YodoRequest.RESTListener {
    /** The context object */
    private Context ac;

    /** GUI Controllers */
    private EditText pipText;
    private EditText confirmPipText;
    private RelativeLayout mRegistrationLayout;

    /** Messages Handler */
    private static YodoHandler handlerMessages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppUtils.setLanguage( RegistrationActivity.this );
        setContentView(R.layout.activity_registration);

        setupGUI();
    }

    @Override
    public void onResume() {
        super.onResume();
        YodoRequest.getInstance().setListener( this );
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
        ac = RegistrationActivity.this;
        handlerMessages = new YodoHandler( RegistrationActivity.this );

        // GUI global components
        pipText             = (EditText) findViewById( R.id.pipText );
        confirmPipText      = (EditText) findViewById( R.id.confirmationPipText );
        mRegistrationLayout = (RelativeLayout) findViewById( R.id.registration_layout );

        // Only used at creation
        Toolbar mActionBarToolbar = (Toolbar) findViewById( R.id.actionBar );

        setSupportActionBar( mActionBarToolbar );
        getSupportActionBar().setDisplayHomeAsUpEnabled( true );

        if( AppEula.show( this ) )
            mRegistrationLayout.setVisibility( View.GONE );
    }

    /**
     * Realize a registration request
     * @param v View of the button, not used
     */
    public void registrationClick(View v) {
        Animation shake = AnimationUtils.loadAnimation( this, R.anim.shake );

        final String pip        = pipText.getText().toString();
        final String confirmPip = confirmPipText.getText().toString();

        if( pip.length() < AppConfig.MIN_PIP_LENGTH ) {
            ToastMaster.makeText( ac, R.string.pip_short, Toast.LENGTH_SHORT ).show();
            pipText.startAnimation( shake );
        }
        else if( !pip.equals( confirmPip ) ) {
            ToastMaster.makeText( ac, R.string.pip_different, Toast.LENGTH_SHORT ).show();
            confirmPipText.startAnimation( shake );
        } else {
            String hardwareToken = AppUtils.getHardwareToken( ac );
            AppUtils.hideSoftKeyboard( this );

            YodoRequest.getInstance().createProgressDialog(
                    RegistrationActivity.this ,
                    YodoRequest.ProgressDialogType.NORMAL
            );

            YodoRequest.getInstance().requestRegistration(
                    RegistrationActivity.this,
                    hardwareToken,
                    pip
            );
        }
    }

    /**
     * Show the passwords
     * @param v, the view of the checkbox
     */
    public void showPasswordClick(View v) {
        AppUtils.showPassword( (CheckBox) v, pipText );
        AppUtils.showPassword( (CheckBox) v, confirmPipText );
    }

    @Override
    public void onEulaAgreedTo() {
        mRegistrationLayout.setVisibility( View.VISIBLE );
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

            case REG_CLIENT_REQUEST:
                code = response.getCode();

                if( code.equals( ServerResponse.AUTHORIZED_REGISTRATION ) ) {
                    Intent intent = new Intent( RegistrationActivity.this, RegistrationBiometricActivity.class );
                    intent.putExtra( Intents.AUTH_NUMBER, response.getAuthNumber() );
                    startActivity( intent );
                    finish();
                } else {
                    message  = response.getMessage();
                    AppUtils.sendMessage( handlerMessages, code, message );
                }

                break;
        }
    }
}
