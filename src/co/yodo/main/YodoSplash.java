package co.yodo.main;

import co.yodo.R;
import co.yodo.helper.ToastMaster;
import co.yodo.helper.Utils;
import co.yodo.helper.YodoGlobals;
import co.yodo.helper.YodoHandler;
import co.yodo.helper.YodoQueries;
import co.yodo.serverconnection.ServerResponse;
import co.yodo.serverconnection.TaskFragment;
import co.yodo.serverconnection.TaskFragment.SwitchServer;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

public class YodoSplash extends FragmentActivity implements TaskFragment.YodoCallback {
	/*!< DEBUG */
	private final static String TAG = "YodoSplash";
	private final static boolean DEBUG = false;
    
    /*!< Messages Handler */
    private static YodoHandler handlerMessages;
    
    /*!< Fragment Information */
    private TaskFragment mTaskFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_yodo_splash);
        
        setupGUI();
        updateData();
    }
    
    private void setupGUI() {
    	Utils.changeLanguage(this);
    	handlerMessages = new YodoHandler(this);
    	
    	// Load Fragment Manager
    	FragmentManager fm = getSupportFragmentManager();
	    mTaskFragment = (TaskFragment) fm.findFragmentByTag(YodoGlobals.TAG_TASK_FRAGMENT);
	    
	    if(mTaskFragment == null) {
	    	mTaskFragment = new TaskFragment();
	    	fm.beginTransaction().add(mTaskFragment, YodoGlobals.TAG_TASK_FRAGMENT).commit();
	    }
    }
    
    private void updateData() {
    	String hrdwToken = Utils.getHardwareToken(this);

        if(hrdwToken == null) {
            ToastMaster.makeText(YodoSplash.this, R.string.error, Toast.LENGTH_LONG).show();
            finish();
        } else {
        	Utils.Logger(DEBUG, TAG, hrdwToken);
        	requestHardwareAuthorization(hrdwToken);
        }
    }
    
    /**
     * Connects to the switch and gets the user authorization
     */
    private void requestHardwareAuthorization(String hrddToken) {
    	String data = YodoQueries.requestHardwareAuthorization(this, hrddToken);
    	
    	SwitchServer request = mTaskFragment.getSwitchServerInstance();
        mTaskFragment.start(request, SwitchServer.AUTH_HW_REQUEST, data);
    }

	@Override
	public void onTaskCompleted(ServerResponse data, int type) {
		finish();

        if(data != null) {
            String code = data.getCode();
            
            if(code.equals(YodoGlobals.AUTHORIZED)) {
                Intent intent = new Intent(YodoSplash.this, YodoPayment.class);
                startActivity(intent);
            } else if(code.equals(YodoGlobals.ERROR_FAILED)) {
            	//Intent intent = new Intent(YodoSplash.this, YodoRegistration.class);
                //startActivity(intent);
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

	@Override
	public void onPreExecute(String message) {

	}

	@Override
	public void onPostExecute() {

	}
}
