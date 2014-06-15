package co.yodo.main;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import co.yodo.R;
import co.yodo.database.ReceiptsSQLiteHelper;
import co.yodo.helper.Utils;
import co.yodo.helper.YodoGlobals;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class YodoReceipts extends ActionBarActivity {
	/*!< milliseconds */
	private static final int TIME_TO_DISMISS_DIALOG = 60000; 	
	
	/*!< Database */
    private ReceiptsSQLiteHelper receiptsdb;
    private SQLiteDatabase db;
    private String[] allColumns = {ReceiptsSQLiteHelper.COLUMN_ID, ReceiptsSQLiteHelper.COLUMN_DESCRIPTION, ReceiptsSQLiteHelper.COLUMN_CREATED,
    		ReceiptsSQLiteHelper.COLUMN_AMOUNT, ReceiptsSQLiteHelper.COLUMN_TENDER, ReceiptsSQLiteHelper.COLUMN_CASHBACK, 
    		ReceiptsSQLiteHelper.COLUMN_AUTHNUMBER, ReceiptsSQLiteHelper.COLUMN_BALANCE};
    
    /*!< GUI Controllers */
    private ListView receipts;
    private ProgressBar progressBar;
    private SimpleAdapter adapter;
    private ArrayList<HashMap<String, String>> list_data;
    
    /*!< List View Keys */
    private static final String KEY_ID          = "id";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_CREATED     = "created";
    private static final String KEY_DATE        = "created_formated";
    private static final String KEY_AMOUNT      = "amount";
    private static final String KEY_TENDER      = "tender";
    private static final String KEY_CASHBACK    = "cashback";
    private static final String KEY_AUTHNUMBER  = "transauthnumber";
    private static final String KEY_BALANCE     = "balance"; 
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.changeLanguage(this);
        setContentView(R.layout.activity_yodo_receipts);

        setupGUI();
        updateData();
    }
    
    private void setupGUI() {
    	receiptsdb = new ReceiptsSQLiteHelper(this, YodoGlobals.DB_NAME, null, 1);
        db = receiptsdb.getWritableDatabase();
        
        receipts    = (ListView)findViewById(R.id.receiptsList);
        progressBar = (ProgressBar)findViewById(R.id.progressBarTransactions);
        receipts.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        SQLiteStatement statement = db.compileStatement("SELECT COUNT(*) FROM " + ReceiptsSQLiteHelper.TABLE_RECEIPTS);
        setTitle(getString(R.string.recipts_title) + " (" + statement.simpleQueryForLong() + ")");
        
        receipts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@SuppressWarnings("unchecked")
			@Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
                ListAdapter listAdapter = receipts.getAdapter();
                HashMap<String, String> dataHash = ((HashMap<String, String>) listAdapter.getItem(position));
               
                showReceiptDialog(dataHash);
            }
        });
    }
    
    private void updateData() {
    	list_data = new ArrayList<HashMap<String, String>>();
		DateFormat dateOriginal = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault());
		DateFormat dateFormated = new SimpleDateFormat("dd-MMM-yyyy", java.util.Locale.getDefault());
		
		Cursor cursor = db.query(ReceiptsSQLiteHelper.TABLE_RECEIPTS, allColumns, null, null, null, null, null);
        cursor.moveToFirst();
        
        while(!cursor.isAfterLast()) {
        	HashMap<String, String> datum = new HashMap<String, String>(8);

        	datum.put(KEY_ID, cursor.getString(0));
        	datum.put(KEY_DESCRIPTION, cursor.getString(1));
        	datum.put(KEY_DATE, cursor.getString(2));
            
			try {
				datum.put(KEY_CREATED, dateFormated.format(dateOriginal.parse(cursor.getString(2))));
			} catch(ParseException e) {
				e.printStackTrace();
			} 
			
            datum.put(KEY_AMOUNT, cursor.getString(3));
            datum.put(KEY_TENDER, cursor.getString(4));
            datum.put(KEY_CASHBACK, cursor.getString(5));
            datum.put(KEY_AUTHNUMBER, cursor.getString(6));
            datum.put(KEY_BALANCE, cursor.getString(7));
            list_data.add(datum);
            cursor.moveToNext();
        }
        adapter = new SimpleAdapter(YodoReceipts.this, list_data, R.layout.row, new String[]{KEY_DESCRIPTION, KEY_CREATED},
        		new int[]{R.id.descriptionView, R.id.createdView});
        receipts.setAdapter(adapter);
        registerForContextMenu(receipts);
        cursor.close();
        progressBar.setVisibility(View.GONE);
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
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
          super.onCreateContextMenu(menu, v, menuInfo);
          
          if(v.getId() == R.id.receiptsList) {
              MenuInflater inflater = getMenuInflater();
              inflater.inflate(R.menu.yodo_menu_receipt, menu);
          }
    }
    
    @SuppressWarnings("unchecked")
	@Override
    public boolean onContextItemSelected(MenuItem item) {
          AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
          switch(item.getItemId()) {
              case R.id.delete:
            	  ListAdapter listAdapter = receipts.getAdapter();
                  HashMap<String, String> dataHash = ((HashMap<String, String>) listAdapter.getItem(info.position));
                  db.delete(ReceiptsSQLiteHelper.TABLE_RECEIPTS, ReceiptsSQLiteHelper.COLUMN_ID + "=" + dataHash.get(KEY_ID), null);
            	  list_data.remove(info.position);
            	  adapter.notifyDataSetChanged();
            	  
            	  SQLiteStatement statement = db.compileStatement("SELECT COUNT(*) FROM " + ReceiptsSQLiteHelper.TABLE_RECEIPTS);
                  setTitle(getString(R.string.recipts_title) + " (" + statement.simpleQueryForLong() + ")");
            	  return true;
              default:
                    return super.onContextItemSelected(item);
          }
    }
    
    /**
	 * Method to show the dialog containing the SKS code
	 * @param qrBitmap
	 */
	private void showReceiptDialog(HashMap<String, String> dataHash) {
		/// brightness
		final WindowManager.LayoutParams lp = getWindow().getAttributes();
		final float brightnessNow = lp.screenBrightness;
		lp.screenBrightness = 100 / 100.0f;
		getWindow().setAttributes(lp);
	
		final Dialog receipt = new Dialog(YodoReceipts.this);
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
 	   	
 	   	DecimalFormat twoDForm = new DecimalFormat("#.##");

 	   	description.setText(dataHash.get(KEY_DESCRIPTION));
 	   	created.setText(dataHash.get(KEY_DATE));
 	   	paid.setText("" + Double.valueOf(twoDForm.format(Double.valueOf(dataHash.get(KEY_AMOUNT))).replace(",", ".")));
 	   	tender.setText("" + Double.valueOf(twoDForm.format(Double.valueOf(dataHash.get(KEY_TENDER))).replace(",", ".")));
 	   	cashBack.setText("" + Double.valueOf(twoDForm.format(Double.valueOf(dataHash.get(KEY_CASHBACK))).replace(",", ".")));
 	   	authNumber.setText(dataHash.get(KEY_AUTHNUMBER));
 	  	balance.setText("" + Double.valueOf(twoDForm.format(Double.valueOf(dataHash.get(KEY_BALANCE))).replace(",", ".")));
 	  	
 	  	deleteButton.setVisibility(View.GONE);
 	  	saveButton.setVisibility(View.GONE);
 	  	
 	  	receipt.setContentView(layout);
		
 	   	receipt.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
		 	   	lp.screenBrightness = brightnessNow;
		 	   	getWindow().setAttributes(lp);
			}
		});
		 
 	   	receipt.show();
		final Timer t = new Timer();
		t.schedule(new TimerTask() {
			public void run() {
				receipt.dismiss(); /// When the task active then close the dialog
				t.cancel(); /// Also just top the timer thread, otherwise, you may receive a crash report
			}
		}, TIME_TO_DISMISS_DIALOG); 
	}
}
