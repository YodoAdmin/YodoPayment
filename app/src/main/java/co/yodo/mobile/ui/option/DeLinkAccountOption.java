package co.yodo.mobile.ui.option;

import android.content.Intent;
import android.view.View;

import co.yodo.mobile.R;
import co.yodo.mobile.component.Intents;
import co.yodo.mobile.helper.PrefUtils;
import co.yodo.mobile.network.ApiClient;
import co.yodo.mobile.network.model.ServerResponse;
import co.yodo.mobile.network.request.QueryRequest;
import co.yodo.mobile.ui.DeLinkActivity;
import co.yodo.mobile.ui.MainActivity;
import co.yodo.mobile.ui.notification.AlertDialogHelper;
import co.yodo.mobile.ui.notification.YodoHandler;
import co.yodo.mobile.ui.option.contract.IRequestOption;
import co.yodo.mobile.ui.validator.PIPValidator;

/**
 * Created by hei on 14/06/16.
 * Implements the DeLink Account Option of the MainActivity
 */
public class DeLinkAccountOption extends IRequestOption implements ApiClient.RequestsListener {
    /** Temporal */
    private String mTempPIP;

    /** Response codes for the server requests */
    private static final int QUERY_LNK_ACC_REQ = 0x04;

    /**
     * Sets up the main elements of the options
     * @param activity The Activity to handle
     */
    public DeLinkAccountOption( MainActivity activity, YodoHandler handlerMessages ) {
        super( activity, handlerMessages );

        // Dialog
        final View layout = buildLayout();
        final View.OnClickListener okClick = new View.OnClickListener() {
            @Override
            public void onClick( View view  ) {
                try {
                    if( mPipValidator.validate( etInput ) ) {
                        mAlertDialog.dismiss();
                        setTempPIP( etInput.getText().toString() );

                        mProgressManager.createProgressDialog( mActivity );
                        mRequestManager.setListener( DeLinkAccountOption.this );
                        mRequestManager.invoke( new QueryRequest(
                                QUERY_LNK_ACC_REQ,
                                mHardwareToken,
                                mTempPIP,
                                QueryRequest.Record.LINKED_ACCOUNTS
                        ) );
                    }
                } catch( NoSuchFieldException e ) {
                    e.printStackTrace();
                }
            }
        };

        mAlertDialog = AlertDialogHelper.create(
                mActivity,
                layout,
                buildOnClick( okClick )
        );
    }

    private void setTempPIP( String tempPIP ) {
        this.mTempPIP = tempPIP;
    }

    @Override
    public void execute() {
        mAlertDialog.show();
        clearGUI();
    }

    @Override
    public void onPrepare() {
        PrefUtils.setSubscribing( mActivity, false );
    }

    @Override
    public void onResponse( int responseCode, ServerResponse response ) {
        // Set listener to the principal activity
        mProgressManager.destroyProgressDialog();
        mRequestManager.setListener( ( (MainActivity) mActivity) );

        // Get the response code
        String code = response.getCode();
        String message;

        switch( responseCode ) {
            case QUERY_LNK_ACC_REQ:
                switch( code ) {
                    case ServerResponse.AUTHORIZED:
                        String from = response.getParam( ServerResponse.FROM );
                        String to = response.getParam( ServerResponse.TO );

                        Intent i = new Intent( mActivity, DeLinkActivity.class );
                        i.putExtra( Intents.LINKED_ACC_TO, to );
                        i.putExtra( Intents.LINKED_ACC_FROM, from );
                        i.putExtra( Intents.LINKED_PIP, mTempPIP );
                        mActivity.startActivity( i );
                        break;

                    // We don't have links
                    case ServerResponse.ERROR_FAILED:
                        message = mActivity.getString( R.string.error_message_no_links );
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
