package co.yodo.main;

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
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.InputType;
import android.util.Log;
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
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import co.yodo.R;
import co.yodo.database.CouponsDataSource;
import co.yodo.database.ReceiptsSQLiteHelper;
import co.yodo.helper.AdvertisingService;
import co.yodo.helper.CreateAlertDialog;
import co.yodo.helper.FormatHelper;
import co.yodo.helper.HardwareToken;
import co.yodo.helper.Language;
import co.yodo.helper.ToastMaster;
import co.yodo.helper.YodoBase;
import co.yodo.helper.YodoGlobals;
import co.yodo.photoview.PhotoViewAttacher;
import co.yodo.serverconnection.ServerResponse;
import co.yodo.serverconnection.SwitchServer;
import co.yodo.sks.Encrypter;
import co.yodo.sks.SKSCreater;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;

/**
 * Created by luis on 22/07/13.
 */
public class YodoPayment extends Activity implements YodoBase {
	/*!< DEBUG */
	private final static boolean DEBUG = false;
	
    /*!< Database */
    private ReceiptsSQLiteHelper receiptsdb;
    private SQLiteDatabase db;
    private CouponsDataSource couponsdb;
    
    /*!< Async Tasks for Receipt */
    private SwitchServer receiptRequest;
    private SwitchServer balanceRequest;
    private boolean receiptFlag = false;
    private String receiptData;
    private String balanceData;

    /*!< Bluetooth */
    private BluetoothAdapter mBluetoothAdapter;
    private boolean advertising;
    private String actualMerch = "";

    /*!< Variable used as an authentication number */
    private static String HARDWARE_TOKEN;
    private String temp_pip = "";

    /*!< Object used to encrypt user's information */
    private Encrypter oEncrypter;

    /*!< GUI Controllers */
    private TextView accountNumberText;
    private TextView dateText;
    private TextView balanceText;
    private EditText inputBox;
    private ScrollView navigationView;
	private RelativeLayout adsView;
	private ImageView advertisingImage;
	private Bitmap bmAdvertising;
	private PhotoViewAttacher mAttacher;

    /*!< User's data separator */
    private static final String	USR_SEP = "**";
    private static final String SKS_SEP = "**";
    private static final String	REQ_SEP = ",";

    /*!< ID for queries */
    private final static int AUTH_REQ = 0;
    private final static int BAL_REQ  = 1;
    private final static int REC_REQ  = 2;
    private final static int CLS_REQ  = 3;
    private final static int ADS_REQ  = 4;
    private final static int BIO_REQ  = 5;
    
    /*!< Different Biometric */
    private boolean isDefine = false;

    /*!< SKS time to dismiss milliseconds */
    private static final int TIME_TO_DISMISS_SKS = 60000;

    /*!< Activity Result */
    private static final int REQUEST_ENABLE_BLUETOOTH = 0;
    private static final int REQUEST_FACE_ACTIVITY    = 1;    
    
    /*!< Alert Messages */
	private AlertDialog alertDialog;
	
	/*!< Navigation Bar */
	private boolean stateLayout = false;
	
	/*!< Preferences */
	private SharedPreferences settings;
	
	/*!< Receiver */
	private DevicesReceiver myReceiver;
	private boolean started = false;
    
    /*!< Message Handler */
    private static MainHandler handlerMessages;

    /**
     * Handles the message if there isn't internet connection
     */
    private static class MainHandler extends Handler {
        private final WeakReference<YodoPayment> wMain;

        public MainHandler(YodoPayment main) {
            super();
            this.wMain = new WeakReference<YodoPayment>(main);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            YodoPayment main = wMain.get();

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
        requestWindowFeature(Window.FEATURE_NO_TITLE);
    	setContentView(R.layout.activity_yodo_main);
    	
    	setupGUI();
        updateData();
    }
    
