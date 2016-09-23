package co.yodo.mobile.ui.option;

import android.view.View;

import co.yodo.mobile.component.totp.TOTPUtils;
import co.yodo.mobile.helper.PrefUtils;
import co.yodo.mobile.network.ApiClient;
import co.yodo.mobile.network.model.ServerResponse;
import co.yodo.mobile.network.request.AuthenticateRequest;
import co.yodo.mobile.ui.ResetPIPActivity;
import co.yodo.mobile.ui.notification.AlertDialogHelper;
import co.yodo.mobile.ui.notification.YodoHandler;
import co.yodo.mobile.ui.option.contract.IRequestOption;

/**
 * Created by hei on 04/08/16.
 * Implements the Reset PIP Option of the ResetPIPActivity
 */
public class ResetPIPOption extends IRequestOption implements ApiClient.RequestsListener {
    /** Temporal */
    private String mTempPIP;

    /** Response codes for the server requests */
    private static final int AUTH_REQ  = 0x00;

    public ResetPIPOption( ResetPIPActivity activity, YodoHandler handlerMessages ) {
        super( activity, handlerMessages );

        // Dialog
        final View layout = buildLayout();
        final View.OnClickListener okClick = new View.OnClickListener() {
            @Override
            public void onClick( View view  ) {
                try {
                    if( mPipValidator.validate( etInput ) ) {
                        mAlertDialog.dismiss();

                        // Set a temporary PIP and Code
                        final String pip = TOTPUtils.defaultOTP( etInput.getText().toString() );
                        //final String pip = etInput.getText().toString();
                        setTempPIP( pip );

                        // Start the request
                        mProgressManager.createProgressDialog( mActivity );
                        mRequestManager.setListener( ResetPIPOption.this );
                        mRequestManager.invoke( new AuthenticateRequest(
                                AUTH_REQ,
                                mHardwareToken,
                                mTempPIP
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

    /**
     * Stores the pip in a temporal variable
     * @param tempPIP The PIP value
     */
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
        mRequestManager.setListener( (ResetPIPActivity) mActivity );

        // Get the response code
        String code = response.getCode();

        switch( responseCode ) {
            case AUTH_REQ:
                // If the auth is correct, let's change the password
                if( code.equals( ServerResponse.AUTHORIZED ) ) {
                    ( (ResetPIPActivity) mActivity ).doReset( mTempPIP );
                }
                // There was an error during the process
                else {
                    String message = response.getMessage();
                    YodoHandler.sendMessage( mHandlerMessages, code, message );
                }
                break;
        }
    }
}
