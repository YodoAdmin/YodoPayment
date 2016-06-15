package co.yodo.mobile.ui.extension;

import android.content.Intent;
import android.view.View;

import co.yodo.mobile.R;
import co.yodo.mobile.component.Intents;
import co.yodo.mobile.helper.GUIUtils;
import co.yodo.mobile.helper.PrefUtils;
import co.yodo.mobile.network.YodoRequest;
import co.yodo.mobile.network.model.ServerResponse;
import co.yodo.mobile.network.request.QueryRequest;
import co.yodo.mobile.ui.DeLinkActivity;
import co.yodo.mobile.ui.MainActivity;
import co.yodo.mobile.ui.extension.contract.IOption;
import co.yodo.mobile.ui.notification.AlertDialogHelper;
import co.yodo.mobile.ui.notification.ProgressDialogHelper;
import co.yodo.mobile.ui.notification.YodoHandler;
import co.yodo.mobile.ui.validator.PIPValidator;

/**
 * Created by hei on 14/06/16.
 * Implements the DeLink Account Option of the MainActivity
 */
public class DeLinkAccountOption extends IOption implements YodoRequest.RESTListener {
    /** Elements for the request */
    private final YodoRequest mRequestManager;
    private final YodoHandler mHandlerMessages;
    private final String mHardwareToken;

    /** Elements for the AlertDialog */
    private final String mTitle;
    private final PIPValidator mValidator;

    /** Temporal */
    private String mTempPIP;

    /** Response codes for the server requests */
    private static final int QUERY_LNK_ACC_REQ = 0x04;

    /**
     * Sets up the main elements of the options
     * @param activity The Activity to handle
     */
    public DeLinkAccountOption( MainActivity activity, YodoRequest requestManager, YodoHandler handlerMessages ) {
        super( activity );
        // Request
        this.mRequestManager  = requestManager;
        this.mHandlerMessages = handlerMessages;
        this.mHardwareToken   = PrefUtils.getHardwareToken( this.mActivity );

        // AlertDialog
        this.mTitle     = this.mActivity.getString( R.string.input_pip );
        this.mValidator = new PIPValidator( this.etInput );
    }

    private void setTempPIP( String tempPIP ) {
        this.mTempPIP = tempPIP;
    }

    @Override
    public void execute() {
        View.OnClickListener positiveClick = new View.OnClickListener() {
            @Override
            public void onClick( View view ) {
                GUIUtils.hideSoftKeyboard( mActivity );

                if( mValidator.validate() ) {
                    mAlertDialog.dismiss();
                    setTempPIP( etInput.getText().toString() );

                    ProgressDialogHelper.getInstance().createProgressDialog( mActivity );
                    mRequestManager.setListener( DeLinkAccountOption.this );
                    mRequestManager.invoke( new QueryRequest(
                            QUERY_LNK_ACC_REQ,
                            mHardwareToken,
                            mTempPIP,
                            QueryRequest.Record.LINKED_ACCOUNTS
                    ) );
                }
            }
        };

        AlertDialogHelper.showAlertDialog(
                this.mActivity,
                this.mTitle,
                this.etInput,
                buildOnClick( positiveClick )
        );
    }

    @Override
    public void onResponse( int responseCode, ServerResponse response ) {
        // Set listener to the principal activity
        ProgressDialogHelper.getInstance().destroyProgressDialog();
        mRequestManager.setListener( ( (MainActivity) this.mActivity) );

        // Get the response code
        String code = response.getCode();
        String message;

        switch( responseCode ) {
            case QUERY_LNK_ACC_REQ:
                switch( code ) {
                    case ServerResponse.AUTHORIZED:
                        String from = response.getParam( ServerResponse.FROM );
                        String to = response.getParam( ServerResponse.TO );

                        Intent i = new Intent( this.mActivity, DeLinkActivity.class );
                        i.putExtra( Intents.LINKED_ACC_TO, to );
                        i.putExtra( Intents.LINKED_ACC_FROM, from );
                        i.putExtra( Intents.LINKED_PIP, this.mTempPIP );
                        this.mActivity.startActivity( i );
                        break;

                    // We don't have links
                    case ServerResponse.ERROR_FAILED:
                        message = this.mActivity.getString( R.string.error_message_no_links );
                        YodoHandler.sendMessage( mHandlerMessages, code, message );
                        break;

                    // If it is something else, show the error
                    default:
                        message = response.getMessage();
                        YodoHandler.sendMessage( mHandlerMessages, code, message );
                        break;
                }

                setTempPIP( null );
                break;
        }
    }
}