    @Override
    protected void onResume() {
    	if(DEBUG)
    		Log.e("Yodo", "onResume");
    	
    	super.onResume();
    	
    	IntentFilter intentFilter = new IntentFilter();
	    intentFilter.addAction(YodoGlobals.DEVICES_BT);
	    registerReceiver(myReceiver, intentFilter);
	 
        if(advertising && mBluetoothAdapter != null) {
    		setupBluetooth(); 
    	}
    }

    @Override
    protected void onPause() {
    	if(DEBUG)
    		Log.e("Yodo", "onPause");
    	
        super.onPause();
        
        if(myReceiver != null)
        	unregisterReceiver(myReceiver);
        
        if(mBluetoothAdapter != null) {
        	processStopService(AdvertisingService.TAG);
        }
        
        if(receiptsdb != null)
        	receiptsdb.close();
        
        System.gc();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        System.gc();
    }
    
    @Override
    public void onBackPressed() {
    	if(stateLayout) {
    		navigationClick(null);
    	}
    	else {
    		super.onBackPressed();
    	}
    }
    
    private void setupGUI() {
    	Language.changeLanguage(this);
        handlerMessages = new MainHandler(this);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        
        myReceiver = new DevicesReceiver();

        receiptsdb = new ReceiptsSQLiteHelper(this, YodoGlobals.DB_NAME, null, 1);
        db = receiptsdb.getWritableDatabase();
        
        couponsdb = new CouponsDataSource(this);
        couponsdb.open();

        accountNumberText = (TextView)this.findViewById(R.id.accountNumberText);
        dateText 		  = (TextView)this.findViewById(R.id.dateText);
        balanceText 	  = (TextView)this.findViewById(R.id.balanceText);
        adsView           = (RelativeLayout) findViewById(R.id.ads_view);  
		navigationView    = (ScrollView) findViewById(R.id.navigation_view);
		advertisingImage  = (ImageView) findViewById(R.id.advertisingImage);
		
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
			                    
			                    success = false;
			                    // Encode the file as a PNG image.
			                    FileOutputStream outStream;
			                    try {
			                        outStream = new FileOutputStream(image);
			                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream); 
	
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
        HardwareToken token = new HardwareToken(getApplicationContext());
        HARDWARE_TOKEN = token.getToken();

        settings = getSharedPreferences(YodoGlobals.PREFERENCES, Context.MODE_PRIVATE);
        advertising  = settings.getBoolean(YodoGlobals.ID_ADVERTISING, YodoGlobals.DEFAULT_ADS);
        
        boolean use = settings.getBoolean(YodoGlobals.ID_FIRST_USE, YodoGlobals.DEFAULT_USE);
    	if(use) {
    		navigationClick(null);
    		
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

        accountNumberText.setText(HARDWARE_TOKEN);
        dateText.setText(getDate());
    }
    
    /** 
	 * Menu 
	 */
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.yodo_main, menu);
        return super.onCreateOptionsMenu(menu);
    }
	
	/**
	 * Ejecute the function of the menu item selected
	 * @return	boolean
	 */
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {    
        	case R.id.menu_exit:
    			finish();
        		return true;
        		
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
        		
        		ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, YodoGlobals.languages);
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
        		
        		final AlertDialog alertDialog = builder.create();
                alertDialog.show();
   
