package co.yodo.mobile.main;

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
import android.widget.Toast;

import co.yodo.mobile.R;
import co.yodo.mobile.component.ToastMaster;
import co.yodo.mobile.component.YodoHandler;
import co.yodo.mobile.data.ServerResponse;
import co.yodo.mobile.helper.AppConfig;
import co.yodo.mobile.helper.AppUtils;
import co.yodo.mobile.helper.Intents;
import co.yodo.mobile.net.YodoRequest;

public class PipResetActivity extends AppCompatActivity implements YodoRequest.RESTListener {
    /** The context object */
    private Context ac;

    /** Messages Handler */
    private static YodoHandler handlerMessages;

    /** Hardware Identifier */
    private String hardwareToken;
    private String authNumber;

    /** GUI Controllers */
    private EditText currentPipText;
    private EditText newPipText;
    private EditText confirmPipText;

    /** Activity Result */
    private static final int REQUEST_FACE_ACTIVITY = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppUtils.setLanguage( PipResetActivity.this );
        setContentView(R.layout.activity_pip_reset);

        setupGUI();
        updateData();

        if( savedInstanceState != null && savedInstanceState.getBoolean( AppConfig.IS_SHOWING ) ) {
            YodoRequest.getInstance().createProgressDialog(
                    PipResetActivity.this ,
                    YodoRequest.ProgressDialogType.NORMAL
            );
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(
                AppConfig.IS_SHOWING,
                YodoRequest.getInstance().progressDialogShowing()
        );
    }

    @Override
    public void onResume() {
        super.onResume();
        YodoRequest.getInstance().setListener( this );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        YodoRequest.getInstance().destroyProgressDialog();
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
        ac = PipResetActivity.this;
        handlerMessages = new YodoHandler( PipResetActivity.this );

        //GUI Global components
        currentPipText = (EditText) findViewById( R.id.currentPipText );
        newPipText     = (EditText) findViewById( R.id.newPipText );
        confirmPipText = (EditText) findViewById( R.id.confirmPipText );

        // Only used at creation
        Toolbar toolbar = (Toolbar) findViewById( R.id.actionBar );

        setSupportActionBar( toolbar );
        getSupportActionBar().setDisplayHomeAsUpEnabled( true );
    }

