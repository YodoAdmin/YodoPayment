package co.yodo.mobile.ui.option;

import android.view.View;

import co.yodo.mobile.component.totp.TOTPUtils;
import co.yodo.mobile.helper.AppConfig;
import co.yodo.mobile.helper.FormatUtils;
import co.yodo.mobile.helper.PrefUtils;
import co.yodo.mobile.network.ApiClient;
import co.yodo.mobile.network.model.ServerResponse;
import co.yodo.mobile.network.request.QueryRequest;
import co.yodo.mobile.ui.MainActivity;
import co.yodo.mobile.ui.notification.AlertDialogHelper;
import co.yodo.mobile.ui.notification.YodoHandler;
import co.yodo.mobile.ui.option.contract.IRequestOption;

/**
 * Created by hei on 14/06/16.
 * Implements the Balance Option of the MainActivity
 */
public class BalanceOption extends IRequestOption implements ApiClient.RequestsListener {
    /** Response codes for the server requests */
    private static final int QUERY_BAL_REQ = 0x04;

    /**
     * Sets up the main elements of the options
     * @param activity The Activity to handle
     */
    public BalanceOption( MainActivity activity, YodoHandler handlerMessages ) {
        super( activity, handlerMessages );

        // Dialog
        final View layout = buildLayout();
        final View.OnClickListener okClick = new View.OnClickListener() {
            @Override
            public void onClick( View view  ) {
                try {
                    if( mPipValidator.validate( etInput ) ) {
                        mAlertDialog.dismiss();
                        final String pip = TOTPUtils.defaultOTP( etInput.getText().toString() );

                        mProgressManager.createProgressDialog( mActivity );
                        mRequestManager.setListener( BalanceOption.this );
                        mRequestManager.invoke( new QueryRequest(
                                QUERY_BAL_REQ,
                                mHardwareToken,
                                pip
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
        mRequestManager.setListener( ( (MainActivity) mActivity ) );

        // Get the response code
        String code = response.getCode();

        switch( responseCode ) {
            case QUERY_BAL_REQ:
                switch( code ) {
                    case ServerResponse.AUTHORIZED_BALANCE:
                        final String tvBalance =
                                FormatUtils.truncateDecimal( response.getParam( ServerResponse.BALANCE ) ) + " " +
                                response.getParam( ServerResponse.CURRENCY );
                        // Trim the balance
                        ( (MainActivity) mActivity ).setBalance( tvBalance );
                        break;

                    case ServerResponse.ERROR_NO_BALANCE:
                        // Clear the balance
                        ( (MainActivity) mActivity ).setBalance( AppConfig.DEFAULT_BALANCE );
                        /*Snackbar.make(
                                mDrawerLayout,
                                R.string.message_error_no_balance,
                                Snackbar.LENGTH_LONG
                        ).show();*/
                        break;

                    default:
                        // Clear the balance
                        ( (MainActivity) mActivity ).setBalance( "" );
                        String message = response.getMessage();
                        YodoHandler.sendMessage( mHandlerMessages, code, message );
                        break;
                }
                break;
        }
    }
}
