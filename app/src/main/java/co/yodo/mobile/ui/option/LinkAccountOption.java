package co.yodo.mobile.ui.option;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import co.yodo.mobile.R;
import co.yodo.mobile.helper.PrefUtils;
import co.yodo.mobile.network.ApiClient;
import co.yodo.mobile.network.model.ServerResponse;
import co.yodo.mobile.network.request.LinkRequest;
import co.yodo.mobile.ui.MainActivity;
import co.yodo.mobile.ui.notification.AlertDialogHelper;
import co.yodo.mobile.ui.notification.YodoHandler;
import co.yodo.mobile.ui.option.contract.IRequestOption;

/**
 * Created by hei on 14/06/16.
 * Implements the Input Linking Code Option of the MainActivity
 */
public class LinkAccountOption extends IRequestOption implements ApiClient.RequestsListener {
    /** Response codes for the server requests */
    private static final int LINK_REQ = 0x10;

    /**
     * Sets up the main elements of the options
     * @param activity The Activity to handle
     */
    public LinkAccountOption( MainActivity activity, YodoHandler handlerMessages ) {
        super( activity, handlerMessages );

        // Dialog
        LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        final View layout = inflater.inflate( R.layout.dialog_with_code, new LinearLayout( mActivity ), false );
        etInput = (EditText) layout.findViewById( R.id.cetLinkingCode );
        final View.OnClickListener okClick = new View.OnClickListener() {
            @Override
            public void onClick( View view  ) {
                mAlertDialog.dismiss();
                final String linkingCode = etInput.getText().toString();

                // Start the request, and set the listener to this object
                mProgressManager.createProgressDialog( mActivity );
                mRequestManager.setListener( LinkAccountOption.this );
                mRequestManager.invoke( new LinkRequest(
                        LINK_REQ,
                        mHardwareToken,
                        linkingCode
                ) );
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
        etInput.setText( "" );
        mAlertDialog.show();
        etInput.requestFocus();
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
            case LINK_REQ:
                String message = response.getMessage();
                YodoHandler.sendMessage( mHandlerMessages, code, message );
                break;
        }
    }
}
