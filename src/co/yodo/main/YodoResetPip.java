package co.yodo.main;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import java.lang.ref.WeakReference;

import co.yodo.R;
import co.yodo.helper.FormatHelper;
import co.yodo.helper.HardwareToken;
import co.yodo.helper.Language;
import co.yodo.helper.ToastMaster;
import co.yodo.helper.YodoBase;
import co.yodo.helper.YodoGlobals;
import co.yodo.serverconnection.ServerResponse;
import co.yodo.serverconnection.SwitchServer;
import co.yodo.sks.Encrypter;

/**
 * Created by luis on 25/07/13.
 */
public class YodoResetPip extends Activity implements YodoBase {
    /*!< Variable used as an authentication number */
    private static String HARDWARE_TOKEN;

    /*!< Object used to encrypt user's information */
    private Encrypter oEncrypter;

    /*!< GUI Controllers */
    private EditText pipText;
    private EditText newPipText;
    private EditText confirmPipText;

    /*!< ID for queries */
    private final static int AUTH_REQ = 0;
    private final static int CPIP_REQ = 1;
    private final static int BIO_REQ  = 2;
    
    /*!< Activity Result */
    private static final int REQUEST_FACE_ACTIVITY = 0;

    /*!< User's password */
    private String currentPip, newPip, confirmPip;

    /*!< User's data separator */
    private static final String	REQ_SEP     = ",";
    
    /*!< Alert Messages */
	private AlertDialog alertDialog;
	
	/*!< Message Handler */
    private static MainHandler handlerMessages;

    /**
     * Handles the message if there isn't internet connection
     */
    private static class MainHandler extends Handler {
        private final WeakReference<YodoResetPip> wMain;

