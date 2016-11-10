package co.yodo.mobile.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.google.android.gms.gcm.GcmListenerService;

import org.greenrobot.eventbus.EventBus;

import co.yodo.mobile.R;
import co.yodo.mobile.database.model.Receipt;
import co.yodo.mobile.helper.FormatUtils;
import co.yodo.mobile.helper.SystemUtils;
import co.yodo.mobile.network.model.ServerResponse;
import co.yodo.mobile.database.ReceiptsDataSource;
import co.yodo.mobile.helper.PrefUtils;
import co.yodo.mobile.ui.MainActivity;
import co.yodo.mobile.ui.ReceiptsActivity;
import co.yodo.mobile.network.handler.JSONHandler;

public class YodoGCMListenerService extends GcmListenerService {
    @SuppressWarnings( "unused" )
    private static final String TAG = YodoGCMListenerService.class.getSimpleName();

    /** The context object */
    private Context ac;

    /** Notification for arriving messages */
    NotificationManager mNotificationManager;
    NotificationCompat.Builder mBuilder;

    @Override
    public void onCreate() {
        super.onCreate();
        // get the context
        ac = YodoGCMListenerService.this;
    }

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    @Override
    public void onMessageReceived( String from, Bundle data ) {
        String message = data.getString( "message" );
        SystemUtils.iLogger( TAG, "From: " + from );
        SystemUtils.iLogger( TAG, "Message: " + message );

        /**
         * Production applications would usually process the message here.
         * Eg: - Syncing with server.
         *     - Store message in local database.
         *     - Update UI.
         */
        Receipt receipt = JSONHandler.parseReceipt( message );

        if( !PrefUtils.isForeground( ac ) )
            sendNotification( receipt );
        else
            EventBus.getDefault().post( receipt );
    }

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param receipt GCM message received as a ServerResponse
     */
    private synchronized void sendNotification( Receipt receipt ) {
        try {
            // Database
            ReceiptsDataSource receiptsdb = ReceiptsDataSource.getInstance( ac );
            final boolean isOpen = receiptsdb.isOpen();
            if( !isOpen )
                receiptsdb.open();

            receiptsdb.addReceipt( receipt );

            // Updates the current balance
            PrefUtils.saveBalance( ac, String.format( "%s %s",
                    FormatUtils.truncateDecimal( receipt.getBalanceAmount() ),
                    receipt.getCurrency()
            ) );

            if( !isOpen )
                receiptsdb.close();
            EventBus.getDefault().post( receipt );
        } catch( NullPointerException e ) {
            e.printStackTrace();
        }

        Intent intent = new Intent( this, ReceiptsActivity.class );

        // Use TaskStackBuilder to build the back stack and get the PendingIntent
        PendingIntent pendingIntent = TaskStackBuilder.create( this )
                        .addNextIntentWithParentStack( intent )
                        .getPendingIntent( 0, PendingIntent.FLAG_UPDATE_CURRENT );

        String text =
                FormatUtils.truncateDecimal( receipt.getTotalAmount() ) + " " +
                receipt.getTCurrency();

        Uri defaultSoundUri = RingtoneManager.getDefaultUri( RingtoneManager.TYPE_NOTIFICATION );
        mBuilder = new NotificationCompat.Builder( this )
                .setSmallIcon( getNotificationIcon() )
                .setContentTitle( getString( R.string.new_recipt ) )
                .setContentText( getString( R.string.paid ) + " " + text )
                .setAutoCancel( true )
                .setSound( defaultSoundUri )
                .setContentIntent( pendingIntent );

        mNotificationManager = (NotificationManager) getSystemService( Context.NOTIFICATION_SERVICE );
        mNotificationManager.notify( 0, mBuilder.build() );
    }

    /**
     * Gets the correct icon for notifications depending on the Android version
     * @return The drawable with the correct icon
     */
    private int getNotificationIcon() {
        boolean useWhiteIcon = (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP);
        return useWhiteIcon ? R.drawable.ic_notification : R.drawable.ic_launcher;
    }
}
