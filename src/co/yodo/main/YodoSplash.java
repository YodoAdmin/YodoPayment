package co.yodo.main;

import java.lang.ref.WeakReference;

import co.yodo.R;
import co.yodo.helper.HardwareToken;
import co.yodo.helper.Language;
import co.yodo.helper.ToastMaster;
import co.yodo.helper.YodoBase;
import co.yodo.helper.YodoGlobals;
import co.yodo.serverconnection.ServerResponse;
import co.yodo.serverconnection.SwitchServer;
import co.yodo.sks.Encrypter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;
import android.app.Activity;
import android.content.Intent;

public class YodoSplash extends Activity implements YodoBase {
	/*!< Variable used as an authentication number */
    private static String HARDWARE_TOKEN;

    /*!< Object used to encrypt user's information */
    private Encrypter oEncrypter;
    
    /*!< Messages Handler */
    private static MainHandler handlerMessages;
    
    /**
     * Handles the message if there isn't internet connection
     */
    private static class MainHandler extends Handler {
        private final WeakReference<YodoSplash> wMain;

        public MainHandler(YodoSplash main) {
            super();
            this.wMain = new WeakReference<YodoSplash>(main);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            YodoSplash main = wMain.get();

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
        }
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_yodo_splash);
       
        setupGUI();
        updateData();
        
        if(HARDWARE_TOKEN != null)
        	requestHardwareAuthorization();
    }
    
    private void setupGUI() {
    	Language.changeLanguage(this);
    	handlerMessages = new MainHandler(this);
    }
    
    /**
     * Update user account data within the application
     */
    private void updateData() {
        HardwareToken token = new HardwareToken(getApplicationContext());
        HARDWARE_TOKEN = token.getToken();

        if(HARDWARE_TOKEN == null) {
            ToastMaster.makeText(YodoSplash.this, R.string.error, Toast.LENGTH_LONG).show();
            finish();
        }
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
     * Connects to the switch and gets the user authorization
     */
    private void requestHardwareAuthorization() {
        String sEncryptedUsrData;

        // Encrypting user's  to create request
        getEncrypter().setsUnEncryptedString(HARDWARE_TOKEN);
        getEncrypter().rsaEncrypt(this);
        sEncryptedUsrData = getEncrypter().bytesToHex();

        new SwitchServer(YodoSplash.this).execute(SwitchServer.AUTH_HW_REQUEST, sEncryptedUsrData);
    }
    
    @Override
    public void setData(ServerResponse data, int queryType) {
        finish();

        if(data != null) {
            String code = data.getCode();
            if(code.equals(YodoGlobals.AUTHORIZED)) {
                Intent intent = new Intent(YodoSplash.this, YodoPayment.class);
                startActivity(intent);
            } else if(code.equals(YodoGlobals.ERROR_FAILED)) {
            	Intent intent = new Intent(YodoSplash.this, YodoRegistration.class);
                startActivity(intent);
            } else if(code.equals(YodoGlobals.ERROR_INTERNET)) {
                handlerMessages.sendEmptyMessage(YodoGlobals.NO_INTERNET);
            } else {
            	final Message message = new Message();
                message.what = YodoGlobals.UNKOWN_ERROR;
                
            	Bundle bundle = new Bundle();
            	bundle.putString("message", data.getMessage());
            	message.setData(bundle);
            	
            	handlerMessages.sendMessage(message);
            }
        } else {
            handlerMessages.sendEmptyMessage(YodoGlobals.GENERAL_ERROR);
        }
    }
}
