package co.yodo.mobile.ui.option;

import android.view.View;

import co.yodo.mobile.R;
import co.yodo.mobile.helper.PrefUtils;
import co.yodo.mobile.network.YodoRequest;
import co.yodo.mobile.network.model.ServerResponse;
import co.yodo.mobile.network.request.LinkRequest;
import co.yodo.mobile.ui.MainActivity;
import co.yodo.mobile.ui.option.contract.IOption;
import co.yodo.mobile.ui.notification.AlertDialogHelper;
import co.yodo.mobile.ui.notification.ProgressDialogHelper;
import co.yodo.mobile.ui.notification.YodoHandler;

/**
 * Created by hei on 14/06/16.
 * Implements the Input Linking Code Option of the MainActivity
 */
public class LinkAccountOption extends IOption implements YodoRequest.RESTListener {
    /** Elements for the request */
    private final YodoRequest mRequestManager;
    private final YodoHandler mHandlerMessages;
    private final String mHardwareToken;

    /** Elements for the AlertDialog */
    private final String mTitle;

    /** Response codes for the server requests */
    private static final int LINK_REQ = 0x10;

    /**
     * Sets up the main elements of the options
     * @param activity The Activity to handle
     */
    public LinkAccountOption( MainActivity activity, YodoRequest requestManager, YodoHandler handlerMessages ) {
        super( activity );
        // Request
        this.mRequestManager  = requestManager;
        this.mHandlerMessages = handlerMessages;
        this.mHardwareToken   = PrefUtils.getHardwareToken( this.mActivity );

        // AlertDialog
        this.mTitle = this.mActivity.getString( R.string.input_linking_code );
    }

    @Override
    public void execute() {
        View.OnClickListener positiveClick = new View.OnClickListener() {
            @Override
            public void onClick( View view ) {
                mAlertDialog.dismiss();
                final String linkingCode = etInput.getText().toString();

                // Start the request, and set the listener to this object
                ProgressDialogHelper.getInstance().createProgressDialog( mActivity );
                mRequestManager.setListener( LinkAccountOption.this );
                mRequestManager.invoke( new LinkRequest(
                        LINK_REQ,
                        mHardwareToken,
                        linkingCode
                ) );
            }
        };

        AlertDialogHelper.showAlertDialog(
                this.mActivity,
                this.mTitle,
                R.string.show_linking_code,
                this.etInput,
                buildOnClick( positiveClick )
        );
    }

    @Override
    public void onResponse( int responseCode, ServerResponse response ) {
        // Set listener to the principal activity
        ProgressDialogHelper.getInstance().destroyProgressDialog();
        mRequestManager.setListener( ( (MainActivity) this.mActivity ) );

        // Get the response code
        String code = response.getCode();

        switch( responseCode ) {
            case LINK_REQ:
                String message = response.getMessage();
                YodoHandler.sendMessage( mHandlerMessages, code, message );
                break;
        }
    }
}
