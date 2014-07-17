package co.yodo.main;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import co.yodo.R;
import co.yodo.helper.ToastMaster;
import co.yodo.helper.Utils;
import co.yodo.helper.YodoGlobals;
import co.yodo.helper.YodoHandler;
import co.yodo.helper.YodoQueries;
import co.yodo.serverconnection.ServerResponse;
import co.yodo.serverconnection.TaskFragment;
import co.yodo.serverconnection.TaskFragment.SwitchServer;

public class YodoResetPip extends ActionBarActivity implements TaskFragment.YodoCallback {
	/*!< DEBUG */
	private final static String TAG = YodoResetPip.class.getName();
	private final static boolean DEBUG = true;
	
	 /*!< GUI Controllers */
    private EditText pipText;
    private EditText newPipText;
    private EditText confirmPipText;
    
    /*!< User's password */
    private String currentPip, newPip, confirmPip, authNumber;
    
    /*!< Variable used as an authentication number */
    private static final String KEY_TEMP_PIP     = "key_temp_pip";
    private static final String KEY_TEMP_NEW_PIP = "key_temp_new_pip";
    private static final String KEY_TEMP_AUTH_N  = "key_temp_auth_number";
    private static String hrdwToken;
    
    /*!< Messages Handler */
    private static YodoHandler handlerMessages;
    
    /*!< Alert Messages */
	private AlertDialog alertDialog;
    
    /*!< Fragment Information */
    private TaskFragment mTaskFragment;
    private ProgressDialog progDialog;
    private String message;
    
    /*!< ID for queries */
    private final static int AUTH_REQ = 0;
    private final static int CPIP_REQ = 1;
    private final static int BIO_REQ  = 2;
    
    /*!< Activity Result */
    private static final int REQUEST_FACE_ACTIVITY = 0;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.changeLanguage(this);
        setContentView(R.layout.activity_yodo_reset_pip);
        
        setupGUI();
        updateData();
        
