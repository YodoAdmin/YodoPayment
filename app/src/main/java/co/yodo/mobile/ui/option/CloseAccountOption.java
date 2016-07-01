package co.yodo.mobile.ui.option;

import android.content.DialogInterface;
import android.view.View;

import co.yodo.mobile.R;
import co.yodo.mobile.helper.GUIUtils;
import co.yodo.mobile.helper.PrefUtils;
import co.yodo.mobile.network.YodoRequest;
import co.yodo.mobile.network.model.ServerResponse;
import co.yodo.mobile.network.request.CloseRequest;
import co.yodo.mobile.ui.MainActivity;
import co.yodo.mobile.ui.option.contract.IOption;
import co.yodo.mobile.ui.notification.AlertDialogHelper;
import co.yodo.mobile.ui.notification.ProgressDialogHelper;
import co.yodo.mobile.ui.notification.YodoHandler;
import co.yodo.mobile.ui.validator.PIPValidator;

/**
 * Created by hei on 14/06/16.
 * Implements the Clouse Account Option for the MainActivity
 */
public class CloseAccountOption extends IOption implements YodoRequest.RESTListener {
    /** Elements for the request */
    private final YodoRequest mRequestManager;
    private final YodoHandler mHandlerMessages;
    private final String mHardwareToken;

    /** Elements for the AlertDialog */
    private final String mTitle;
    private final String mMessage;
    private final PIPValidator mValidator;

    /** Response codes for the server requests */
    private static final int CLOSE_REQ = 0x08;

    /**
     * Sets up the main elements of the options
     * @param activity The Activity to handle
     */
    public CloseAccountOption( MainActivity activity, YodoRequest requestManager, YodoHandler handlerMessages ) {
        super( activity );
        // Request
        this.mRequestManager  = requestManager;
        this.mHandlerMessages = handlerMessages;
        this.mHardwareToken   = PrefUtils.getHardwareToken( this.mActivity );

        // AlertDialog
        this.mTitle     = this.mActivity.getString( R.string.input_pip );
        this.mMessage   = this.mActivity.getString( R.string.close_message );
        this.mValidator = new PIPValidator( this.etInput );
    }

    @Override
    public void execute() {
        View.OnClickListener positiveClick = new View.OnClickListener() {
            @Override
            public void onClick( View view ) {
                GUIUtils.hideSoftKeyboard( mActivity );

                if( mValidator.validate() ) {
                    mAlertDialog.dismiss();
                    final String pip = etInput.getText().toString();

                    ProgressDialogHelper.getInstance().createProgressDialog( mActivity );
                    mRequestManager.setListener( CloseAccountOption.this );
                    mRequestManager.invoke( new CloseRequest(
                            CLOSE_REQ,
                            mHardwareToken,
                            pip
                    ) );
                }
            }
        };

        AlertDialogHelper.showAlertDialog(
                this.mActivity,
                this.mTitle,
                this.mMessage,
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
            case CLOSE_REQ:
                if( code.equals( ServerResponse.AUTHORIZED ) ) {
                    // Clears the saved data
                    ( (MainActivity) this.mActivity).clearSavedData();

                    // Setups the AlertDialog
                    DialogInterface.OnClickListener onClick = new DialogInterface.OnClickListener() {
                        public void onClick( DialogInterface dialog, int which ) {
                            mActivity.finish();
                        }
                    };

                    AlertDialogHelper.showAlertDialog(
                            mActivity,
                            mActivity.getString( R.string.farewell_message_tittle ),
                            mActivity.getString( R.string.farewell_message ),
                            onClick
                    );
                } else {
                    String message = response.getMessage();
                    YodoHandler.sendMessage( mHandlerMessages, code, message );
                }
                break;
        }
    }
}
