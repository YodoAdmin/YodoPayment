package co.yodo.mobile.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import co.yodo.mobile.R;
import co.yodo.mobile.component.ToastMaster;
import co.yodo.mobile.component.YodoHandler;
import co.yodo.mobile.data.ServerResponse;
import co.yodo.mobile.helper.AppUtils;
import co.yodo.mobile.helper.Intents;
import co.yodo.mobile.net.YodoRequest;

public class SplashActivity extends Activity implements YodoRequest.RESTListener {
    /** The context object */
    private Context ac;

    /** Messages Handler */
    private static YodoHandler handlerMessages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        requestWindowFeature( Window.FEATURE_NO_TITLE );
        getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN );
        setContentView( R.layout.activity_splash );

        setupGUI();
        updateData();
    }

    @Override
    public void onResume() {
        super.onResume();
        YodoRequest.getInstance().setListener( this );
    }

    private void setupGUI() {
        ac = SplashActivity.this;
        handlerMessages = new YodoHandler( SplashActivity.this );
    }

    private void updateData() {
        String hardwareToken = AppUtils.getHardwareToken( ac );
        if( hardwareToken == null ) {
            ToastMaster.makeText( ac, R.string.no_hardware, Toast.LENGTH_LONG ).show();
            finish();
        } else {
            YodoRequest.getInstance().requestAuthentication( SplashActivity.this, hardwareToken );
        }
    }

    @Override
    public void onResponse(YodoRequest.RequestType type, ServerResponse response) {
        finish();
        String code;

        switch( type ) {
            case ERROR_NO_INTERNET:
                handlerMessages.sendEmptyMessage( YodoHandler.NO_INTERNET );
                break;

            case ERROR_GENERAL:
                handlerMessages.sendEmptyMessage( YodoHandler.GENERAL_ERROR );
                break;

            case AUTH_REQUEST:
                code = response.getCode();

                if( code.equals( ServerResponse.AUTHORIZED ) ) {
                    String authNumber = AppUtils.getAuthNumber( ac );
                    if( !authNumber.equals( "" ) ) {
                        Intent intent = new Intent( SplashActivity.this, RegistrationBiometricActivity.class );
                        intent.putExtra( Intents.AUTH_NUMBER, authNumber );
                        startActivity( intent );
                        finish();
                    } else {
                        Intent intent = new Intent( SplashActivity.this, MainActivity.class );
                        startActivity( intent );
                    }
                } else if( code.equals( ServerResponse.ERROR_FAILED ) ) {
                    Intent intent = new Intent( SplashActivity.this, RegistrationActivity.class);
                    startActivity( intent );
                }

                break;
        }
    }
}
