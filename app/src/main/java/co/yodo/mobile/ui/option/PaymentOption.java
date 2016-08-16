package co.yodo.mobile.ui.option;

import android.view.View;

import co.yodo.mobile.helper.PrefUtils;
import co.yodo.mobile.network.ApiClient;
import co.yodo.mobile.network.model.ServerResponse;
import co.yodo.mobile.network.request.AuthenticateRequest;
import co.yodo.mobile.ui.MainActivity;
import co.yodo.mobile.ui.notification.AlertDialogHelper;
import co.yodo.mobile.ui.notification.YodoHandler;
import co.yodo.mobile.ui.option.contract.IRequestOption;

/**
 * Created by hei on 14/06/16.
 * Implements the Payment Option of the MainActivity
 */
public class PaymentOption extends IRequestOption implements ApiClient.RequestsListener {
    /** Temporal */
    private String mTempPIP;

    /** Response codes for the server requests */
    private static final int AUTH_REQ = 0x00;

    /**
     * Sets up the main elements of the options
     * @param activity The Activity to handle
     */
    public PaymentOption( MainActivity activity, YodoHandler handlerMessages ) {
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
                        final String pip = etInput.getText().toString();
                        setTempPIP( pip );

                        // Start the request
                        mProgressManager.createProgressDialog( mActivity );
                        mRequestManager.setListener( PaymentOption.this );
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
        mRequestManager.setListener( (MainActivity) mActivity );

        // Get the response code
        String code = response.getCode();

        switch( responseCode ) {
            case AUTH_REQ:
                if( code.equals( ServerResponse.AUTHORIZED ) ) {
                    ( (MainActivity) mActivity).payment( mTempPIP );
                } else {
                    String message = response.getMessage();
                    YodoHandler.sendMessage( mHandlerMessages, code, message );
                }

                setTempPIP( null );
                break;
        }
    }
}