        public MainHandler(YodoResetPip main) {
            super();
            this.wMain = new WeakReference<YodoResetPip>(main);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            YodoResetPip main = wMain.get();

            // message arrived after activity death
            if(main == null)
                return;

            if(msg.what == YodoGlobals.NO_INTERNET) {
                ToastMaster.makeText(main, R.string.no_internet, Toast.LENGTH_LONG).show();
            }
            else if(msg.what == YodoGlobals.GENERAL_ERROR) {
                ToastMaster.makeText(main, R.string.error, Toast.LENGTH_LONG).show();
            }
            else if(msg.what == YodoGlobals.SUCCESS) {
                ToastMaster.makeText(main, R.string.change_pip_succesfull, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Language.changeLanguage(this);
        setContentView(R.layout.activity_yodo_reset_pip);
        
        setupGUI();
        updateData();
    }

    @SuppressLint("NewApi")
	private void setupGUI() {
        handlerMessages = new MainHandler(this);
        setTitle(getString(R.string.reset_pip));
        
        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
        	getActionBar().setDisplayHomeAsUpEnabled(true);
        }

        pipText        = (EditText)findViewById(R.id.currentPipText);
        newPipText     = (EditText)findViewById(R.id.newPipText);
        confirmPipText = (EditText)findViewById(R.id.confirmPipText);
    }

    private void updateData() {
        HardwareToken token = new HardwareToken(getApplicationContext());
        HARDWARE_TOKEN = token.getToken();
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                finish();
            break;

            default:
            break;
        }
        return true;
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
     * Handle Buttons Actions
     * */
    public void showPressed(View v) {
        if(((CheckBox)v).isChecked()) {
            pipText.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
            newPipText.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
            confirmPipText.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
        else {
            pipText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            newPipText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            confirmPipText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
    }

    public void updatePipClick(View v) {
        currentPip = pipText.getText().toString();
        newPip     = newPipText.getText().toString();
        confirmPip = confirmPipText.getText().toString();
        Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
        boolean errorMessage = false;

        if(currentPip.length() < YodoGlobals.MIN_PIP_LENGTH) {
            pipText.startAnimation(shake);
            ToastMaster.makeText(YodoResetPip.this, R.string.pip_length_short, Toast.LENGTH_SHORT).show();
            errorMessage = true;
        }

        if(newPip.length() < YodoGlobals.MIN_PIP_LENGTH) {
            newPipText.startAnimation(shake);

            if(!errorMessage) {
                ToastMaster.makeText(YodoResetPip.this, R.string.pip_length_short, Toast.LENGTH_SHORT).show();
                errorMessage = true;
            }
        }

        if(!newPip.equals(confirmPip)) {
            confirmPipText.startAnimation(shake);

            if(!errorMessage) {
                ToastMaster.makeText(YodoResetPip.this, R.string.pip_confirm_diff, Toast.LENGTH_SHORT).show();
                errorMessage = true;
            }
        }

        confirmPip = "";
        if(!errorMessage) {
        	requestPIPHardwareAuthentication(currentPip);
        }
    }
    
    private void lockScreenOrientation() {
        int currentOrientation = getResources().getConfiguration().orientation;
        if(currentOrientation == Configuration.ORIENTATION_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    }
    
    
    private void unlockScreenOrientation() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
    }

    /**
     * Connects to the switch and sends the user data to authenticate
     */
    private void requestPIPHardwareAuthentication(String pip) {
        String sEncryptedUsrData, sFormattedUsrData;
        sFormattedUsrData = FormatHelper.formatUsrData(HARDWARE_TOKEN, pip);

        // Encrypting user's data to create request
        getEncrypter().setsUnEncryptedString(sFormattedUsrData);
        getEncrypter().rsaEncrypt(this);
        sEncryptedUsrData = this.getEncrypter().bytesToHex();

        lockScreenOrientation();
        SwitchServer request = new SwitchServer(YodoResetPip.this);
        request.setType(AUTH_REQ);
        request.setDialog(true, getString(R.string.auth_message));
        request.execute(SwitchServer.AUTH_HW_PIP_REQUEST, sEncryptedUsrData);
    }

    /**
     * Connects to the switch and change the actual password for a new one
     */
    private void requestPIPReset(String pip, String newPìp) {
        String sEncryptedUsrData, sEncryptedOldPip, sEncryptedNewPip;
        StringBuilder userData = new StringBuilder();
        
        // Encrypting user's IMEI to create request
        getEncrypter().setsUnEncryptedString(HARDWARE_TOKEN);
        getEncrypter().rsaEncrypt(this);
        sEncryptedUsrData = getEncrypter().bytesToHex();

        getEncrypter().setsUnEncryptedString(pip);
        getEncrypter().rsaEncrypt(YodoResetPip.this);
        sEncryptedOldPip = getEncrypter().bytesToHex();

        getEncrypter().setsUnEncryptedString(newPìp);
        getEncrypter().rsaEncrypt(this);
        sEncryptedNewPip = getEncrypter().bytesToHex();

        userData.append(sEncryptedUsrData).append(REQ_SEP);
        userData.append(sEncryptedOldPip).append(REQ_SEP);
        userData.append(sEncryptedNewPip);
       
        SwitchServer request = new SwitchServer(YodoResetPip.this);
        request.setType(CPIP_REQ);
        request.setDialog(true, getString(R.string.change_pip_message));
        request.execute(SwitchServer.RESET_PIP_REQUEST, userData.toString());
    }
    
    /**
	 * Connects to the switch and request the biometric token
	 * @return String message The message of good bye
	 */
	private void requestBiometricToken(String pip) {
		StringBuilder userData = new StringBuilder();
		String sEncryptedUsrData;

		userData.append(HARDWARE_TOKEN).append(REQ_SEP);
		userData.append(pip).append(REQ_SEP);
		userData.append(YodoGlobals.QUERY_BIO);
		
		/// Encrypting user's data to create request
		this.getEncrypter().setsUnEncryptedString(userData.toString());
		this.getEncrypter().rsaEncrypt(this);
		sEncryptedUsrData = this.getEncrypter().bytesToHex();
		
		SwitchServer request = new SwitchServer(YodoResetPip.this);
        request.setType(BIO_REQ);
        request.setDialog(true, getString(R.string.biometric_message));
        request.execute(SwitchServer.BIOMETRIC_REQUEST, sEncryptedUsrData);
	}

    @Override
    public void setData(ServerResponse data, int queryType) {
    	AlertDialog.Builder builder = new AlertDialog.Builder(YodoResetPip.this);
    	
        if(data != null) {
            String code = data.getCode();
            if(code.equals(YodoGlobals.AUTHORIZED)) {
	            switch(queryType) {
	                case AUTH_REQ:
	                	requestBiometricToken(currentPip);
	                    //requestPIPReset(currentPip, newPip);
	                break;
	
	                case CPIP_REQ:
                        handlerMessages.sendEmptyMessage(YodoGlobals.SUCCESS);
                        finish();
                    break;
                    
	                case BIO_REQ:
	                	Intent intent = new Intent(YodoResetPip.this, YodoCamera.class);
	            		intent.putExtra(YodoGlobals.ID_TOKEN, data.getParams());
	                	startActivityForResult(intent, REQUEST_FACE_ACTIVITY);
	                	
	                	currentPip = "";
	                    newPip = "";
	                break;
	            }
            }
	        else if(code.equals(YodoGlobals.ERROR_INTERNET)) {
	        	unlockScreenOrientation();
	            handlerMessages.sendEmptyMessage(YodoGlobals.NO_INTERNET);
	        } else {
	        	unlockScreenOrientation();
	        	builder.setTitle(Html.fromHtml("<font color='#FF0000'>" + data.getCode() + "</font>"));
            	builder.setMessage(Html.fromHtml("<font color='#FF0000'>" + data.getMessage() + "</font>"));
            	builder.setPositiveButton(getString(R.string.ok), null);
            	alertDialog = builder.create();
            	alertDialog.show();
	        }
        } else {
        	unlockScreenOrientation();
            handlerMessages.sendEmptyMessage(YodoGlobals.GENERAL_ERROR);
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {      
            case(REQUEST_FACE_ACTIVITY):
            	if(resultCode == Activity.RESULT_OK) {
            		requestPIPReset(pipText.getText().toString(), newPipText.getText().toString());
            	}
            break;
        }
    }
}
