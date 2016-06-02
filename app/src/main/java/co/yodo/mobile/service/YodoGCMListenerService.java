package co.yodo.mobile.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.gcm.GcmListenerService;

import org.greenrobot.eventbus.EventBus;

import co.yodo.mobile.R;
import co.yodo.mobile.database.model.Receipt;
import co.yodo.mobile.network.model.ServerResponse;
import co.yodo.mobile.database.ReceiptsDataSource;
import co.yodo.mobile.helper.AppUtils;
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
        AppUtils.Logger( TAG, "From: " + from );
        AppUtils.Logger( TAG, "Message: " + message );

        /*if( from.startsWith( "/topics/" ) ) {
            // message received from some topic.
        } else {
            // normal downstream message.
        }*/

        /**
         * Production applications would usually process the message here.
         * Eg: - Syncing with server.
         *     - Store message in local database.
         *     - Update UI.
         */
        ServerResponse response = JSONHandler.parseReceipt( message );

        if( !AppUtils.isForeground( ac ) )
            sendNotification( response );
        else
            EventBus.getDefault().post( response );
    }

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param response GCM message received as a ServerResponse
     */
    private synchronized void sendNotification( ServerResponse response ) {
        try {
            // Database
            //ReceiptsDataSource receiptsdb = new ReceiptsDataSource( ac );
            ReceiptsDataSource receiptsdb = ReceiptsDataSource.getInstance( ac );
            final boolean isOpen = receiptsdb.isOpen();
            if( !isOpen )
                receiptsdb.open();

            final Receipt receipt = receiptsdb.createReceipt(
                    response.getParam( ServerResponse.AUTHNUMBER ),
                    response.getParam( ServerResponse.DESCRIPTION ),
                    response.getParam( ServerResponse.TCURRENCY ),
                    response.getParam( ServerResponse.EXCH_RATE ),
                    response.getParam( ServerResponse.DCURRENCY ),
                    AppUtils.truncateDecimal( response.getParam( ServerResponse.AMOUNT ) ),
                    AppUtils.truncateDecimal( response.getParam( ServerResponse.TAMOUNT ) ),
                    AppUtils.truncateDecimal( response.getParam( ServerResponse.CASHBACK ) ),
                    AppUtils.truncateDecimal( response.getParam( ServerResponse.BALANCE ) ),
                    response.getParam( ServerResponse.CURRENCY ),
                    response.getParam( ServerResponse.DONOR ),
                    response.getParam( ServerResponse.RECEIVER ),
                    response.getParam( ServerResponse.CREATED )
            );

            if( !isOpen )
                receiptsdb.close();
            EventBus.getDefault().post( receipt );
        } catch( NullPointerException e ) {
            e.printStackTrace();
        }

        Intent intent = new Intent( this, ReceiptsActivity.class );
        intent.addFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP );
        PendingIntent pendingIntent = PendingIntent.getActivity( this, 0, intent, PendingIntent.FLAG_ONE_SHOT );

        String text =
                AppUtils.truncateDecimal( response.getParam( ServerResponse.AMOUNT ) ) + " " +
                response.getParam( ServerResponse.TCURRENCY );

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
