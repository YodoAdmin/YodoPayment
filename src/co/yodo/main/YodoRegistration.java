package co.yodo.main;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;
import co.yodo.R;
import co.yodo.helper.Eula;
import co.yodo.helper.ToastMaster;
import co.yodo.helper.Utils;
import co.yodo.helper.YodoGlobals;
import co.yodo.helper.YodoHandler;
import co.yodo.helper.YodoQueries;
import co.yodo.serverconnection.ServerResponse;
import co.yodo.serverconnection.TaskFragment;
import co.yodo.serverconnection.TaskFragment.SwitchServer;

public class YodoRegistration extends ActionBarActivity implements TaskFragment.YodoCallback {
	/*!< DEBUG */
	private final static String TAG = YodoRegistration.class.getName();
	private final static boolean DEBUG = false;
	
	/*!< GUI Controllers */
    private RelativeLayout registration;
    private EditText pipText;
    private EditText confirmPipText;
	
	/*!< Variable used as an authentication number */
    private static String hrdwToken;
    
    /*!< Preferences */
	private SharedPreferences settings;
    
    /*!< Messages Handler */
    private static YodoHandler handlerMessages;
    
    /*!< Fragment Information */
    private TaskFragment mTaskFragment;
    private ProgressDialog progDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.changeLanguage(this);
        setContentView(R.layout.activity_yodo_registration);

        setupGUI();
        updateData();
        
        Eula.show(this);
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
    
    private void setupGUI() {
    	handlerMessages = new YodoHandler(this);
    	settings        = getSharedPreferences(YodoGlobals.PREFERENCES_EULA, MODE_PRIVATE);
    	getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    	
    	// Load Fragment Manager
    	FragmentManager fm = getSupportFragmentManager();
	    mTaskFragment = (TaskFragment) fm.findFragmentByTag(YodoGlobals.TAG_TASK_FRAGMENT);
	    
	    if(mTaskFragment == null) {
	    	mTaskFragment = new TaskFragment();
	    	fm.beginTransaction().add(mTaskFragment, YodoGlobals.TAG_TASK_FRAGMENT).commit();
	    }
	    
	    progDialog = new ProgressDialog(this);
    	
    	registration   = (RelativeLayout) findViewById(R.id.registration_layout);
        pipText        = (EditText)findViewById(R.id.pipText);
        confirmPipText = (EditText)findViewById(R.id.confirmationPipText);
        
        if(settings.getBoolean(YodoGlobals.PREFERENCE_EULA_ACCEPTED, false) == true)
            loadInstructions();
    }
    
    private void updateData() {
    	hrdwToken = Utils.getHardwareToken(this);
    }
    
    public void loadInstructions() {
        registration.setVisibility(View.VISIBLE);
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
   	 * Connects to the switch and register the user
   	 * @return String 
   	 */
   	private void requestRegistration(String pip) {
        long time = System.currentTimeMillis();
        
   		String data = YodoQueries.requestRegistration(this, hrdwToken, pip, String.valueOf(time));
   		
   		SwitchServer request = mTaskFragment.getSwitchServerInstance();
   		request.setDialog(true, getString(R.string.registering_user_pip));
     
   		mTaskFragment.start(request, SwitchServer.REGISTER_REQUEST, data);
   	}
    
	@Override
	public void onPreExecute(String message) {
		Utils.showProgressDialog(progDialog, message);
	}

	@Override
	public void onPostExecute() {
		if(progDialog != null)
			progDialog.dismiss();
	}

	@Override
	public void onTaskCompleted(ServerResponse data, int type) {
		finish();
		
		if(DEBUG)
			Log.e(TAG, data.toString());

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
