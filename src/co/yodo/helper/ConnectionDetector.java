package co.yodo.helper;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.HttpStatus;

public class ConnectionDetector {
	/*!< DEBUG */
	private final static boolean DEBUG = false;
	
	private Context _context;
	 
    public ConnectionDetector(Context context) {
        this._context = context;
    }
 
    public boolean isConnectingToInternet(){
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
    
    public boolean isOnline(String ip) {
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
        
        if(DEBUG)
        	Log.e("internet", String.valueOf(responded));
        
        return responded;
    }
}