        		return true;
        }
        return super.onOptionsItemSelected(item);
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
    private void showSKSDialog(Bitmap qrBitmap) {
        // brightness
        final WindowManager.LayoutParams lp = getWindow().getAttributes();
        final float brightnessNow = lp.screenBrightness;
        lp.screenBrightness = 100 / 100.0f;
        getWindow().setAttributes(lp);

        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_sks);
        dialog.setCancelable(false);
        
        dialog.setOnKeyListener(new Dialog.OnKeyListener() {
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				if(keyCode == KeyEvent.KEYCODE_BACK) {
                    dialog.dismiss();
                }
				return true;
			}
        });
        
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                lp.screenBrightness = brightnessNow;
                getWindow().setAttributes(lp);
                receiptFlag = true;
                requestReceipt(temp_pip);
                requestBalance(temp_pip);
            }
        });

        ImageView image = (ImageView) dialog.findViewById(R.id.sks);
        image.setImageBitmap(qrBitmap);
        dialog.show();

        final Timer t = new Timer();
        t.schedule(new TimerTask() {
            public void run() {
                dialog.dismiss(); /// When the task active then close the dialog
                t.cancel(); /// Also just top the timer thread, otherwise, you may receive a crash report
            }
        }, TIME_TO_DISMISS_SKS);
    }
    
    private void receiptDialog() {
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
        TextView balance = (TextView)layout.findViewById(R.id.yodoBalanceText);
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
        }
        query += balanceData + ")";
        balance.setText(balanceData);
        final String finalQuery = query;
        
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
    
    /*!< Button Actions */
    public void navigationClick(View v) {
    	if(!stateLayout) {
			adsView.setVisibility(View.GONE);
			navigationView.setVisibility(View.VISIBLE);
		}
		else {
			adsView.setVisibility(View.VISIBLE);
			navigationView.setVisibility(View.GONE);
		}
    	stateLayout = !stateLayout;
    }
    
    public void couponClick(View v) {
    	//ToastMaster.makeText(YodoPayment.this, R.string.not_available, Toast.LENGTH_SHORT).show();
    	Intent intent = new Intent(YodoPayment.this, YodoCoupons.class);
        startActivity(intent);
    }
    
    public void networkClick(View v) {
    	ToastMaster.makeText(YodoPayment.this, R.string.not_available, Toast.LENGTH_SHORT).show();
    }

    /**
     * Navigation Button Actions
     */
    public void resetPipClick(View v) {
        Intent intent = new Intent(YodoPayment.this, YodoResetPip.class);
        startActivity(intent);
    }
    
    public void setBiometricClick(View v) {
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
                	isDefine = true;
                	requestBiometricToken(temp_pip);
                }
            }
        };

        CreateAlertDialog.showAlertDialog(YodoPayment.this, layout, inputBox,
                getString(R.string.input_pip),
                null,
                pipDialogOkButtonClickListener,
                null);
    }

    public void savedReceiptsClick(View v) {
        Intent intent = new Intent(YodoPayment.this, YodoReceipts.class);
        startActivity(intent);
    }
    
    public void settingsClick(View v) {
    	int languagePosition = settings.getInt(YodoGlobals.ID_LANGUAGE, YodoGlobals.DEFAULT_LANGUAGE);
		String code = getResources().getConfiguration().locale.getLanguage();
		
		AlertDialog.Builder builder = new AlertDialog.Builder(YodoPayment.this);
		builder.setInverseBackgroundForced(true);
		View view = getLayoutInflater().inflate(R.layout.dialog_settings, null);
		
		if(languagePosition == -1 && (Arrays.asList(YodoGlobals.lang_code).contains(code))) {
			languagePosition = Arrays.asList(YodoGlobals.lang_code).indexOf(code);
		} else if(languagePosition == -1)
			languagePosition = 0;
		
		final Spinner languagesSpinner = (Spinner) view.findViewById(R.id.languagesSpinner);
		final CheckBox adsCheckBox     = (CheckBox) view.findViewById(R.id.advertisingCheckBox);
		
		ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, YodoGlobals.languages);
		languagesSpinner.setAdapter(adapter);
		languagesSpinner.setSelection(languagePosition);
		
		adsCheckBox.setChecked(advertising);
		if(mBluetoothAdapter == null) {
			adsCheckBox.setEnabled(false);
		}
		
		builder.setTitle(getString(R.string.action_settings));
		builder.setView(view);
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
		
		final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    
    public void linksClick(View v) {
    	ToastMaster.makeText(YodoPayment.this, R.string.not_available, Toast.LENGTH_SHORT).show();
    }
    
    public void pairClick(View v) {
    	ToastMaster.makeText(YodoPayment.this, R.string.not_available, Toast.LENGTH_SHORT).show();
    }

    public void balanceClick(View v) {
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

        CreateAlertDialog.showAlertDialog(YodoPayment.this, layout, inputBox,
                getString(R.string.input_pip),
                null,
                pipDialogOkButtonClickListener,
                null);
    }

    public void closeAccClick(View v) {
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
                	//requestBiometricToken(temp_pip);
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

    /**
     * Main Layout Button Actions
     * */
    public void paymentClick(View v) {
    	advertisingImage.setImageDrawable(null);
    	
    	if(stateLayout) {
    		navigationClick(null);
    	}
    	
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
                    requestPIPHardwareAuthentication(temp_pip);
                }
            }
        };

        CreateAlertDialog.showAlertDialog(YodoPayment.this, layout, inputBox,
                getString(R.string.input_pip),
                null,
                pipDialogOkButtonClickListener,
                null);
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
    private void requestPIPHardwareAuthentication(String pip) {
        String sEncryptedUsrData, sFormattedUsrData;
        sFormattedUsrData = FormatHelper.formatUsrData(HARDWARE_TOKEN, pip);

        // Encrypting user's data to create request
        getEncrypter().setsUnEncryptedString(sFormattedUsrData);
        getEncrypter().rsaEncrypt(this);
        sEncryptedUsrData = getEncrypter().bytesToHex();

        SwitchServer request = new SwitchServer(YodoPayment.this);
        request.setType(AUTH_REQ);
        request.setDialog(true, getString(R.string.auth_message));
        request.execute(SwitchServer.AUTH_HW_PIP_REQUEST, sEncryptedUsrData);
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
		
		SwitchServer request = new SwitchServer(YodoPayment.this);
        request.setType(BIO_REQ);
        request.setDialog(true, getString(R.string.biometric_message));
        request.execute(SwitchServer.BIOMETRIC_REQUEST, sEncryptedUsrData);
	}

    /**
     * Connects to the switch and close the account
     */
    private void requestCloseAccount(String pip) {
        String sEncryptedUsrData;
        StringBuilder sUsrData = new StringBuilder();

        long time = System.currentTimeMillis();
        String timeStamp = String.valueOf(time);

        sUsrData.append(pip).append(USR_SEP);
        sUsrData.append(HARDWARE_TOKEN).append(USR_SEP);
        sUsrData.append(timeStamp).append(REQ_SEP);
        sUsrData.append("0").append(REQ_SEP);
        sUsrData.append("0");

        // Encrypting user's data to create request
        getEncrypter().setsUnEncryptedString(sUsrData.toString());
        getEncrypter().rsaEncrypt(this);
        sEncryptedUsrData = getEncrypter().bytesToHex();

        SwitchServer request = new SwitchServer(YodoPayment.this);
        request.setType(CLS_REQ);
        request.setDialog(true, getString(R.string.closing_message));
        request.execute(SwitchServer.CLOSE_REQUEST, sEncryptedUsrData);
    }
    
    /**
     * Connects to the switch and gets the user balance
     */
    private void requestBalance(String pip) {
        String sEncryptedUsrData, sFormattedUsrData ;
        sFormattedUsrData = FormatHelper.formatUsrData(HARDWARE_TOKEN, pip);

        // Encrypting user's data to create request
        getEncrypter().setsUnEncryptedString(sFormattedUsrData);
        getEncrypter().rsaEncrypt(this);
        sEncryptedUsrData = this.getEncrypter().bytesToHex();

        balanceRequest = new SwitchServer(YodoPayment.this);
        balanceRequest.setType(BAL_REQ);
        if(!receiptFlag)
        	balanceRequest.setDialog(true, getString(R.string.balance_message));
        balanceRequest.execute(SwitchServer.BALANCE_REQUEST, sEncryptedUsrData);
    }

    /**
     * Connects to the switch and request the receipt
     */
    private void requestReceipt(String pip) {
        StringBuilder userData = new StringBuilder();
        String sEncryptedUsrData;

        userData.append(HARDWARE_TOKEN).append(REQ_SEP);
        userData.append(pip).append(REQ_SEP);
        userData.append(YodoGlobals.RECORD_LOCATOR);

        /// Encrypting user's data to create request
        getEncrypter().setsUnEncryptedString(userData.toString());
        getEncrypter().rsaEncrypt(this);
        sEncryptedUsrData = this.getEncrypter().bytesToHex();

        receiptRequest = new SwitchServer(YodoPayment.this);
        receiptRequest.setType(REC_REQ);
        receiptRequest.setDialog(true, getString(R.string.receipt_message));
        receiptRequest.execute(SwitchServer.RECEIPT_REQUEST, sEncryptedUsrData);
    }
    
    /**
	 * Connects to the switch and get merch images
	 * @return String 
	 */
	private void requestAdvertisingRequest(String MERCHANT) {
		String sEncryptedUsrData;
		StringBuilder sAdvertisingData = new StringBuilder();
		
		sAdvertisingData.append(HARDWARE_TOKEN).append(REQ_SEP);
		sAdvertisingData.append(MERCHANT).append(REQ_SEP);
		sAdvertisingData.append(YodoGlobals.QUERY_ADS);
		
		if(DEBUG)
			Log.e("Query", sAdvertisingData.toString());
		
		// Encrypting user's data to create request
		this.getEncrypter().setsUnEncryptedString(sAdvertisingData.toString());
		this.getEncrypter().rsaEncrypt(this);
		sEncryptedUsrData = this.getEncrypter().bytesToHex();
		
		SwitchServer request = new SwitchServer(YodoPayment.this);
        request.setType(ADS_REQ);
        request.execute(SwitchServer.QUERY_ADS_REQUEST, sEncryptedUsrData);
	}

    @Override
    public void setData(ServerResponse data, int queryType) {
    	AlertDialog.Builder builder = new AlertDialog.Builder(YodoPayment.this);
    	
    	if(settings.getBoolean(YodoGlobals.ID_ADVERTISING, YodoGlobals.DEFAULT_ADS))
    		processStartService(AdvertisingService.TAG);
    	
        if(data != null && data.getCode() != null) {
            String code = data.getCode();
 
            if(code.equals(YodoGlobals.AUTHORIZED)) {
            	switch (queryType) {
	                case AUTH_REQ:
                        new TimeTask().execute(temp_pip);
	                break;
	
	                case BAL_REQ:
	                	String aParams[] = data.getParams().split(ServerResponse.ENTRY_SEPARATOR);
                        for(String param : aParams) {
                            String aValParams[] = param.split(ServerResponse.VALUE_SEPARATOR);
                            if(aValParams[0].equals(ServerResponse.BALANCE_ELEM)) {
                                DecimalFormat twoDForm = new DecimalFormat("#.##");
                                if(DEBUG)
                                	Log.e("valor", twoDForm.format(Double.valueOf(aValParams[1])).replace(",", "."));
                                double tempBalance = Double.valueOf(twoDForm.format(Double.valueOf(aValParams[1])).replace(",", "."));

                                if(!receiptFlag) 
                                	balanceText.setText(String.valueOf(tempBalance));
                                else  
                                	balanceData = String.valueOf(tempBalance);
                            }
                        }
                        
                        if(receiptRequest != null && receiptData != null && receiptRequest.getStatus() == AsyncTask.Status.FINISHED) {
                        	receiptDialog();
                        	balanceRequest = receiptRequest = null;
                    	}
                        receiptFlag = false;
                        temp_pip = "";
                    break;
	
	                case REC_REQ:
	                	receiptData = data.getParams();
	                	
	                	if(balanceRequest != null && balanceData!= null && balanceRequest.getStatus() == AsyncTask.Status.FINISHED) {
                    		receiptDialog();
                    		receiptFlag = false;
                    		receiptRequest = balanceRequest = null;
                   	 	} 
                        temp_pip = "";
                    break;
                    
	                case ADS_REQ:
	                	String url = data.getParams();
	                	
	                	if(!url.equals(YodoGlobals.NO_ADS)) {
	                		//ToastMaster.makeText(YodoPayment.this, getString(R.string.merch_found) + " " + actualMerch, Toast.LENGTH_LONG).show();
	                		new DownloadTask().execute(data.getParams().replaceAll(" ", "%20"));
	                	}
	                break;
	                
	                case BIO_REQ:
	                	if(!isDefine) {
	                		Intent intent = new Intent(YodoPayment.this, YodoCamera.class);
		            		intent.putExtra(YodoGlobals.ID_TOKEN, data.getParams());
		                	startActivityForResult(intent, REQUEST_FACE_ACTIVITY);
	                	} else {
	                		if(data.getParams().equals(YodoGlobals.USER_BIOMETRIC)) {
	                			
	                		} else {
	                			ToastMaster.makeText(YodoPayment.this, R.string.token_defined, Toast.LENGTH_LONG).show();
	                		}
	                		isDefine = false;
	                	}
	                	
	                break;
	
	                case CLS_REQ:
	                	temp_pip = "";
                        final SharedPreferences EulaPreferences = getSharedPreferences(YodoGlobals.PREFERENCES_EULA, Activity.MODE_PRIVATE);
                        EulaPreferences.edit().putBoolean(YodoGlobals.PREFERENCE_EULA_ACCEPTED, false).commit();
                        
                        if(!db.isOpen())
 	                       db = receiptsdb.getWritableDatabase();
 					
	 					db.delete(ReceiptsSQLiteHelper.TABLE_RECEIPTS, null, null);
	 				    db.close();

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
            	if(resultCode == Activity.RESULT_OK) { 
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
				
				if(Arrays.asList(YodoGlobals.IMG_EXT).contains(extension)) {
					URL url = new URL(ads);
					bmAdvertising = BitmapFactory.decodeStream(url.openConnection().getInputStream());
				}
			} catch (MalformedURLException e) {
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

    class TimeTask extends AsyncTask<String, Void, Long> {
    	private String pip;
    	private ProgressDialog progDialog;
    	
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progDialog = new ProgressDialog(YodoPayment.this);
            progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progDialog.setMessage(getString(R.string.sks_message));
            progDialog.setCancelable(false);
            progDialog.show();
        }
        
        @Override
        protected Long doInBackground(String... params) {
        	pip = params[0];
            NTPUDPClient timeClient = new NTPUDPClient();
            timeClient.setDefaultTimeout(YodoGlobals.TIMEOUT_SERVER);
            TimeInfo timeInfo;
            
            List<String> timeList = Arrays.asList(YodoGlobals.TIME_SERVERS); 
            for(String server : timeList) {  
            	if(DEBUG)
            		Log.e("server", server);
            	
            	try {
            		InetAddress inetAddress = InetAddress.getByName(server);
                    timeInfo = timeClient.getTime(inetAddress);
                    Long returnTime = timeInfo.getMessage().getTransmitTimeStamp().getTime();   //server time
                     
                    if(returnTime != null) {
                    	if(DEBUG)
                    		Log.e("time", String.valueOf(returnTime));

                    	return returnTime;
                    }
                } catch (IOException e) {
                	e.printStackTrace();
                }
            } 
            return null;
        }
        
        @Override
        protected void onPostExecute(Long result) {
            super.onPostExecute(result);
            
            if(progDialog != null)
            	progDialog.dismiss();
            
            if(result != null) {
	            String originalCode = pip + SKS_SEP + HARDWARE_TOKEN + SKS_SEP + result / 1000L;
	            
	            if(DEBUG)
	            	Log.e("Code", originalCode);
	        	
	        	try {
	                showSKSDialog(SKSCreater.createSKS(originalCode, YodoPayment.this, SKSCreater.SKS_CODE));
	            } catch (UnsupportedEncodingException e) {
	                e.printStackTrace();
	            }
            } else {
            	handlerMessages.sendEmptyMessage(YodoGlobals.GENERAL_ERROR);
            }
        }
    }
    
    private class DevicesReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context arg0, Intent intent) {
			actualMerch = intent.getStringExtra(YodoGlobals.DATA_DEVICE);
			requestAdvertisingRequest(actualMerch.replaceAll(" ", "%20"));
		}
	}
}