    private void updateData() {
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
    public void showPasswordClick(View v) {
        AppUtils.showPassword( (CheckBox) v, currentPipText);
        AppUtils.showPassword( (CheckBox) v, newPipText );
        AppUtils.showPassword( (CheckBox) v, confirmPipText );
    }

    public void resetPipClick(View v) {
        Animation shake = AnimationUtils.loadAnimation( this, R.anim.shake );

        final String currentPip = currentPipText.getText().toString();
        final String newPip     = newPipText.getText().toString();
        final String confirmPip = confirmPipText.getText().toString();

        if( currentPip.length() < AppConfig.MIN_PIP_LENGTH ) {
            ToastMaster.makeText( ac, R.string.pip_short, Toast.LENGTH_SHORT ).show();
            currentPipText.startAnimation( shake );
        }
        else if( newPip.length() < AppConfig.MIN_PIP_LENGTH ) {
            ToastMaster.makeText( ac, R.string.pip_short, Toast.LENGTH_SHORT ).show();
            newPipText.startAnimation( shake );
        }
        else if( !newPip.equals( confirmPip ) ) {
            ToastMaster.makeText( ac, R.string.pip_different, Toast.LENGTH_SHORT ).show();
            confirmPipText.startAnimation( shake );
        } else {
            AppUtils.hideSoftKeyboard( this );

            YodoRequest.getInstance().createProgressDialog(
                    PipResetActivity.this ,
                    YodoRequest.ProgressDialogType.NORMAL
            );

            YodoRequest.getInstance().requestPIPAuthentication(
                    PipResetActivity.this,
                    hardwareToken, currentPip
            );
        }
    }

    public void forgotPipClick(View v) {
        Animation shake = AnimationUtils.loadAnimation( this, R.anim.shake );

        final String newPip     = newPipText.getText().toString();
        final String confirmPip = confirmPipText.getText().toString();

        if( newPip.length() < AppConfig.MIN_PIP_LENGTH ) {
            ToastMaster.makeText( ac, R.string.pip_short, Toast.LENGTH_SHORT ).show();
            newPipText.startAnimation( shake );
        }
        else if( !newPip.equals( confirmPip ) ) {
            ToastMaster.makeText( ac, R.string.pip_different, Toast.LENGTH_SHORT ).show();
            confirmPipText.startAnimation( shake );
        } else {
            AppUtils.hideSoftKeyboard( this );

            YodoRequest.getInstance().createProgressDialog(
                    PipResetActivity.this,
                    YodoRequest.ProgressDialogType.NORMAL
            );

            YodoRequest.getInstance().requestBiometricToken(
                    PipResetActivity.this,
                    hardwareToken
            );
        }
    }

    @Override
    public void onResponse(YodoRequest.RequestType type, ServerResponse response) {
        YodoRequest.getInstance().destroyProgressDialog();
        String code, message;

        switch( type ) {
            case ERROR_NO_INTERNET:
                handlerMessages.sendEmptyMessage(YodoHandler.NO_INTERNET);
                break;

            case ERROR_GENERAL:
                handlerMessages.sendEmptyMessage(YodoHandler.GENERAL_ERROR);
                break;

            case AUTH_PIP_REQUEST:
                code = response.getCode();

                if( code.equals( ServerResponse.AUTHORIZED ) ) {
                    final String currentPip = currentPipText.getText().toString();
                    final String newPip     = newPipText.getText().toString();

                    YodoRequest.getInstance().createProgressDialog(
                            PipResetActivity.this,
                            YodoRequest.ProgressDialogType.NORMAL
                    );

                    YodoRequest.getInstance().requestPIPReset(
                            PipResetActivity.this,
                            hardwareToken,
                            currentPip, newPip
                    );
                } else {
                    message  = response.getMessage();
                    AppUtils.sendMessage(handlerMessages, code, message);
                }

                break;

            case RESET_BIO_PIP_REQUEST:
            case RESET_PIP_REQUEST:
                code = response.getCode();

                if( code.equals( ServerResponse.AUTHORIZED ) ) {
                    ToastMaster.makeText( ac, R.string.change_successful, Toast.LENGTH_LONG ).show();
                    finish();
                } else {
                    message  = response.getMessage();
                    AppUtils.sendMessage( handlerMessages, code, message );
                }

                break;

            case QUERY_BIO_REQUEST:
                code = response.getCode();

                if( code.equals( ServerResponse.AUTHORIZED ) ) {
                    String biometricToken = response.getParam( ServerResponse.BIOMETRIC );
                    if( !biometricToken.equals( AppConfig.YODO_BIOMETRIC ) ) {
                        authNumber = response.getAuthNumber();

                        Intent intent = new Intent( PipResetActivity.this, CameraActivity.class );
                        intent.putExtra( Intents.BIOMETRIC_TOKEN, biometricToken );
                        startActivityForResult( intent, REQUEST_FACE_ACTIVITY );
                    } else {
                        AppUtils.sendMessage(
                                handlerMessages,
                                ServerResponse.ERROR_FAILED,
                                getString( R.string.no_biometric )
                        );
                    }
                } else {
                    message  = response.getMessage();
                    AppUtils.sendMessage( handlerMessages, code, message );
                }

                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch( requestCode ) {
            case REQUEST_FACE_ACTIVITY:
                if(resultCode == RESULT_OK) {
                    final String newPip = newPipText.getText().toString();

                    YodoRequest.getInstance().createProgressDialog(
                            PipResetActivity.this,
                            YodoRequest.ProgressDialogType.NORMAL
                    );

                    YodoRequest.getInstance().requestBiometricPIPReset(
                            PipResetActivity.this,
                            authNumber,
                            hardwareToken,
                            newPip
                    );
                }
                break;
        }
    }
}
