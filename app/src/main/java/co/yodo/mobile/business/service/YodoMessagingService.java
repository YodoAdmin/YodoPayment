package co.yodo.mobile.business.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import co.yodo.mobile.R;
import co.yodo.mobile.helper.FormatUtils;
import co.yodo.mobile.helper.PreferencesHelper;
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
    /** Notification generals */
    private static final String CHANNEL_ID = "yodo_channel";
    private static final int NOTIFICATION_ID = 1;

    /** Notifications manager */
    private NotificationManager notificationManager;

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) getSystemService( Context.NOTIFICATION_SERVICE );
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Timber.e("From: " + remoteMessage.getFrom());

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
            final String content =  remoteMessage.getNotification().getBody();
            Timber.d("Message Notification Body: " + content);
            NotificationCompat.Builder builder = getNotificationBuilder();
            builder.setContentText(content);
            getManager().notify(NOTIFICATION_ID, builder.build());
        }
    }

    /**
     * Dismiss the current notification
     */
    public static void cancelNotification( Context context ) {
        NotificationManager nManager = (NotificationManager) context.getSystemService( Context.NOTIFICATION_SERVICE );
        if (nManager != null) {
            nManager.cancelAll();
        }
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
        getManager().notify(NOTIFICATION_ID, builder.build());
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
        getManager().notify(NOTIFICATION_ID, builder.build());
    }

    /**
     * Sets the basic attributes for the notification
     * @return The notification builder
     */
    private NotificationCompat.Builder getNotificationBuilder() {
        // Creates the resources for the notification
        final Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        // Creates an explicit intent for an Activity in your app
        Intent intent = new Intent(this, PaymentActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        // Builds the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(getNotificationIcon())
                .setSound(sound)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        // Channel for Android Oreo
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // The user-visible name of the channel.
            CharSequence name = getString(R.string.text_channel_name);
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_HIGH);

            // Audio settings
            AudioAttributes att = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build();

            // Configure the notification channel.
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setSound(sound, att);
            getManager().createNotificationChannel(channel);
        }

        return builder;
    }

    /**
     * Gets the notification manager, or initialize it if not already initialized
     * @return The notification manager object
     */
    private NotificationManager getManager() {
        if (notificationManager == null) {
            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return notificationManager;
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
        PreferencesHelper.saveBalance( String.format( "%s %s",
                FormatUtils.truncateDecimal( balance ), currency
        ) );
    }
}
