package co.yodo.mobile.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import co.yodo.mobile.helper.SystemUtils;

/**
 * Created by hei on 01/07/16.
 * Handles the heartbeat for the GCM
 */
public class HeartbeatReceiver extends BroadcastReceiver {
    /** DEBUG */
    private static final String TAG = HeartbeatReceiver.class.getSimpleName();

    /** Intents to wake up the GCM */
    private static final Intent GTALK_HEART_BEAT_INTENT
            = new Intent( "com.google.android.intent.action.GTALK_HEARTBEAT" );
    private static final Intent MCS_MCS_HEARTBEAT_INTENT
            = new Intent( "com.google.android.intent.action.MCS_HEARTBEAT" );

    @Override
    public void onReceive( Context context, Intent intent) {
        context.sendBroadcast( GTALK_HEART_BEAT_INTENT );
        context.sendBroadcast( MCS_MCS_HEARTBEAT_INTENT );
        SystemUtils.iLogger( TAG, "HeartbeatReceiver sent heartbeat request" );
    }
}
