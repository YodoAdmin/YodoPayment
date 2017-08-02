package co.yodo.mobile.business.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import co.yodo.mobile.R;
import co.yodo.mobile.helper.FormatUtils;
import co.yodo.mobile.helper.PrefUtils;
import co.yodo.mobile.model.db.Receipt;
import co.yodo.mobile.model.dtos.Transfer;
import co.yodo.mobile.ui.PaymentActivity;
import co.yodo.mobile.utils.JsonUtils;
import timber.log.Timber;

/**
 * Created by yodop on 2017-07-27.
 * Any message handling beyond receiving notifications on apps in the background.
 */
public class YodoMessagingService extends FirebaseMessagingService {
    /** Notification for arriving messages */
    private NotificationManager notificationManager;
    private static final int RECEIPT_NOTIFICATION_ID = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) getSystemService( Context.NOTIFICATION_SERVICE );
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Timber.d("From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            try {
                Timber.d("Message data payload: " + remoteMessage.getData());
                String message = remoteMessage.getData().get("message");

                if (!JsonUtils.isValidJson(message)) {
                    return;
                }

                try {
                    JSONObject json = new JSONObject(message);
                    final String type = json.getString("type");
                    if (type.equals("transfer")) {
                        Transfer transfer = Transfer.fromJSON(message);
                        sendTransferNotification(transfer);
                        return;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                try {
                    Receipt receipt = Receipt.fromJSON(message);
                    receipt.save();
                    sendReceiptNotification(receipt);
                    EventBus.getDefault().postSticky(receipt);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            final String notification =  remoteMessage.getNotification().getBody();
            Timber.d("Message Notification Body: " + notification);
            NotificationCompat.Builder builder = getNotificationBuilder().setContentText(notification);
            notificationManager.notify(RECEIPT_NOTIFICATION_ID, builder.build());
        }
    }

    /**
     * Dismiss the current notification
     */
    public static void cancelNotification( Context context ) {
        NotificationManager nManager = (NotificationManager) context.getSystemService( Context.NOTIFICATION_SERVICE );
        nManager.cancelAll();
    }

    /**
     * Create and show a simple notification containing the received GCM message.
     * @param receipt GCM message received as a Receipt
     */
    private synchronized void sendReceiptNotification(Receipt receipt ) {
        // We only update the balance if we paid the receipt
        if( receipt.getDonorAccount() == null ) {
            final String balance = receipt.getBalanceAmount();
            final String currency = receipt.getCurrency();
            updateBalance(balance, currency);
        }

        final String text = FormatUtils.truncateDecimal( receipt.getTotalAmount() ) + " " +  receipt.getTCurrency();
        NotificationCompat.Builder builder = getNotificationBuilder()
                .setContentTitle( getString( R.string.text_receipt_notification_title ) )
                .setContentText( getString( R.string.text_receipt_notification_text, text ) );

        notificationManager.notify( RECEIPT_NOTIFICATION_ID, builder.build() );
    }

    /**
     * Create and show a simple notification containing the received GCM message.
     * @param transfer GCM message received as a Transfer
     */
    private synchronized void sendTransferNotification(Transfer transfer) {
        final String balance = transfer.getAccountBalance();
        final String currency = transfer.getAccountCurrency();
        updateBalance(balance, currency);

        final String temp = FormatUtils.truncateDecimal( transfer.getAmount() ) + " " +  transfer.getCurrency();
        final String from = getString( R.string.text_transfer_notification_from, transfer.getFrom() );
        final String amount = getString( R.string.text_transfer_notification_amount, temp );
        NotificationCompat.Builder builder = getNotificationBuilder()
                .setContentTitle( getString( R.string.text_transfer_notification_title ) )
                .setStyle(new NotificationCompat.InboxStyle()
                        .addLine(from)
                        .addLine(amount)
                );

        notificationManager.notify( RECEIPT_NOTIFICATION_ID, builder.build() );
    }

    /**
     * Sets the basic attributes for the notification
     * @return The notification builder
     */
    private NotificationCompat.Builder getNotificationBuilder() {
        Intent intent = new Intent( this, PaymentActivity.class );
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        final Uri defaultSoundUri = RingtoneManager.getDefaultUri( RingtoneManager.TYPE_NOTIFICATION );
        return new NotificationCompat.Builder( this )
                .setSmallIcon( getNotificationIcon() )
                .setAutoCancel( true )
                .setSound( defaultSoundUri )
                .setContentIntent( pendingIntent );
    }

    /**
     * Gets the correct icon for notifications depending on the Android version
     * @return The drawable with the correct icon
     */
    private int getNotificationIcon() {
        boolean useWhiteIcon = (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP);
        return useWhiteIcon ? R.mipmap.ic_notification : R.mipmap.ic_launcher;
    }

    private void updateBalance(String balance, String currency) {
        // Trim the balance
        PrefUtils.saveBalance( String.format( "%s %s",
                FormatUtils.truncateDecimal( balance ), currency
        ) );
    }
}