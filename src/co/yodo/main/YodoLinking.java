package co.yodo.main;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import co.yodo.R;
import co.yodo.helper.CreateAlertDialog;
import co.yodo.helper.ToastMaster;
import co.yodo.helper.Utils;
import co.yodo.helper.YodoGlobals;
import co.yodo.helper.YodoHandler;
import co.yodo.helper.YodoQueries;
import co.yodo.serverconnection.ServerResponse;
import co.yodo.serverconnection.TaskFragment;
import co.yodo.serverconnection.TaskFragment.SwitchServer;

public class YodoLinking extends ActionBarActivity implements TaskFragment.YodoCallback {
	/*!< DEBUG */
	private final static String TAG = YodoLinking.class.getName();
	private final static boolean DEBUG = false;
	
	/*!< Variable used as an authentication number */
	private static String hrdwToken;
	
	/*!< Messages Handler */
    private static YodoHandler handlerMessages;
    
    /*!< ID for queries */
    private final static int CODE_REQ = 0;
    private final static int LINK_REQ = 1;
    
    /*!< GUI Controllers */
    private EditText inputBox;
	
	/*!< Fragment Information */
    private TaskFragment mTaskFragment;
    private ProgressDialog progDialog;
    private AlertDialog alertDialog;
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.changeLanguage(this);
    	setContentView(R.layout.activity_yodo_linking);
    	
