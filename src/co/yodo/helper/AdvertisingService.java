package co.yodo.helper;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

public class AdvertisingService extends Service {
	/*!< DEBUG */
	private static final boolean DEBUG = false;
	public static final String TAG = "BluetoothTag";
	
	/*!< Bluetooth Timer */
	private BluetoothAdapter mBluetoothAdapter;
	private boolean registered = false;
	private TimerTask task;
	private Timer timer;
	
	/*!< Times */
	private static final int DELAY_TIME  = 0;
	private static final int PERIOD_TIME = 45000;
	
	/*!< Merch Name */
	private static final String YODO_POS = "Yodo-Merch-";

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onCreate() {
		if(DEBUG)
			Log.d(TAG, "onCreate");
	    
	    mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	    if(mBluetoothAdapter != null) {
	    	IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mReceiver, filter);
            registered = true;
            
	    	timer = new Timer();
	    	task = new TimerTask() {
	    		@Override
	            public void run() {
	    			if(mBluetoothAdapter.isDiscovering()) {
	    	            mBluetoothAdapter.cancelDiscovery();
	    	        }
	    	    	mBluetoothAdapter.startDiscovery();
	    	    	
	    	    	if(DEBUG)
	    	    		Log.d(TAG, "onDiscover");
	            }
	        };
	    }
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		if(DEBUG)
			Log.d(TAG, "onStart");    
	    
	    if(mBluetoothAdapter != null && !mBluetoothAdapter.isEnabled()) {
	        stopSelf();
	    } else {
	        timer.schedule(task, DELAY_TIME, PERIOD_TIME);
	    }
	}
	 
	@Override
	public void onDestroy() {
		if(DEBUG)
			Log.d(TAG, "onDestroy");
		
		if(mBluetoothAdapter!= null && mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
		
		if(mReceiver != null && registered)
			unregisterReceiver(mReceiver);
		
		if(timer != null)
			timer.cancel();
	}
	
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @SuppressWarnings("unused")
		public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                
                if(device != null && device.getName() != null && DEBUG)
            		Log.e("Device", device.getName());
                
                if(device != null && device.getName() != null && device.getName().startsWith(YODO_POS)) {
                	if(mBluetoothAdapter.isDiscovering())
                    	mBluetoothAdapter.cancelDiscovery();
                	
                    String[] parts = device.getName().split(YODO_POS);
                    
                    if(parts.length >= 2) {
                    	String actualMerch = parts[1];
                    	
                    	Intent it = new Intent();
                    	it.setAction(YodoGlobals.DEVICES_BT);
                    	it.putExtra(YodoGlobals.DATA_DEVICE, actualMerch);
                        sendBroadcast(it);
                    }
                }
            }
        }
    };
}
