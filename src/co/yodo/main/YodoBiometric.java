package co.yodo.main;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.view.View;
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

public class YodoBiometric extends ActionBarActivity implements TaskFragment.YodoCallback {
	/*!< DEBUG */
	private final static String TAG = YodoBiometric.class.getName();
	
	/*!< Result Activities Identifiers */
    private static final int FACE_ACTIVITY = 1;
    
    /*!< ID for queries */
    private final static int AUTH_REQ = 0;
    private final static int BIO_REQ  = 1;
    
    /*!< Variable used as an authentication number */
    private static String hrdwToken;
    private String authNumber;
    private String biometricToken;
    
    /*!< Messages Handler */
    private static YodoHandler handlerMessages;
    
    /*!< Fragment Information */
    private TaskFragment mTaskFragment;
    private ProgressDialog progDialog;
	private AlertDialog alertDialog;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.changeLanguage(this);
    	setContentView(R.layout.activity_yodo_biometric);
    	
    	setupGUI();
        updateData();
    }
    
    private void setupGUI() {
    	handlerMessages = new YodoHandler(this);
    	getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    	
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
    	
    	Bundle bundle = this.getIntent().getExtras();
        
        if(bundle != null)
        	authNumber = bundle.getString(YodoGlobals.ID_AUTHORIZATION);
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
    	if(biometricToken == null || biometricToken.equals("")) {
    		ToastMaster.makeText(YodoBiometric.this, R.string.no_biometric, Toast.LENGTH_SHORT).show();
			return;
    	} else {
    		requestHardwareAuthorization();
    	}
    }
    
    /**
     * Connects to the switch and gets the user authorization
     */
    private void requestHardwareAuthorization() {
    	String data = YodoQueries.requestHardwareAuthorization(this, hrdwToken);
    	
    	SwitchServer request = mTaskFragment.getSwitchServerInstance();
    	request.setType(AUTH_REQ);
    	request.setDialog(true, getString(R.string.auth_message));
    	
        mTaskFragment.start(request, SwitchServer.AUTH_HW_REQUEST, data);
    }
    
    /**
	 * Connects to the switch and send the user data
	 * @return boolean True if the registration is successfully, else false 
	 */
	private void requestBiometricRegistration() {
		String data = YodoQueries.requestBiometricRegistration(this, authNumber, biometricToken);
    	
    	SwitchServer request = mTaskFragment.getSwitchServerInstance();
    	request.setType(BIO_REQ);
    	request.setDialog(true, getString(R.string.registering_user_biometric));
    	
        mTaskFragment.start(request, SwitchServer.BIO_REG_REQUEST, data);
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

	@SuppressWarnings("unused")
	@Override
	public void onTaskCompleted(ServerResponse data, int queryType) {
		AlertDialog.Builder builder = new AlertDialog.Builder(YodoBiometric.this);
		
		Utils.Logger(TAG, data.toString());
		
        if(data != null) {
            String code = data.getCode();
            if(code.equals(YodoGlobals.AUTHORIZED)) {
            	switch(queryType) {
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
        } else 
        	handlerMessages.sendEmptyMessage(YodoGlobals.GENERAL_ERROR);
	}
	
	@Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case(FACE_ACTIVITY) : {
                if(resultCode == RESULT_OK) {
                    biometricToken = data.getStringExtra(YodoGlobals.FACE_DATA);
                    ToastMaster.makeText(YodoBiometric.this, R.string.saved_face, Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }

}
