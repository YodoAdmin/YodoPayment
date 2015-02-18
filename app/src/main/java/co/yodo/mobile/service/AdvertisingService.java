package co.yodo.mobile.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import co.yodo.mobile.broadcastreceiver.BroadcastMessage;
import co.yodo.mobile.helper.AppConfig;
import co.yodo.mobile.helper.AppUtils;

public class AdvertisingService extends Service {
    /** DEBUG */
    public static final String TAG = AdvertisingService.class.getSimpleName();

    /** The context object */
    private Context ac;

    /** The Local Broadcast Manager */
    private LocalBroadcastManager lbm;

    /** Bluetooth Generals */
    private BluetoothAdapter mBluetoothAdapter = null;

    /** Init Delay */
    private final static Integer DEFAUL_INIT_DELAY = 1000 * 30; // 25 seconds

    /** Handlers */
    private Handler mTimerHandler;

    /**
     * It gets called when the service is started.
     *
     * @param i The intent received.
     * @param flags Additional data about this start request.
     * @param startId A unique integer representing this specific request to start.
     * @return Using START_STICKY the service will run again if got killed by
     * the service.
     */
    @Override
    public int onStartCommand(Intent i, int flags, int startId) {
        // get the context
        ac = AdvertisingService.this;
        // get local broadcast
        lbm = LocalBroadcastManager.getInstance( ac );
        // register broadcast
        registerBroadcasts();
        // Configurations
        bootstrap();
        // if the service is killed by Android, service starts again
        return START_STICKY;
    }

    /**
     * When the service get destroyed by Android or manually.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        // unregister broadcast
        unregisterBroadcasts();
        mTimerHandler.removeCallbacks( mDoScan );
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * The bootstrap for the advertising service
     */
    private void bootstrap() {
        // Adapter available
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if( mBluetoothAdapter == null ) {
            stopSelf();
            return;
        }

        if( !mBluetoothAdapter.isEnabled() )
            mBluetoothAdapter.enable();

        // Start the timer
        mTimerHandler = new Handler();
        mTimerHandler.postDelayed( mDoScan, DEFAUL_INIT_DELAY );
    }

    // Runnable that takes care of start the scans
    private Runnable mDoScan = new Runnable() {
        @Override
        public void run() {
            if( mBluetoothAdapter.isDiscovering() )
                mBluetoothAdapter.cancelDiscovery();

            mBluetoothAdapter.startDiscovery();
            mTimerHandler.postDelayed( mDoScan, AppConfig.DEFAULT_SCAN_INTERVAL );
        }
    };

    /**
     * Register/Unregister the Broadcast Receivers.
     */
    private void registerBroadcasts() {
        IntentFilter filter = new IntentFilter();
        filter.addAction( BluetoothDevice.ACTION_FOUND );
        filter.addAction( BluetoothAdapter.ACTION_STATE_CHANGED );
        registerReceiver( mReceiver, filter );
    }

    private void unregisterBroadcasts() {
        unregisterReceiver( mReceiver );
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {

            switch( intent.getAction() ) {
                case BluetoothDevice.ACTION_FOUND:
                    final BluetoothDevice device = intent.getParcelableExtra( BluetoothDevice.EXTRA_DEVICE );

                    if( device.getName() != null && device.getName().startsWith( AppConfig.YODO_POS ) ) {
                        AppUtils.Logger( TAG, device.getName() );

                        if( mBluetoothAdapter.isDiscovering() )
                            mBluetoothAdapter.cancelDiscovery();

                        String[] parts = device.getName().split( AppConfig.YODO_POS );

                        if( parts.length >= 2 ) {
                            Intent it = new Intent( BroadcastMessage.ACTION_NEW_MERCHANT );
                            it.putExtra( BroadcastMessage.EXTRA_NEW_MERCHANT, parts[1] );
                            lbm.sendBroadcast( it );
                        }
                    }

                    break;

                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    final int state = intent.getIntExtra( BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR );

                    switch( state ) {
                        case BluetoothAdapter.STATE_TURNING_OFF:
                            AppUtils.saveAdvertising( ac, false );
                            stopSelf();
                            break;
                    }

                    break;
            }

        }
    };
}