        // Restore saved state.
	    if(savedInstanceState != null) {
	    	currentPip = savedInstanceState.getString(KEY_TEMP_PIP);
	    	newPip     = savedInstanceState.getString(KEY_TEMP_NEW_PIP);
	    	authNumber = savedInstanceState.getString(KEY_TEMP_AUTH_N);
	    	
	    	if(savedInstanceState.getBoolean(YodoGlobals.KEY_IS_SHOWING)) {
	    		message    = savedInstanceState.getString(YodoGlobals.KEY_MESSAGE);
	    		Utils.showProgressDialog(progDialog, message);
	    	}
	    } 
    }
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	    outState.putBoolean(YodoGlobals.KEY_IS_SHOWING, progDialog.isShowing());
	    outState.putString(YodoGlobals.KEY_MESSAGE, message);
	    
	    if(currentPip != null)
	    	outState.putString(KEY_TEMP_PIP, currentPip);
	    
	    if(newPip != null)
	    	outState.putString(KEY_TEMP_NEW_PIP, newPip);
	    
	    if(authNumber != null)
	    	outState.putString(KEY_TEMP_AUTH_N, authNumber);
	}
	
	@Override
    protected void onDestroy() {
        super.onDestroy();
        System.gc();
    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.yodo_reset_pip, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
	        case android.R.id.home:
	            finish();
	        break;
	
	        case R.id.action_forgot_pip:
	            newPip     = newPipText.getText().toString();
	            confirmPip = confirmPipText.getText().toString();
	            
	            Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
	            boolean errorMessage = false;
	            
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
	            	requestBiometricToken();
	            }
	        break;
	        
	        default:
	        break;
	    }
		return super.onOptionsItemSelected(item);
	}
	
	private void setupGUI() {
		handlerMessages = new YodoHandler(this);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		pipText        = (EditText)findViewById(R.id.currentPipText);
        newPipText     = (EditText)findViewById(R.id.newPipText);
        confirmPipText = (EditText)findViewById(R.id.confirmPipText);
        
        // Load Fragment Manager
    	FragmentManager fm = getSupportFragmentManager();
	    mTaskFragment = (TaskFragment) fm.findFragmentByTag(YodoGlobals.TAG_TASK_FRAGMENT);
	    
	    if(mTaskFragment == null) {
	    	mTaskFragment = new TaskFragment();
	    	fm.beginTransaction().add(mTaskFragment, YodoGlobals.TAG_TASK_FRAGMENT).commit();
	    }
        
        progDialog = new ProgressDialog(this);
	}
	
	private void updateData() {
		hrdwToken = Utils.getHardwareToken(this);
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
    
    /**
     * Connects to the switch and authenticate the user
     */
    private void requestPIPHardwareAuthentication(String pip) {
        String data = YodoQueries.requestPIPHardwareAuthentication(this, hrdwToken, pip);

        SwitchServer request = mTaskFragment.getSwitchServerInstance();
        request.setType(AUTH_REQ);
        request.setDialog(true, getString(R.string.auth_message));
        
        mTaskFragment.start(request, SwitchServer.AUTH_HW_PIP_REQUEST, data);
    }
    
    /**
  	 * Connects to the switch and request the biometric token
  	 * @return String message The message of good bye
  	 */
  	private void requestBiometricToken() {
  		String data = YodoQueries.requestBiometricToken(this, hrdwToken);

        SwitchServer request = mTaskFragment.getSwitchServerInstance();
        request.setType(BIO_REQ);
        request.setDialog(true, getString(R.string.biometric_message));
        
        mTaskFragment.start(request, SwitchServer.BIOMETRIC_REQUEST, data);
  	}
  	
  	/**
     * Connects to the switch and change the actual password for a new one
     */
    private void requestPIPReset(String pip, String newPip) {
    	String data = YodoQueries.requestPIPReset(this, hrdwToken, pip, newPip);

        SwitchServer request = mTaskFragment.getSwitchServerInstance();
        request.setType(CPIP_REQ);
        request.setDialog(true, getString(R.string.change_pip_message));
        
        mTaskFragment.start(request, SwitchServer.RESET_PIP_REQUEST, data);
    }
    
    /**
     * Connects to the switch and change the actual password for a new one
     */
    private void requestPIPResetBio(String authNumber, String newPip) {
    	String data = YodoQueries.requestPIPResetBio(this, authNumber, hrdwToken, newPip);

        SwitchServer request = mTaskFragment.getSwitchServerInstance();
        request.setType(CPIP_REQ);
        request.setDialog(true, getString(R.string.change_pip_message));
        
        mTaskFragment.start(request, SwitchServer.RESET_PIP_BIO_REQUEST, data);
    }
	
	@Override
	public void onPreExecute(String message) {
		this.message = message;
		Utils.showProgressDialog(progDialog, message);
	}

	@Override
	public void onPostExecute() {
		if(progDialog != null)
			progDialog.dismiss();
	}

	@Override
	public void onTaskCompleted(ServerResponse data, int queryType) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	
        if(data != null && data.getCode() != null) {
            String code = data.getCode();
 
            if(code.equals(YodoGlobals.AUTHORIZED)) {
            	switch(queryType) {
	                case AUTH_REQ:
	                	requestPIPReset(currentPip, newPip);
		            	currentPip = "";
	                    newPip = "";
	                	//requestBiometricToken(currentPip);
	                break;
	                
	                case BIO_REQ:
	                	String biometricToken = data.getParamsWithoutTime()[0];
	                	
	                	if(!biometricToken.equals(YodoGlobals.USER_BIOMETRIC)) {
		                	authNumber = data.getAuthNumber();
		                	
		                	Intent intent = new Intent(YodoResetPip.this, YodoCamera.class);
			            	intent.putExtra(YodoGlobals.ID_TOKEN, biometricToken);
			            	startActivityForResult(intent, REQUEST_FACE_ACTIVITY);
	                	} else {
	                		ToastMaster.makeText(YodoResetPip.this, R.string.no_biometric, Toast.LENGTH_SHORT).show();
	                	}
	                break;
	                
	                case CPIP_REQ:
                        handlerMessages.sendEmptyMessage(YodoGlobals.SUCCESS);
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
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {      
            case(REQUEST_FACE_ACTIVITY):
            	if(resultCode == RESULT_OK) {
            		if(DEBUG)
            			Log.i(TAG, "Biometric Success");
            		
            		requestPIPResetBio(authNumber, newPip);
            		authNumber = "";
                    newPip = "";
            	}
            break;
        }
    }
}
