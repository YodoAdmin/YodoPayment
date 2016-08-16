package co.yodo.mobile.ui.option;

import android.content.DialogInterface;
import android.view.View;

import co.yodo.mobile.R;
import co.yodo.mobile.helper.PrefUtils;
import co.yodo.mobile.network.ApiClient;
import co.yodo.mobile.network.model.ServerResponse;
import co.yodo.mobile.network.request.CloseRequest;
import co.yodo.mobile.ui.MainActivity;
import co.yodo.mobile.ui.notification.AlertDialogHelper;
import co.yodo.mobile.ui.notification.YodoHandler;
import co.yodo.mobile.ui.option.contract.IRequestOption;

/**
 * Created by hei on 14/06/16.
 * Implements the Clouse Account Option for the MainActivity
 */
public class CloseAccountOption extends IRequestOption implements ApiClient.RequestsListener {
    /** Response codes for the server requests */
    private static final int CLOSE_REQ = 0x08;

    /**
     * Sets up the main elements of the options
     * @param activity The Activity to handle
     */
    public CloseAccountOption( MainActivity activity, YodoHandler handlerMessages ) {
        super( activity, handlerMessages );

        // Dialog
        final View layout = buildLayout();
        final View.OnClickListener okClick = new View.OnClickListener() {
            @Override
            public void onClick( View view  ) {
                try {
                    if( mPipValidator.validate( etInput ) ) {
                        mAlertDialog.dismiss();
                        final String pip = etInput.getText().toString();

                        mProgressManager.createProgressDialog( mActivity );
                        mRequestManager.setListener( CloseAccountOption.this );
                        mRequestManager.invoke( new CloseRequest(
                                CLOSE_REQ,
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
                R.string.close_message,
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
        mRequestManager.setListener( (MainActivity) mActivity );

        // Get the response code
        String code = response.getCode();

        switch( responseCode ) {
            case CLOSE_REQ:
                if( code.equals( ServerResponse.AUTHORIZED ) ) {
                    // Clears the saved data
                    ( (MainActivity) mActivity ).clearSavedData();

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
