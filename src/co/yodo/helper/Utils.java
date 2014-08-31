package co.yodo.helper;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Locale;

import org.apache.http.HttpStatus;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.text.format.Time;
import android.util.Log;
import android.widget.Toast;
import co.yodo.R;

public class Utils {
	private static final String	PCLIENT_SEP = "/";
	
	/**
	 * Gets the imei or the mac of the mobile
	 * @param Context Activity thats calling the function
	 */
	public static String getHardwareToken(Context _context) {
		String HARDWARE_TOKEN = null;
		
		// Load Services
		TelephonyManager telephonyManager = (TelephonyManager) _context.getSystemService(Context.TELEPHONY_SERVICE);
		BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		//WifiManager wifiManager = (WifiManager) _context.getSystemService(Context.WIFI_SERVICE);
		
		if(HARDWARE_TOKEN == null && telephonyManager != null) {
			HARDWARE_TOKEN = telephonyManager.getDeviceId();
		}
		
		if(HARDWARE_TOKEN == null && mBluetoothAdapter != null) {
			if(mBluetoothAdapter.isEnabled()) {
				String tempMAC = mBluetoothAdapter.getAddress();
				HARDWARE_TOKEN = tempMAC.replaceAll(":", "");
			} 
		} 
		
		/*if(HARDWARE_TOKEN == null && wifiManager != null) {
			if(wifiManager.isWifiEnabled()) {
				WifiInfo wifiInf = wifiManager.getConnectionInfo();
				String tempMAC = wifiInf.getMacAddress();
				HARDWARE_TOKEN = tempMAC.replaceAll(":", "");
			}
		}*/
		
		if(HARDWARE_TOKEN == null)
			ToastMaster.makeText(_context, R.string.no_hdw, Toast.LENGTH_LONG).show();
		
		return HARDWARE_TOKEN;
	}
	
	/**
	 * Change the language of the application
	 * 	 * @param Context Activity thats calling the function
	 */
	public static void changeLanguage(Context _context) {
		SharedPreferences settings = _context.getSharedPreferences(YodoGlobals.PREFERENCES, Context.MODE_PRIVATE);
		String code = _context.getResources().getConfiguration().locale.getLanguage();
		int languagePosition = settings.getInt(YodoGlobals.ID_LANGUAGE, YodoGlobals.DEFAULT_LANGUAGE);
		Locale appLoc = null;
		
		if(languagePosition == -1 && (Arrays.asList(YodoGlobals.lang_code).contains(code))) {
			appLoc = _context.getResources().getConfiguration().locale;		
		}
		else if(YodoGlobals.languages[languagePosition].equals("Spanish")) {
			appLoc = new Locale("es");
	    } 
		else if(YodoGlobals.languages[languagePosition].equals("Chinese")) {
	    	appLoc = new Locale("zh");
	    } 
		else if(YodoGlobals.languages[languagePosition].equals("Japanese")) {
	    	appLoc = new Locale("ja");
	    }
		else if(YodoGlobals.languages[languagePosition].equals("French")) {
	    	appLoc = new Locale("fr");
	    }
	    else {
	    	appLoc = new Locale("en");
	    }
		
		Resources standardResources = _context.getResources();
		Locale.setDefault(appLoc);
		Configuration appConfig = new Configuration(standardResources.getConfiguration());
		appConfig.locale = appLoc;
		standardResources.updateConfiguration(appConfig, standardResources.getDisplayMetrics());
	}
	
	public static boolean isConnectingToInternet(Context _context) {
        ConnectivityManager connectivity = (ConnectivityManager) _context.getSystemService(Context.CONNECTIVITY_SERVICE);
          if(connectivity != null) {
              NetworkInfo[] info = connectivity.getAllNetworkInfo();
              if(info != null)
                  for(NetworkInfo anInfo : info) {
                      if(anInfo.isConnected() && anInfo.isAvailable()) {
                          return true;
                      }
                  }
          }
          return false;
    }
    
