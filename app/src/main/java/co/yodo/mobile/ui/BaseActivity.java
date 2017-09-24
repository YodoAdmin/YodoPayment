package co.yodo.mobile.ui;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.ButterKnife;
import co.yodo.mobile.R;
import co.yodo.mobile.business.service.YodoGCMListenerService;
import co.yodo.mobile.helper.PreferencesHelper;
import co.yodo.mobile.model.db.Receipt;
import co.yodo.mobile.model.dtos.ErrorEvent;
import co.yodo.mobile.ui.dialog.ReceiptDialog;
import co.yodo.mobile.ui.dialog.contract.IDialog;
import co.yodo.mobile.ui.notification.ToastMaster;
import co.yodo.mobile.utils.ErrorUtils;
import co.yodo.mobile.utils.GuiUtils;

/**
 * Created by hei on 08/03/17.
 * Base activity for the Yodo Payment application
 */
public class BaseActivity extends AppCompatActivity {
    /** Account identifier */
    protected String uuidToken;

    /** Any dialog that is in the front */
    private IDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GuiUtils.setLanguage(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    /**
     * Setups the GUI controllers
     */
    protected void setupGUI(Bundle savedInstanceState) {
        // Injection
        ButterKnife.bind(this);

        // Setup the toolbar
        GuiUtils.setActionBar(this);
    }

    /**
     * Sets the main values, you should call it in all
     * the activities that extend this class
     */
    public void updateData() {
        uuidToken = PreferencesHelper.getUuidToken();
        // Gets the hardware token - account identifier
        /*uuidToken = PreferencesHelper.getHardwareToken();
        if (uuidToken == null ) {
            ToastMaster.makeText(this, R.string.error_hardware, Toast.LENGTH_LONG ).show();
            finish();
        }*/
    }

    /**
     * Use this function to set any dialog that we want to dismiss or take
     * any other action over it in the future
     * @param dialog The dialog to handle
     */
    public void setDialog( IDialog dialog ) {
        this.dialog = dialog;
    }

    /**
     * Handles the receipt by showing a dialog
     * @param receipt The receipt information
     */
    private void buildReceiptDialog(final Receipt receipt) {
        final View.OnClickListener onSave = new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                onSaveReceipt(receipt);

                // Show a notification which can reverse the save
                Snackbar.make(findViewById( android.R.id.content), R.string.text_receipt_saved, Snackbar.LENGTH_LONG)
                        .setAction(R.string.text_undo, new View.OnClickListener() {
                            @Override
                            public void onClick( View v ) {
                                // Disable the button to avoid double click
                                v.setEnabled(false);
                                buildReceiptDialog(receipt);
                            }
                        }).show();
            }
        };

        final View.OnClickListener onDelete = new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                onDeleteReceipt(receipt);

                // Show a notification which can reverse the delete
                Snackbar.make(findViewById(android.R.id.content), R.string.text_receipt_deleted, Snackbar.LENGTH_LONG)
                        .setAction( R.string.text_undo, new View.OnClickListener() {
                            @Override
                            public void onClick( View v ) {
                                // Disable the button to avoid double click, and save the receipt
                                v.setEnabled(false);
                                receipt.save();
                                buildReceiptDialog(receipt);
                            }
                        } ).show();
            }
        };

        new ReceiptDialog.Builder(this)
                .description(receipt.getDescription())
                .created(receipt.getCreated())
                .total(receipt.getTotalAmount(), receipt.getTCurrency())
                .authnumber(receipt.getAuthNumber())
                .donor(receipt.getDonorAccount())
                .recipient(receipt.getRecipientAccount())
                .tender(receipt.getTenderAmount(), receipt.getDCurrency())
                .cashback(receipt.getCashBackAmount(), receipt.getTCurrency())
                .save(onSave)
                .delete(onDelete)
                .build();
    }

    public void onDeleteReceipt(Receipt receipt) {
        receipt.delete();
    }

    public void onSaveReceipt(Receipt receipt) {
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ErrorEvent event) {
        // Show an error message
        final String message = event.message;
        if (event.tag == ErrorEvent.TYPE.SNACKBAR) {
            Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show();
        } else {
            ErrorUtils.handleError(
                    this,
                    message,
                    false
            );
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onResponseEvent(final Receipt receipt) {
        // Remove any notification
        EventBus.getDefault().removeStickyEvent(receipt);
        YodoGCMListenerService.cancelNotification(this);

        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }

        // Update the GUI
        buildReceiptDialog(receipt);
        if (this instanceof PaymentActivity) {
            updateData();
        }
    }
}