    	setupGUI();
        updateData();
    }
	
	private void setupGUI() {
		handlerMessages   = new YodoHandler(this);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		progDialog = new ProgressDialog(this);
		
		// Load Fragment Manager
    	FragmentManager fm = getSupportFragmentManager();
	    mTaskFragment = (TaskFragment) fm.findFragmentByTag(YodoGlobals.TAG_TASK_FRAGMENT);
	    
	    if(mTaskFragment == null) {
	    	mTaskFragment = new TaskFragment();
	    	fm.beginTransaction().add(mTaskFragment, YodoGlobals.TAG_TASK_FRAGMENT).commit();
	    }
	}
	
	private void updateData() {
		hrdwToken = Utils.getHardwareToken(this);
	}
	
	/**
     * Dialog Button Actions
     * */
    public void showPressed(View v) {
        if(((CheckBox)v).isChecked())
            inputBox.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        else
            inputBox.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
    }
    
    @SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	public void copyCode(String text) {
    	int sdk = android.os.Build.VERSION.SDK_INT;
    	if(sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
    	    android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
    	    clipboard.setText(text);
    	} else {
    	    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE); 
    	    android.content.ClipData clip = android.content.ClipData.newPlainText("Copied", text);
    	    clipboard.setPrimaryClip(clip);
    	}
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
		return super.onOptionsItemSelected(item);
	}
	
	public void linkingOptions(View v) {
		final CharSequence[] items = {
				"Input Linking Code", "Generate Linking Code"
		};

	    AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    builder.setIcon(R.drawable.ic_launcher);
	    builder.setTitle(getString(R.string.linking_menu));
	    builder.setItems(items, new DialogInterface.OnClickListener() {
	    	@Override
	    	public void onClick(DialogInterface dlg, int item) {
	    		if(item == 0) {
    				// Input Linking Code dialog
    		        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
    		        View layout = inflater.inflate(R.layout.dialog_text, new LinearLayout(YodoLinking.this), false);
    		        inputBox = (EditText) layout.findViewById(R.id.dialogInputBox);

    		        // Listener to click event on the dialog in order to view the SKS code
    		        DialogInterface.OnClickListener pipDialogOkButtonClickListener = new DialogInterface.OnClickListener() {
    		            public void onClick(DialogInterface dialog, int which) {
    		                String linkingCode = inputBox.getText().toString();
    		                requestLinkingAccount(linkingCode);
    		            }
    		        };

    		        CreateAlertDialog.showAlertDialog(YodoLinking.this, layout, inputBox,
    		                null,
    		                null,
    		                pipDialogOkButtonClickListener,
    		                null);
	    		}
	    		else if(item == 1) {
    				// Input PIP dialog
    		        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
    		        View layout = inflater.inflate(R.layout.dialog_password, new LinearLayout(YodoLinking.this), false);
    		        inputBox = (EditText) layout.findViewById(R.id.dialogInputBox);

    		        DialogInterface.OnClickListener pipDialogOkButtonClickListener = new DialogInterface.OnClickListener() {
    		        	@Override
    		            public void onClick(DialogInterface dialog, int which) {
    		                String temp_pip = inputBox.getText().toString();

    		                // Check PIP
    		                if(temp_pip == null || temp_pip.length() < YodoGlobals.MIN_PIP_LENGTH) {
    		                    ToastMaster.makeText(YodoLinking.this, R.string.pip_length_short, Toast.LENGTH_SHORT).show();
    		                }
    		                else {
    		                	requestLinkingCode(temp_pip);
    		                }
    		            }
    		        };
    		        
    		        DialogInterface.OnClickListener pipDialogCancelButtonClickListener = new DialogInterface.OnClickListener() {
    		        	@Override
    		            public void onClick(DialogInterface dlg, int which) {
    		        		dlg.dismiss();
    		            }
    		        };

    		        CreateAlertDialog.showAlertDialog(YodoLinking.this, layout, inputBox,
    		                getString(R.string.input_pip),
    		                null,
    		                pipDialogOkButtonClickListener,
    		                pipDialogCancelButtonClickListener);
    			}
	        }
	    });
	    
	    AlertDialog alert = builder.create();
	    alert.show();
	}
	
	/**
     * Connects to the switch and request a linking code
     */
    private void requestLinkingCode(String pip) {
        String data = YodoQueries.requestLinkingCode(this, hrdwToken, pip);

        SwitchServer request = mTaskFragment.getSwitchServerInstance();
        request.setType(CODE_REQ);
        request.setDialog(true, getString(R.string.linking_code_message));
  
        mTaskFragment.start(request, SwitchServer.LINKING_CODE_REQUEST, data);
    }
    
    /**
     * Connects to the switch and request to link an account
     */
    private void requestLinkingAccount(String linkingCode) {
    	long time = System.currentTimeMillis();
        String data = YodoQueries.requestLinkingAccount(this, hrdwToken, linkingCode, String.valueOf(time));

        SwitchServer request = mTaskFragment.getSwitchServerInstance();
        request.setType(LINK_REQ);
        request.setDialog(true, getString(R.string.linking_acc_message));
  
        mTaskFragment.start(request, SwitchServer.LINKING_ACC_REQUEST, data);
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
	public void onTaskCompleted(ServerResponse data, int queryType) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	
        if(data != null && data.getCode() != null) {
            String code = data.getCode();
 
            if(code.equals(YodoGlobals.AUTHORIZED)) {
            	
            	DialogInterface.OnClickListener CancelButtonClickListener = new DialogInterface.OnClickListener() {
                	@Override
                    public void onClick(DialogInterface dlg, int which) {
                		dlg.dismiss();
                    }
                };
                
            	switch(queryType) {
	                case CODE_REQ:
	                	String generatedCode = data.getParams();
	                	int end              = generatedCode.indexOf(ServerResponse.ENTRY_SEPARATOR + ServerResponse.TIME_ELEM);
	                	generatedCode        = generatedCode.substring(0, end);
	                	
	                	if(DEBUG)
	                		Log.e(TAG, generatedCode);
	                	
	                	Dialog dialog = new Dialog(YodoLinking.this);
	                    dialog.getWindow();
	                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
	                    dialog.setContentView(R.layout.dialog_linking);

	                    final TextView codeText   = (TextView) dialog.findViewById(R.id.codeText);
	                    ImageView codeImage = (ImageView) dialog.findViewById(R.id.copyCodeImage);
	                    codeText.setText(generatedCode);
	                    
	                    codeImage.setOnClickListener(new OnClickListener() {
	                        @Override
	                        public void onClick(View v) {
	                        	copyCode(codeText.getText().toString());
	                        	ToastMaster.makeText(YodoLinking.this, R.string.copied_text, Toast.LENGTH_SHORT).show();
	                        }
	                    });
	                    
	                    dialog.show();
	                break;
	                
	                case LINK_REQ:
	                	String text = data.getParams();
	                	int end1    = text.indexOf(ServerResponse.ENTRY_SEPARATOR + ServerResponse.TIME_ELEM);
	                	text        = text.substring(0, end1);
	                	
	                	if(DEBUG)
	                		Log.e(TAG, text);
	                	
	                	CreateAlertDialog.showAlertDialog(YodoLinking.this,
                                getString(R.string.link_account_response),
                                text,
                                CancelButtonClickListener);
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