    public static boolean isOnline(String ip) {
        boolean responded = false;
        try {
            URL url = new URL(ip);
            HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
            urlc.setRequestProperty("User-Agent", "Test");
            urlc.setRequestProperty("Connection", "close");
            urlc.setConnectTimeout(2500);
            urlc.connect();

            if(urlc.getResponseCode() == HttpStatus.SC_OK)
                responded = true;
        } catch(Exception e) {}
        
        return responded;
    }
    
    /**
     * Formats user's data into a well formed string in order to encrypt it
     * @param pUsrAccount IMEI number
     * @param pUsrPip User's pip
     * @return String formated Data
     */
    public static String formatUsrData(String pUsrAccount, String pUsrPip){
        Time now = new Time();
        now.setToNow();

        return pUsrAccount + PCLIENT_SEP + pUsrPip + PCLIENT_SEP + now.toMillis(true) / 1000L;
    }
    
    /**
     * ProgressDialog progressDialog; I have declared earlier.
     */
    public static void showProgressDialog(ProgressDialog progDialog, String message) {
    	progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    	progDialog.setMessage(message);
    	progDialog.setCancelable(false);
    	progDialog.show();
    }
    
    public static String bytesToHex(byte[] data) {
		 if(data == null)
			 return null;
		 
		 int len = data.length;
        String str = "";
        for(int i = 0; i < len; i++) {
       	 if((data[i]&0xFF) < 16)
       		 str = str + "0" + java.lang.Integer.toHexString(data[i]&0xFF);
            else
                str = str + java.lang.Integer.toHexString(data[i]&0xFF);
        }
        return str;
    }
	 
	 public static byte[] hexToBytes(String str) {
		 if(str == null) {
			 return null;
        } else if(str.length() < 2) {
            return null;
        } else {
       	 int len = str.length() / 2;
            byte[] buffer = new byte[len];
            for(int i = 0; i < len; i++) {
           	 buffer[i] = (byte) Integer.parseInt(str.substring(i*2,i*2+2),16);
            }
            return buffer;
        }
	 }
	 
	 public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
	    // Raw height and width of image
	    final int height = options.outHeight;
	    final int width = options.outWidth;
	    int inSampleSize = 1;

	    if (height > reqHeight || width > reqWidth) {

	        final int halfHeight = height / 2;
	        final int halfWidth = width / 2;

	        // Calculate the largest inSampleSize value that is a power of 2 and keeps both
	        // height and width larger than the requested height and width.
	        while ((halfHeight / inSampleSize) > reqHeight
	                && (halfWidth / inSampleSize) > reqWidth) {
	            inSampleSize *= 2;
	        }
	    }

	    return inSampleSize;
	}
	 
	/**
	 * A helper class just o obtain the config file for the Shared Preferences
	 * using the default values for this Shared Preferences app.
	 * 
	 * @param c The Context of the Android system.
	 * @return Returns the shared preferences with the defautl values.
	 */
	private static SharedPreferences getSPrefConfig(Context c) {
		return c.getSharedPreferences(YodoGlobals.SHARED_PREF_FILE, Context.MODE_PRIVATE);
	}
	 
	/**
	 * It gets the linked accounts
	 * 
	 * @param c The Context of the Android system.
	 * @return String The linked account numbers
	 *         null    If there is no value set;
	 */
	public static String getLinkedAccount(Context c) {
		SharedPreferences config = getSPrefConfig(c);
		String n = config.getString(YodoGlobals.SPREF_LINKED_ACC, "");
		
		return n;
	}
	
	/**
	 * It saves the linked accounts to the Shared Preferences.
	 * 
	 * @param c The Context of the Android system.
	 * @param n The new account
	 * @return true  The account was saved successfully.
	 *         false The account was not saved successfully.
	 */
	public static Boolean saveLinkedAccount(Context c, String n) {
		SharedPreferences config = getSPrefConfig(c);
		SharedPreferences.Editor writer = config.edit();
		
		String accounts = getLinkedAccount(c);
		accounts += n + YodoGlobals.ACC_SEP;
		
		writer.putString(YodoGlobals.SPREF_LINKED_ACC, accounts);
		
		return writer.commit();
	}
	
	public static void Logger(boolean DEBUG, String TAG, String data) {
		if(DEBUG)
			Log.e(TAG, data);
	}
}
