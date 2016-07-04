package co.yodo.mobile.ui.option;

import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.view.View;

import co.yodo.mobile.R;
import co.yodo.mobile.helper.FormatUtils;
import co.yodo.mobile.helper.GUIUtils;
import co.yodo.mobile.helper.PrefUtils;
import co.yodo.mobile.network.YodoRequest;
import co.yodo.mobile.network.model.ServerResponse;
import co.yodo.mobile.network.request.QueryRequest;
import co.yodo.mobile.ui.MainActivity;
import co.yodo.mobile.ui.option.contract.IOption;
import co.yodo.mobile.ui.notification.AlertDialogHelper;
import co.yodo.mobile.ui.notification.ProgressDialogHelper;
import co.yodo.mobile.ui.notification.YodoHandler;
import co.yodo.mobile.ui.validator.PIPValidator;

/**
 * Created by hei on 14/06/16.
 * Implements the Balance Option of the MainActivity
 */
public class BalanceOption extends IOption implements YodoRequest.RESTListener {
    /** Elements for the request */
    private final YodoRequest mRequestManager;
    private final YodoHandler mHandlerMessages;
    private final String mHardwareToken;

    /** Elements for the AlertDialog */
    private final String mTitle;
    private final PIPValidator mValidator;

    /** GUI elements */
    private final DrawerLayout mDrawerLayout;

    /** Response codes for the server requests */
    private static final int QUERY_BAL_REQ = 0x04;

    /**
     * Sets up the main elements of the options
     * @param activity The Activity to handle
     */
    public BalanceOption( MainActivity activity, YodoRequest requestManager, YodoHandler handlerMessages ) {
        super( activity );
        // Request
        this.mRequestManager  = requestManager;
        this.mHandlerMessages = handlerMessages;
        this.mHardwareToken   = PrefUtils.getHardwareToken( this.mActivity );

        // AlertDialog
        this.mTitle     = this.mActivity.getString( R.string.input_pip );
        this.mValidator = new PIPValidator( this.etInput );

        // GUI
        mDrawerLayout = ( DrawerLayout ) this.mActivity.findViewById( R.id.drawerLayout );
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
                    mRequestManager.setListener( BalanceOption.this );
                    mRequestManager.invoke( new QueryRequest(
                            QUERY_BAL_REQ,
                            mHardwareToken,
                            pip
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
    public void onPrepare() {
        PrefUtils.setSubscribing( this.mActivity, false );
    }

    @Override
    public void onResponse( int responseCode, ServerResponse response ) {
        // Set listener to the principal activity
        ProgressDialogHelper.getInstance().destroyProgressDialog();
        mRequestManager.setListener( ( (MainActivity) this.mActivity ) );

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
                        ( (MainActivity) this.mActivity ).setBalance( tvBalance );
                        break;

                    case ServerResponse.ERROR_NO_BALANCE:
                        // Clear the balance
                        ( (MainActivity) this.mActivity ).setBalance( "" );
                        Snackbar.make(
                                mDrawerLayout,
                                R.string.message_error_no_balance,
                                Snackbar.LENGTH_SHORT
                        ).show();
                        break;

                    default:
                        // Clear the balance
                        ( (MainActivity) this.mActivity ).setBalance( "" );
                        String message = response.getMessage();
                        YodoHandler.sendMessage( mHandlerMessages, code, message );
                        break;
                }
                break;
        }
    }
}
