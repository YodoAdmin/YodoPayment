package co.yodo.main;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.view.View;
import android.widget.Toast;
import co.yodo.R;
import co.yodo.helper.HardwareToken;
import co.yodo.helper.Language;
import co.yodo.helper.ToastMaster;
import co.yodo.helper.YodoBase;
import co.yodo.helper.YodoGlobals;
import co.yodo.serverconnection.ServerResponse;
import co.yodo.serverconnection.SwitchServer;
import co.yodo.sks.Encrypter;

/**
 * Created by luis on 5/08/13.
 */
public class YodoBiometric extends Activity implements YodoBase {
    /*!< Result Activities Identifiers */
    private static final int FACE_ACTIVITY = 1;
    
    /*!< Variable used as an authentication number */
    private static String HARDWARE_TOKEN;
    private String AUTH_NUMBER = "";

    /*!< Object used to encrypt user's information */
    private Encrypter oEncrypter;
    
    /*!< ID for queries */
    private final static int AUTH_REQ = 0;
    private final static int BIO_REQ  = 1;

    /*!< User Data */
    private String BIOMETRIC_TOKEN = "";
    
    /*!< User's data separator */
    private static final String	REQ_SEP = ",";

    /*!< Message Handler */
    private static MainHandler handlerMessages;
    
    /*!< Alert Messages */
	private AlertDialog alertDialog;

    /**
     * Handles the message if there isn't internet connection
     */
    private static class MainHandler extends Handler {
        private final WeakReference<YodoBiometric> wMain;

        public MainHandler(YodoBiometric main) {
            super();
            this.wMain = new WeakReference<YodoBiometric>(main);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            YodoBiometric main = wMain.get();

            // message arrived after activity death
            if(main == null)
                return;

            if(msg.what == YodoGlobals.NO_INTERNET) {
                ToastMaster.makeText(main, R.string.no_internet, Toast.LENGTH_LONG).show();
            }
            else if(msg.what == YodoGlobals.GENERAL_ERROR) {
                ToastMaster.makeText(main, R.string.error, Toast.LENGTH_LONG).show();
            }
        }
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Language.changeLanguage(this);
    	setTitle(getString(R.string.registration));
    	setContentView(R.layout.activity_yodo_biometric);
    	
    	setupGUI();
        updateData();
    }

    private void setupGUI() {
    	Language.changeLanguage(this);
    	handlerMessages = new MainHandler(this);
    }

    private void updateData() {
    	 HardwareToken token = new HardwareToken(getApplicationContext());
         HARDWARE_TOKEN = token.getToken();
         
         Bundle bundle = this.getIntent().getExtras();
         
         if(bundle != null)
 			AUTH_NUMBER = bundle.getString(YodoGlobals.ID_AUTHORIZATION);
    }
    
    /**
     * Gets object used to encrypt user's information
     * @return	Encrypter
     */
    public Encrypter getEncrypter(){
        if(oEncrypter == null)
            oEncrypter = new Encrypter();
        return oEncrypter;
    }

    /**
     * Handle Button Actions
     * */
    public void voiceBiometricClicked(View v) {
        ToastMaster.makeText(YodoBiometric.this, R.string.not_available, Toast.LENGTH_SHORT).show();
    }

    public void faceBiometricClicked(View v) {
    	PackageManager pm = getPackageManager();

    	if(pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
    		Intent intent = new Intent(YodoBiometric.this, YodoCamera.class);
            startActivityForResult(intent, FACE_ACTIVITY);
    	} else {
    		ToastMaster.makeText(YodoBiometric.this, R.string.no_camera, Toast.LENGTH_SHORT).show();
    	}
    }
    
    public void registerBiometricClick(View v) {
    	if(BIOMETRIC_TOKEN.equals("")) {
    		ToastMaster.makeText(YodoBiometric.this, R.string.no_biometric, Toast.LENGTH_SHORT).show();
			return;
    	} else {
    		requestHardwareAuthorization();
    	}
    }
    
    /**
	 * Connects to the switch and gets the user authorization
	 * @return boolean True if the user exists, else false 
	 */
	private void requestHardwareAuthorization() {
		String sEncryptedUsrData;
		
		//Encrypting user's IMEI to create request
		getEncrypter().setsUnEncryptedString(HARDWARE_TOKEN);
		getEncrypter().rsaEncrypt(this);
		sEncryptedUsrData = this.getEncrypter().bytesToHex();
		
		SwitchServer request = new SwitchServer(YodoBiometric.this);
        request.setType(AUTH_REQ);
        request.setDialog(true, getString(R.string.auth_message));
        request.execute(SwitchServer.AUTH_HW_REQUEST, sEncryptedUsrData);
	}
	
	/**
	 * Connects to the switch and send the user data
	 * @return boolean True if the registration is successfully, else false 
	 */
	private void requestBiometricRegistration() {
		StringBuilder userData = new StringBuilder();

		userData.append(AUTH_NUMBER).append(REQ_SEP);
		userData.append(BIOMETRIC_TOKEN);
		
		SwitchServer request = new SwitchServer(YodoBiometric.this);
        request.setType(BIO_REQ);
        request.setDialog(true, getString(R.string.registering_user_biometric));
        request.execute(SwitchServer.BIO_REG_REQUEST, userData.toString());
	}

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case(FACE_ACTIVITY) : {
                if(resultCode == Activity.RESULT_OK) {
                    BIOMETRIC_TOKEN = data.getStringExtra(YodoGlobals.FACE_DATA);
                    ToastMaster.makeText(YodoBiometric.this, R.string.saved_face, Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }

    @Override
    public void setData(ServerResponse data, int queryType) {
    	AlertDialog.Builder builder = new AlertDialog.Builder(YodoBiometric.this);
    	
        if(data != null) {
            String code = data.getCode();
            if(code.equals(YodoGlobals.AUTHORIZED)) {
            	switch (queryType) {
	                case AUTH_REQ:
	                	requestBiometricRegistration();
	                break;
	
	                case BIO_REQ:
	                	Intent intent = new Intent(YodoBiometric.this, YodoPayment.class);
        				startActivity(intent);
        				finish();
		            break;
	
	            }
            } else if(code.equals(YodoGlobals.ERROR_INTERNET)) {
                handlerMessages.sendEmptyMessage(YodoGlobals.NO_INTERNET);
            } else {
            	builder.setTitle(Html.fromHtml("<font color='#FF0000'>" + data.getCode() + "</font>"));
            	builder.setMessage(Html.fromHtml("<font color='#FF0000'>" + data.getMessage() + "</font>"));
            	builder.setPositiveButton(getString(R.string.ok), null);
            	alertDialog = builder.create();
            	alertDialog.show();
            }
        } else {
            handlerMessages.sendEmptyMessage(YodoGlobals.GENERAL_ERROR);
        }
    }
}
