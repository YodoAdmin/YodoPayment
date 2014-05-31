package co.yodo.main;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import co.yodo.R;
import co.yodo.helper.HardwareToken;
import co.yodo.helper.Language;
import co.yodo.helper.ToastMaster;
import co.yodo.helper.YodoBase;
import co.yodo.helper.Eula;
import co.yodo.helper.YodoGlobals;
import co.yodo.serverconnection.ServerResponse;
import co.yodo.serverconnection.SwitchServer;
import co.yodo.sks.Encrypter;

/**
 * Created by luis on 5/08/13.
 */
public class YodoRegistration extends Activity implements YodoBase {
    /*!< Time Stamp */
    private String timeStamp;

    /*!< Variable used as an authentication number */
    private static String HARDWARE_TOKEN;

    /*!< Preferences */
    private SharedPreferences settings;

    /*!< Object used to encrypt user's information */
    private Encrypter oEncrypter;

    /*!< GUI Controllers */
    private RelativeLayout registration;
    private EditText pipText;
    private EditText confirmPipText;

    /*!< User's data separator */
    private static final String	USR_SEP = "**";

    /*!< Message Handler */
    private static MainHandler handlerMessages;
    
    /**
     * Handles the message if there isn't internet connection
     */
    private static class MainHandler extends Handler {
        private final WeakReference<YodoRegistration> wMain;

        public MainHandler(YodoRegistration main) {
            super();
            this.wMain = new WeakReference<YodoRegistration>(main);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            YodoRegistration main = wMain.get();

            // message arrived after activity death
            if(main == null)
                return;

            if(msg.what == YodoGlobals.NO_INTERNET) {
                ToastMaster.makeText(main, R.string.no_internet, Toast.LENGTH_LONG).show();
            }
            else if(msg.what == YodoGlobals.GENERAL_ERROR) {
                ToastMaster.makeText(main, R.string.error, Toast.LENGTH_LONG).show();
            }
            else if(msg.what == YodoGlobals.UNKOWN_ERROR) {
				String response = msg.getData().getString("message");
				ToastMaster.makeText(main, response, Toast.LENGTH_LONG).show();
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
    	setTitle(getString(R.string.registration));
        setContentView(R.layout.activity_yodo_registration);

        setupGUI();
        updateData();
        
        Eula.show(this);
    }

    private void setupGUI() {
        handlerMessages = new MainHandler(this);
        settings = getSharedPreferences(YodoGlobals.PREFERENCES_EULA, MODE_PRIVATE);

        registration   = (RelativeLayout) findViewById(R.id.registration_layout);
        pipText        = (EditText)findViewById(R.id.pipText);
        confirmPipText = (EditText)findViewById(R.id.confirmationPipText);

        if(settings.getBoolean(YodoGlobals.PREFERENCE_EULA_ACCEPTED, false) == true)
            loadInstructions();
    }

    private void updateData() {
        HardwareToken token = new HardwareToken(getApplicationContext());
        HARDWARE_TOKEN = token.getToken();

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ssZZZZ", java.util.Locale.getDefault());
        long time = System.currentTimeMillis();
        timeStamp = dateFormat.format(time);
    }

    public void loadInstructions() {
        registration.setVisibility(View.VISIBLE);
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
    public void showPressed(View v) {
        if(((CheckBox)v).isChecked()) {
            pipText.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
            confirmPipText.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
        else {
            pipText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            confirmPipText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
    }

    public void registerPipClick(View v) {
        String pip        = pipText.getText().toString();
        String confirmPip = confirmPipText.getText().toString();
        Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
        boolean errorMessage = false;

        if(pip.length() < YodoGlobals.MIN_PIP_LENGTH) {
            pipText.startAnimation(shake);
            ToastMaster.makeText(YodoRegistration.this, R.string.pip_length_short, Toast.LENGTH_SHORT).show();
            errorMessage = true;
        }

        if(!pip.equals(confirmPip)) {
            confirmPipText.startAnimation(shake);

            if(!errorMessage) {
                ToastMaster.makeText(YodoRegistration.this, R.string.pip_confirm_diff, Toast.LENGTH_SHORT).show();
                errorMessage = true;
            }
        }

        if(!errorMessage) {
            requestRegistration(pip);
        }
    }

    /**
     * Connects to the switch and send the user data
     */
    private void requestRegistration(String pip) {
        StringBuilder userData = new StringBuilder();
        String sEncryptedUsrData;

        userData.append(YodoGlobals.USER_BIOMETRIC).append(USR_SEP);
        userData.append(pip).append(USR_SEP);
        userData.append(HARDWARE_TOKEN).append(USR_SEP);
        userData.append(timeStamp);

        // Encrypting user's data to create request
        getEncrypter().setsUnEncryptedString(userData.toString());
        getEncrypter().rsaEncrypt(this);
        sEncryptedUsrData = getEncrypter().bytesToHex();

        SwitchServer request = new SwitchServer(YodoRegistration.this);
        request.setDialog(true, getString(R.string.registering_user_pip));
        request.execute(SwitchServer.REGISTER_REQUEST, sEncryptedUsrData);
    }

    @Override
    public void setData(ServerResponse data, int queryType) {
        finish();

        if(data != null) {
            String code = data.getCode();
            if(code.equals(YodoGlobals.AUTHORIZED_REGISTRATION)) {
                Intent intent = new Intent(YodoRegistration.this, YodoBiometric.class);
                intent.putExtra(YodoGlobals.ID_AUTHORIZATION, data.getAuthNumber());
                startActivity(intent);
            } else if(code.equals(YodoGlobals.ERROR_INTERNET)) {
                handlerMessages.sendEmptyMessage(YodoGlobals.NO_INTERNET);
            } else {
            	final Message message = new Message();
                message.what = YodoGlobals.UNKOWN_ERROR;
                
            	Bundle bundle = new Bundle();
            	bundle.putString("message", data.getParams());
            	message.setData(bundle);
            	
            	handlerMessages.sendMessage(message);
            }
        } else {
            handlerMessages.sendEmptyMessage(YodoGlobals.GENERAL_ERROR);
        }
    }
}
