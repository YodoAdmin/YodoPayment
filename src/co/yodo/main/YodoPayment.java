package co.yodo.main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import co.yodo.R;
import co.yodo.database.CouponsDataSource;
import co.yodo.database.ReceiptsSQLiteHelper;
import co.yodo.helper.AdvertisingService;
import co.yodo.helper.CreateAlertDialog;
import co.yodo.helper.ToastMaster;
import co.yodo.helper.Utils;
import co.yodo.helper.YodoGlobals;
import co.yodo.helper.YodoHandler;
import co.yodo.helper.YodoQueries;
import co.yodo.photoview.PhotoViewAttacher;
import co.yodo.serverconnection.ServerResponse;
import co.yodo.serverconnection.TaskFragment;
import co.yodo.serverconnection.TaskFragment.SwitchServer;
import co.yodo.sks.SKSCreater;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SlidingPaneLayout;
import android.support.v4.widget.SlidingPaneLayout.LayoutParams;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class YodoPayment extends ActionBarActivity implements TaskFragment.YodoCallback {
	/*!< DEBUG */
	private final static String TAG = "YodoPayment";
	private final static boolean DEBUG = true;
	
	/*!< SKS time to dismiss milliseconds */
    private static final int TIME_TO_DISMISS_SKS = 60000;
    
    /*!< SKS data separator */
    private static final String SKS_SEP = "**";
    
    /*!< Database */
    private ReceiptsSQLiteHelper receiptsdb;
    private SQLiteDatabase db;
    private CouponsDataSource couponsdb;
    
    /*!< Bluetooth */
    private BluetoothAdapter mBluetoothAdapter;
    private boolean advertising;
    private String actualMerch = "";
	
	/*!< Variable used as an authentication number */
    private static final String KEY_TEMP_PIP = "key_temp_pip";
    private static String hrdwToken;
    private String temp_pip = "";
	
	/*!< GUI Controllers */
    private TextView accNumberText;
    private TextView accDateText;
    private TextView accBalanceText;
    private EditText inputBox;
	private ImageView advertisingImage;
	private Bitmap bmAdvertising;
	private SlidingPaneLayout mSlidingLayout;
	private PhotoViewAttacher mAttacher;
	private ScrollView mNavigation;
	
	/*!< Different Biometric */
    //private boolean isDefine = false;
	
	/*!< Receiver */
	private DevicesReceiver myReceiver;
	private boolean started = false;
	
	/*!< Activity Result */
    private static final int REQUEST_ENABLE_BLUETOOTH = 0;
    private static final int REQUEST_FACE_ACTIVITY    = 1;
	
	/*!< ID for queries */
    private final static int AUTH_REQ = 0;
    private final static int BAL_REQ  = 1;
    private final static int REC_REQ  = 2;
    private final static int CLS_REQ  = 3;
    private final static int ADS_REQ  = 4;
    private final static int BIO_REQ  = 5;
	
    /*!< QR Bitmap */
    private Bitmap qrCode;
    
	/*!< Preferences */
	private SharedPreferences settings;
	
	/*!< Messages Handler */
    private static YodoHandler handlerMessages;
    
    /*!< Fragment Information */
    private TaskFragment mTaskFragment;
    private ProgressDialog progDialog;
    private AlertDialog alertDialog;
    private String message;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Utils.changeLanguage(this);
		setContentView(R.layout.activity_yodo_payment);
		
		setupGUI();
        updateData();
	    
	    // Restore saved state.
	    if(savedInstanceState != null && savedInstanceState.getBoolean(YodoGlobals.KEY_IS_SHOWING)) {
	    	temp_pip = savedInstanceState.getString(KEY_TEMP_PIP);
	    	message =  savedInstanceState.getString(YodoGlobals.KEY_MESSAGE);
	    	
	    	Utils.showProgressDialog(progDialog, message);
	    } 
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
	    outState.putBoolean(YodoGlobals.KEY_IS_SHOWING, progDialog.isShowing());
	    outState.putString(YodoGlobals.KEY_MESSAGE, message);
	    
	    if(temp_pip != null)
	    	outState.putString(KEY_TEMP_PIP, temp_pip);
	}
	
	@Override
    protected void onResume() {
    	super.onResume();
    	Utils.Logger(DEBUG, TAG, "onResume");

    	IntentFilter intentFilter = new IntentFilter();
	    intentFilter.addAction(YodoGlobals.DEVICES_BT);
	    registerReceiver(myReceiver, intentFilter);
	 
        if(advertising && mBluetoothAdapter != null) {
    		setupBluetooth(); 
    	}
        
        if(couponsdb != null) 
        	couponsdb.open();
    }
	
	@Override
    protected void onPause() {
        super.onPause();
        Utils.Logger(DEBUG, TAG, "onPause");
        
        if(myReceiver != null)
        	unregisterReceiver(myReceiver);
        
        if(mBluetoothAdapter != null) {
        	processStopService(AdvertisingService.TAG);
        }
        
        // Close open databases
        if(receiptsdb != null)
        	receiptsdb.close();
        
        if(couponsdb != null) 
        	couponsdb.close();
        
        System.gc();
        Runtime.getRuntime().gc();
    }
	
	@Override
    protected void onDestroy() {
        super.onDestroy();  
        
        if(progDialog != null && progDialog.isShowing())
        	progDialog.cancel();
        
        if(mBluetoothAdapter != null) {
        	processStopService(AdvertisingService.TAG);
        }
    }
	
	@Override
    public void onBackPressed() {
    	if(mSlidingLayout.isOpen()) {
    		mSlidingLayout.closePane();
    	}
    	else {
    		super.onBackPressed();
    	}
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.yodo_payment, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
			case android.R.id.home:
				if(mSlidingLayout.isOpen()) {
					mSlidingLayout.closePane();
				} else {
					mSlidingLayout.openPane();
				}
			break;
			
			case R.id.action_settings:
				int languagePosition = settings.getInt(YodoGlobals.ID_LANGUAGE, YodoGlobals.DEFAULT_LANGUAGE);
        		String code = getResources().getConfiguration().locale.getLanguage();
        		
        		AlertDialog.Builder builder = new AlertDialog.Builder(YodoPayment.this);
        		builder.setInverseBackgroundForced(true);
        		View v = getLayoutInflater().inflate(R.layout.dialog_settings, null);
        		
        		if(languagePosition == -1 && (Arrays.asList(YodoGlobals.lang_code).contains(code))) {
        			languagePosition = Arrays.asList(YodoGlobals.lang_code).indexOf(code);
        		} else if(languagePosition == -1)
        			languagePosition = 0;
        		
        		final Spinner languagesSpinner = (Spinner) v.findViewById(R.id.languagesSpinner);
        		final CheckBox adsCheckBox     = (CheckBox) v.findViewById(R.id.advertisingCheckBox);
        		
        		ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_list_item_1, YodoGlobals.languages);
        		languagesSpinner.setAdapter(adapter);
        		languagesSpinner.setSelection(languagePosition);
        		
        		adsCheckBox.setChecked(advertising);
        		if(mBluetoothAdapter == null) {
        			adsCheckBox.setEnabled(false);
        		}
        		
        		builder.setTitle(getString(R.string.action_settings));
        		builder.setView(v);
        		builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    	SharedPreferences.Editor editor = settings.edit();
        				
                    	editor.putInt(YodoGlobals.ID_LANGUAGE, languagesSpinner.getSelectedItemPosition());
                    	editor.putBoolean(YodoGlobals.ID_ADVERTISING, adsCheckBox.isChecked());
        				
        				editor.commit();
                        dialog.dismiss();
                        
                        finish();
        				startActivity(getIntent());
                    }
                });
        		builder.setNegativeButton(getString(R.string.cancel), null);
        		
        		alertDialog = builder.create();
                alertDialog.show();
			break;
			
			case R.id.menu_exit:
    			finish();
        	break;

			default:
            break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void setupGUI() {
    	handlerMessages   = new YodoHandler(this);
    	mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    	getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    	
    	// Load Fragment Manager
    	FragmentManager fm = getSupportFragmentManager();
	    mTaskFragment = (TaskFragment) fm.findFragmentByTag(YodoGlobals.TAG_TASK_FRAGMENT);
	    
	    if(mTaskFragment == null) {
	    	mTaskFragment = new TaskFragment();
	    	fm.beginTransaction().add(mTaskFragment, YodoGlobals.TAG_TASK_FRAGMENT).commit();
	    }
    	
	    progDialog = new ProgressDialog(this);
	    
    	// Load Databases
    	receiptsdb = new ReceiptsSQLiteHelper(this, YodoGlobals.DB_NAME, null, 1);
        db = receiptsdb.getWritableDatabase();
        
        couponsdb = new CouponsDataSource(this);
        couponsdb.open();
        
        // Bluetooth Devices Broadcast
        myReceiver = new DevicesReceiver();
    	
        // GUI initialize
    	accNumberText    = (TextView) findViewById(R.id.accountNumberText);
        accDateText 	 = (TextView) findViewById(R.id.dateText);
        accBalanceText 	 = (TextView) findViewById(R.id.balanceText);
		advertisingImage = (ImageView) findViewById(R.id.advertisingImage);
		mSlidingLayout   = (SlidingPaneLayout) findViewById(R.id.sliding_pane_layout);
		mNavigation      = (ScrollView) findViewById(R.id.navigationScroll);
		
		// Handle The Size of the Sidebar
		int orientation = getResources().getConfiguration().orientation; 
    	
    	if(Configuration.ORIENTATION_LANDSCAPE == orientation) { 
    		DisplayMetrics dm = new DisplayMetrics();
    		this.getWindow().getWindowManager().getDefaultDisplay().getMetrics(dm);
    		int width = dm.widthPixels;

    		LayoutParams params = new LayoutParams((int) (width * 0.75), LayoutParams.MATCH_PARENT);
    		mNavigation.setLayoutParams(params);
    	} 
		
		mAttacher = new PhotoViewAttacher(advertisingImage);
		mAttacher.setOnLongClickListener(new OnLongClickListener() {
		    @Override
		    public boolean onLongClick(View v) {
		    	if(advertisingImage.getDrawable() != null) {
			    	AlertDialog.Builder builder = new AlertDialog.Builder(YodoPayment.this);
		            builder.setMessage(getString(R.string.save_image));
		            builder.setCancelable(true);
		            
		            builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
		                public void onClick(DialogInterface dialog, int id) {
		                	BitmapDrawable drawable = (BitmapDrawable) advertisingImage.getDrawable();
		                    Bitmap bitmap = drawable.getBitmap();
		                    
		                    File directory = new File(Environment.getExternalStorageDirectory(), "/Yodo");
		                    boolean success = true;
		                    if(!directory.exists()) {
		                        success = directory.mkdir();
		                    }
		                    
		                    if(success) {
			                    int files = directory.listFiles().length;
			                    File image = new File(directory, "ad" + (files++) + ".png");
			                    
			                    Utils.Logger(DEBUG, TAG, image.toString());
			                    success = false;
			                    // Encode the file as a PNG image.
			                    FileOutputStream outStream;
			                    try {
			                        outStream = new FileOutputStream(image);
			                        bitmap.compress(Bitmap.CompressFormat.PNG, 90, outStream); 
	
			                        outStream.flush();
			                        outStream.close();
			                        success = true;
			                        
        							couponsdb.createCoupon(image.getPath(), actualMerch);
			                    } catch (FileNotFoundException e) {
			                        e.printStackTrace();
			                    } catch (IOException e) {
			                        e.printStackTrace();
			                    }
		                    }
		                    
		                    if(success) {
		                        Toast.makeText(YodoPayment.this, R.string.image_saved_ok, Toast.LENGTH_SHORT).show();
		                    } else {
		                        Toast.makeText(YodoPayment.this, R.string.image_saved_failed, Toast.LENGTH_SHORT).show();
		                    }
		                    
		                    dialog.cancel();
		                }
		            });
		            
		            builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
		                public void onClick(DialogInterface dialog, int id) {
		                    dialog.cancel();
		                }
		            });
	
		            alertDialog = builder.create();
		            alertDialog.show();
		    	}
		        return true;
		    }
		});
    }
    
    private void updateData() {
    	hrdwToken = Utils.getHardwareToken(this);
    	
    	settings = getSharedPreferences(YodoGlobals.PREFERENCES, Context.MODE_PRIVATE);
        advertising = settings.getBoolean(YodoGlobals.ID_ADVERTISING, YodoGlobals.DEFAULT_ADS);

        if(advertising && mBluetoothAdapter == null) {
        	advertising = false;
        	SharedPreferences.Editor editor = settings.edit();
        	editor.putBoolean(YodoGlobals.ID_ADVERTISING, advertising);
			editor.commit();
        }
        
        boolean use = settings.getBoolean(YodoGlobals.ID_FIRST_USE, YodoGlobals.DEFAULT_USE);
    	if(use) {
    		mSlidingLayout.openPane();
    		
    		AlertDialog.Builder builder = new AlertDialog.Builder(YodoPayment.this);
			builder.setTitle(getString(R.string.instructions_title));
        	builder.setMessage(getString(R.string.instructions_message));
        	builder.setPositiveButton(getString(R.string.ok), null);
        	alertDialog = builder.create();
        	alertDialog.show();
        	
    		SharedPreferences.Editor editor = settings.edit();
			editor.putBoolean(YodoGlobals.ID_FIRST_USE, false);
			editor.commit();
    	}

    	accNumberText.setText(hrdwToken);
        accDateText.setText(getDate());
    }
    
    private void processStartService(final String tag) {
    	if(!started) {
    		Intent intent = new Intent(getApplicationContext(), AdvertisingService.class);
    	    intent.addCategory(tag);
    	    startService(intent);
    	}
    	started = true;
	}
	
	private void processStopService(final String tag) {
	    Intent intent = new Intent(getApplicationContext(), AdvertisingService.class);
	    intent.addCategory(tag);
	    stopService(intent);
	    started = false;
	}
	
	private void setupBluetooth() {    	
		if(mBluetoothAdapter != null) {
			if(mBluetoothAdapter.isEnabled()) {
				processStartService(AdvertisingService.TAG);
			} 
			else {
				Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableBTIntent, REQUEST_ENABLE_BLUETOOTH);
			}
		}
	}
    
    /**
     * Gets the actual date formated
     * @return	String actual date
     */
    private String getDate() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH) + 1;
        int day = c.get(Calendar.DAY_OF_MONTH);
        String sMonth = (month < 10) ? "0" + month : "" + month;
        String sDay   = (day   < 10) ? "0" + day   : "" + day;
        return year +"/"+ (sMonth) +"/"+ sDay;
    }
    
    /**
     * Method to show the dialog containing the SKS code
     * @param qrBitmap
     */
    private void showSKSDialog(String code) {
    	//retrieve display dimensions
        /*Rect displayRectangle = new Rect();
        Window window = getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);
        SKSCreater.setWidth((int)(displayRectangle.width() * 0.8f));*/
    	try {
			qrCode = SKSCreater.createSKS(code, YodoPayment.this, SKSCreater.SKS_CODE);
    	
	    	final Dialog sksDialog = new Dialog(this);
	        
	        sksDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
	        sksDialog.setContentView(R.layout.dialog_sks);
	        sksDialog.setCancelable(false);
	        
	        // brightness
	        final WindowManager.LayoutParams lp = getWindow().getAttributes();
	        final float brightnessNow = lp.screenBrightness;
	        
	        sksDialog.setOnShowListener(new DialogInterface.OnShowListener() {
				@Override
				public void onShow(DialogInterface dialog) {
					lp.screenBrightness = 100 / 100.0f;
			        getWindow().setAttributes(lp);
				}
	        });
	        
	        sksDialog.setOnKeyListener(new Dialog.OnKeyListener() {
				@Override
				public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
					if(keyCode == KeyEvent.KEYCODE_BACK) {
	                    dialog.dismiss();
	                }
					return true;
				}
	        });
	        
	        sksDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
	            @Override
	            public void onDismiss(DialogInterface dialog) {
	            	if(qrCode != null) {
	            		qrCode.recycle();
	            		qrCode = null;
	            	}
	            	
	        		lp.screenBrightness = brightnessNow;
	        		getWindow().setAttributes(lp);
	        		requestReceipt(temp_pip);
	        		
	        		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
	            }
	        });
	        
	        ImageView image = (ImageView) sksDialog.findViewById(R.id.sks);
	        image.setImageBitmap(qrCode);
	        
	        int currentOrientation = getResources().getConfiguration().orientation;
	        if(currentOrientation == Configuration.ORIENTATION_LANDSCAPE) 
	        	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
	        else
	        	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
	        
	        sksDialog.show();
	
	        final Timer t = new Timer();
	        t.schedule(new TimerTask() {
	            public void run() {
	            	sksDialog.dismiss(); /// When the task active then close the dialog
	                t.cancel(); /// Also just top the timer thread, otherwise, you may receive a crash report
	            }
	        }, TIME_TO_DISMISS_SKS);
    	} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
    }
    
    private void receiptDialog(String receiptData) {
    	final Dialog receipt = new Dialog(YodoPayment.this);
        receipt.requestWindowFeature(Window.FEATURE_NO_TITLE);

        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.dialog_receipt, null);

        TextView description = (TextView)layout.findViewById(R.id.descriptionText);
        TextView authNumber = (TextView)layout.findViewById(R.id.authNumberText);
        TextView created = (TextView)layout.findViewById(R.id.createdText);
        TextView paid = (TextView)layout.findViewById(R.id.paidText);
        TextView tender = (TextView)layout.findViewById(R.id.cashTenderText);
        TextView cashBack = (TextView)layout.findViewById(R.id.cashBackText);
        //TextView balance = (TextView)layout.findViewById(R.id.yodoBalanceText);
        ImageView deleteButton = (ImageView)layout.findViewById(R.id.deleteButton);
        ImageView saveButton = (ImageView)layout.findViewById(R.id.saveButton);

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                receipt.dismiss();
            }
        });

        if(!db.isOpen())
            db = receiptsdb.getWritableDatabase();

        String query = "INSERT INTO " + ReceiptsSQLiteHelper.TABLE_RECEIPTS +
                " (" + ReceiptsSQLiteHelper.COLUMN_DESCRIPTION +
                ", " + ReceiptsSQLiteHelper.COLUMN_CREATED +
                ", " + ReceiptsSQLiteHelper.COLUMN_AMOUNT +
                ", " + ReceiptsSQLiteHelper.COLUMN_TENDER +
                ", " + ReceiptsSQLiteHelper.COLUMN_CASHBACK +
                ", " + ReceiptsSQLiteHelper.COLUMN_AUTHNUMBER +
                ", " + ReceiptsSQLiteHelper.COLUMN_BALANCE + ") VALUES ('";

        DecimalFormat twoDForm = new DecimalFormat("#.##");
        String aParams[] = receiptData.split(ServerResponse.ENTRY_SEPARATOR);

        for(String param : aParams) {
            String aValParams[] = param.split(ServerResponse.VALUE_SEPARATOR);

            if(aValParams[0].equals(ServerResponse.DESCRIPTION_ELEM)) {
                query += aValParams[1] + "', '";
                description.setText(aValParams[1]);
            }

            else if(aValParams[0].equals(ServerResponse.CREATED_ELEM)) {
                query += aValParams[1] + "', ";
                created.setText(aValParams[1]);
            }

            else if(aValParams[0].equals(ServerResponse.AMOUNT_ELEM)) {
                double tempPaid = Double.valueOf(twoDForm.format(Double.valueOf(aValParams[1])).replace(",", "."));
                query += tempPaid + ", ";
                paid.setText(String.valueOf(tempPaid));
            }

            else if(aValParams[0].equals(ServerResponse.TENDER_ELEM)) {
                double tempTender = Double.valueOf(twoDForm.format(Double.valueOf(aValParams[1])).replace(",", "."));
                query += tempTender + ", ";
                tender.setText(String.valueOf(tempTender));
            }

            else if(aValParams[0].equals(ServerResponse.CASHBACK_ELEM)) {
                double tempCashBack = Double.valueOf(twoDForm.format(Double.valueOf(aValParams[1])).replace(",", "."));
                query += tempCashBack + ", ";
                cashBack.setText(String.valueOf(tempCashBack));
            }

            else if(aValParams[0].equals(ServerResponse.RECEIVE_ELEM)) {
                query += aValParams[1] + ", ";
                authNumber.setText(aValParams[1]);
            }
            
            else if(aValParams[0].equals(ServerResponse.BALANCE_ELEM)) {
            	double tempBalance = Double.valueOf(twoDForm.format(Double.valueOf(aValParams[1])).replace(",", "."));
                query += tempBalance + ")";
                //balance.setText(String.valueOf(tempBalance));
                accBalanceText.setText(String.valueOf(tempBalance));
            }
        }
        final String finalQuery = query;
        Utils.Logger(DEBUG, TAG, finalQuery);
        
        saveButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	if(db != null) {
	            	db.execSQL(finalQuery);
	            	db.close();
	            	ToastMaster.makeText(YodoPayment.this, R.string.saved_receipt, Toast.LENGTH_SHORT).show();
            	}
            	receipt.dismiss();
            }
        });
        
        receipt.setCancelable(false);
        receipt.setContentView(layout);
        receipt.show();
    }
    
    /**
     * Navigation Button Actions
     */
    public void resetPipClick(View v) {
    	if(mSlidingLayout.isOpen())
    		mSlidingLayout.closePane();
    		
        Intent intent = new Intent(YodoPayment.this, YodoResetPip.class);
        startActivity(intent);
    }
    
    public void linksClick(View v) {
    	if(mSlidingLayout.isOpen())
    		mSlidingLayout.closePane();
    	
    	ToastMaster.makeText(YodoPayment.this, R.string.not_available, Toast.LENGTH_SHORT).show();
    }
    
    public void pairClick(View v) {
    	if(mSlidingLayout.isOpen())
    		mSlidingLayout.closePane();
    	
    	ToastMaster.makeText(YodoPayment.this, R.string.not_available, Toast.LENGTH_SHORT).show();
    }
    
    public void savedReceiptsClick(View v) {
    	if(mSlidingLayout.isOpen())
    		mSlidingLayout.closePane();
    	
        Intent intent = new Intent(YodoPayment.this, YodoReceipts.class);
        startActivity(intent);
    }
    
    public void balanceClick(View v) {
    	if(mSlidingLayout.isOpen())
    		mSlidingLayout.closePane();
    	
        // Input PIP dialog
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.dialog_password, null);
        inputBox = (EditText) layout.findViewById(R.id.dialogInputBox);

        // Listener to click event on the dialog in order to view the sks code
        DialogInterface.OnClickListener pipDialogOkButtonClickListener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String temp_pip = inputBox.getText().toString();

                // Check PIP
                if(temp_pip == null || temp_pip.length() < YodoGlobals.MIN_PIP_LENGTH) {
                    ToastMaster.makeText(YodoPayment.this, R.string.pip_length_short, Toast.LENGTH_SHORT).show();
                }
                else {
                    requestBalance(temp_pip);
                }
            }
        };

        CreateAlertDialog.showAlertDialog(this, layout, inputBox,
                getString(R.string.input_pip),
                null,
                pipDialogOkButtonClickListener,
                null);
    }
    
    /**
     * Main Layout Button Actions
     * */
    public void paymentClick(View v) {
    	advertisingImage.setImageDrawable(null);
    	
        // Input PIP dialog
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.dialog_password, null);
        inputBox = (EditText) layout.findViewById(R.id.dialogInputBox);

        // Listener to click event on the dialog in order to view the sks code
        DialogInterface.OnClickListener pipDialogOkButtonClickListener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                temp_pip = inputBox.getText().toString();

                // Check PIP
                if(temp_pip.length() < YodoGlobals.MIN_PIP_LENGTH) {
                    ToastMaster.makeText(YodoPayment.this, R.string.pip_length_short, Toast.LENGTH_SHORT).show();
                }
                else {
                	processStopService(AdvertisingService.TAG);
                	requestSKSAuthentication(temp_pip);
                }
            }
        };

        CreateAlertDialog.showAlertDialog(this, layout, inputBox,
                getString(R.string.input_pip),
                null,
                pipDialogOkButtonClickListener,
                null);
    }
    
    public void closeAccClick(View v) {
    	if(mSlidingLayout.isOpen())
    		mSlidingLayout.closePane();
    	
        // Input PIP dialog
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.dialog_password, null);
        inputBox = (EditText) layout.findViewById(R.id.dialogInputBox);

        // Listener to click event on the dialog in order to view the sks code
        DialogInterface.OnClickListener pipDialogOkButtonClickListener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                temp_pip = inputBox.getText().toString();

                // Check PIP
                if(temp_pip == null || temp_pip.length() < YodoGlobals.MIN_PIP_LENGTH) {
                    ToastMaster.makeText(YodoPayment.this, R.string.pip_length_short, Toast.LENGTH_SHORT).show();
                }
                else {
                	requestCloseAccount(temp_pip);
                }
            }
        };

        CreateAlertDialog.showAlertDialog(YodoPayment.this, layout, inputBox,
                getString(R.string.input_pip),
                getString(R.string.close_message),
                pipDialogOkButtonClickListener,
                null);
    }
    
    public void couponClick(View v) {
    	Intent intent = new Intent(YodoPayment.this, YodoCoupons.class);
        startActivity(intent);
    }
    
    public void networkClick(View v) {
    	ToastMaster.makeText(YodoPayment.this, R.string.not_available, Toast.LENGTH_SHORT).show();
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
    
    /**
     * Connects to the switch and authenticate the user
     */
    private void requestSKSAuthentication(String pip) {
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
	/*private void requestBiometricToken(String pip) {
		StringBuilder userData = new StringBuilder();
		String sEncryptedUsrData;

		userData.append(HARDWARE_TOKEN).append(REQ_SEP);
		userData.append(pip).append(REQ_SEP);
		userData.append(YodoGlobals.QUERY_BIO);
		
		/// Encrypting user's data to create request
		this.getEncrypter().setsUnEncryptedString(userData.toString());
		this.getEncrypter().rsaEncrypt(this);
		sEncryptedUsrData = this.getEncrypter().bytesToHex();
		
		SwitchServer request = new SwitchServer(this);
        request.setType(BIO_REQ);
        request.setDialog(true, getString(R.string.biometric_message));
        request.execute(SwitchServer.BIOMETRIC_REQUEST, sEncryptedUsrData);
	}*/

    /**
     * Connects to the switch and close the account
     */
    private void requestCloseAccount(String pip) {
        String data = YodoQueries.requestCloseAccount(this, hrdwToken, pip);

        SwitchServer request = mTaskFragment.getSwitchServerInstance();
        request.setType(CLS_REQ);
        request.setDialog(true, getString(R.string.closing_message));
  
        mTaskFragment.start(request, SwitchServer.CLOSE_REQUEST, data);
    }
    
    /**
     * Connects to the switch and gets the user balance
     */
    private void requestBalance(String pip) {
    	String data = YodoQueries.requestBalance(this, hrdwToken, pip);
    	
    	SwitchServer request = mTaskFragment.getSwitchServerInstance();
    	request.setType(BAL_REQ);
    	request.setDialog(true, getString(R.string.balance_message));
    	
    	mTaskFragment.start(request, SwitchServer.BALANCE_REQUEST, data);
    }

    /**
     * Connects to the switch and request the receipt
     */
    private void requestReceipt(String pip) {
    	String data = YodoQueries.requestReceipt(this, hrdwToken, pip);

    	SwitchServer request = mTaskFragment.getSwitchServerInstance();
    	request.setType(REC_REQ);
    	request.setDialog(true, getString(R.string.receipt_message));
        
        mTaskFragment.start(request, SwitchServer.RECEIPT_REQUEST, data);
    }
    
    /**
	 * Connects to the switch and get merch images
	 * @return String 
	 */
	private void requestAdvertising(String merch) {
		String data = YodoQueries.requestAdvertising(this, hrdwToken, merch);
		
		SwitchServer request = mTaskFragment.getSwitchServerInstance();
        request.setType(ADS_REQ);

        mTaskFragment.start(request, SwitchServer.QUERY_ADS_REQUEST, data);
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
    	
    	if(settings.getBoolean(YodoGlobals.ID_ADVERTISING, YodoGlobals.DEFAULT_ADS))
    		processStartService(AdvertisingService.TAG);
    	
        if(data != null && data.getCode() != null) {
            String code = data.getCode();
 
            if(code.equals(YodoGlobals.AUTHORIZED)) {
            	switch(queryType) {
	                case AUTH_REQ:
	                	Long time = data.getTime();
	                	if(time != null) {
		                	String originalCode = temp_pip + SKS_SEP + hrdwToken + SKS_SEP + time;
		    	            Utils.Logger(DEBUG, TAG, originalCode);
		    	            showSKSDialog(originalCode);
	                	}
	                break;
	
	                case BAL_REQ:
	                	String aParams[] = data.getParams().split(ServerResponse.ENTRY_SEPARATOR);
                        for(String param : aParams) {
                            String aValParams[] = param.split(ServerResponse.VALUE_SEPARATOR);
                            if(aValParams[0].equals(ServerResponse.BALANCE_ELEM)) {
                                DecimalFormat twoDForm = new DecimalFormat("#.##");
                                
                                Utils.Logger(DEBUG, TAG, twoDForm.format(Double.valueOf(aValParams[1])).replace(",", "."));
       
                                double tempBalance = Double.valueOf(twoDForm.format(Double.valueOf(aValParams[1])).replace(",", "."));
                                accBalanceText.setText(String.valueOf(tempBalance));
                            }
                        }

                        temp_pip = "";
                    break;
	
	                case REC_REQ:
	                	receiptDialog(data.getParams());
                        temp_pip = "";
                    break;
                    
	                case ADS_REQ:
	                	String url = data.getParams();
	                	int end    = url.indexOf(ServerResponse.ENTRY_SEPARATOR + ServerResponse.TIME_ELEM);
	                	url        = url.substring(0, end);
	                	
	                	if(!url.equals(YodoGlobals.NO_ADS)) {
	                		new DownloadTask().execute(url.replaceAll(" ", "%20"));
	                	}
	                break;
	                
	                case BIO_REQ:
	                	/*if(!isDefine) {
	                		Intent intent = new Intent(YodoPayment.this, YodoCamera.class);
		            		intent.putExtra(YodoGlobals.ID_TOKEN, data.getParams());
		                	startActivityForResult(intent, REQUEST_FACE_ACTIVITY);
	                	} else {
	                		if(data.getParams().equals(YodoGlobals.USER_BIOMETRIC)) {
	                			
	                		} else {
	                			ToastMaster.makeText(YodoPayment.this, R.string.token_defined, Toast.LENGTH_LONG).show();
	                		}
	                		isDefine = false;
	                	}*/
	                break;
	
	                case CLS_REQ:
	                	temp_pip = "";
                        final SharedPreferences EulaPreferences = getSharedPreferences(YodoGlobals.PREFERENCES_EULA, Activity.MODE_PRIVATE);
                        EulaPreferences.edit().putBoolean(YodoGlobals.PREFERENCE_EULA_ACCEPTED, false).commit();
                        
                        if(!db.isOpen())
 	                       db = receiptsdb.getWritableDatabase();
 					
	 					db.delete(ReceiptsSQLiteHelper.TABLE_RECEIPTS, null, null);
	 				    db.close();
	 					
	 				    SharedPreferences.Editor editor = settings.edit();
	 		        	editor.putBoolean(YodoGlobals.ID_ADVERTISING, false);
	 					editor.commit();

                        // Listener to click event on the dialog in order to view the sks code
                        DialogInterface.OnClickListener okButtonClickListener = new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                                System.exit(0);
                            }
                        };

                        CreateAlertDialog.showAlertDialog(YodoPayment.this,
                                getString(R.string.farewell_message_tittle),
                                getString(R.string.farewell_message),
                                okButtonClickListener);
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
            case(REQUEST_ENABLE_BLUETOOTH):
                if(resultCode == RESULT_OK) {
                	processStartService(AdvertisingService.TAG);
                } else {
                	advertising = false;
                	SharedPreferences.Editor editor = settings.edit();
                	editor.putBoolean(YodoGlobals.ID_ADVERTISING, false);
    				editor.commit();
                }
            break;
            
            case(REQUEST_FACE_ACTIVITY):
            	if(resultCode == RESULT_OK) { 
            		requestCloseAccount(temp_pip);
            	}
            break;
        }
    }
    
    class DownloadTask extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... params) {
        	try {
				String ads = params[0];
				String extension = ".jpeg"; 
				
				if(ads.contains(".")) {
					extension = ads.substring(ads.lastIndexOf("."));
				}
				
				Utils.Logger(DEBUG, TAG, ads);
				if(Arrays.asList(YodoGlobals.IMG_EXT).contains(extension)) {
					URL url = new URL(ads);
					bmAdvertising = BitmapFactory.decodeStream(url.openConnection().getInputStream());
				}
			} catch(MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return bmAdvertising;	
        }
        
        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            
            if(bmAdvertising != null) {
				advertisingImage.setImageBitmap(bmAdvertising);
				mAttacher.update();
            }
        }
    }
    
    private class DevicesReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context arg0, Intent intent) {
			actualMerch = intent.getStringExtra(YodoGlobals.DATA_DEVICE);
			requestAdvertising(actualMerch.replaceAll(" ", "%20"));
		}
	}
}
