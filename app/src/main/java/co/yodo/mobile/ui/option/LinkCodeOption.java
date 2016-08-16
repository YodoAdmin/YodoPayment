package co.yodo.mobile.ui.option;

import android.app.Dialog;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import co.yodo.mobile.R;
import co.yodo.mobile.helper.GUIUtils;
import co.yodo.mobile.helper.PrefUtils;
import co.yodo.mobile.network.ApiClient;
import co.yodo.mobile.network.model.ServerResponse;
import co.yodo.mobile.network.request.QueryRequest;
import co.yodo.mobile.ui.MainActivity;
import co.yodo.mobile.ui.notification.AlertDialogHelper;
import co.yodo.mobile.ui.notification.ToastMaster;
import co.yodo.mobile.ui.notification.YodoHandler;
import co.yodo.mobile.ui.option.contract.IRequestOption;

/**
 * Created by hei on 14/06/16.
 * Implements the Generate Link Code Option of the MainActivity
 */
public class LinkCodeOption extends IRequestOption implements ApiClient.RequestsListener {
    /** Response codes for the server requests */
    private static final int QUERY_LNK_REQ = 0x04;

    /**
     * Sets up the main elements of the options
     * @param activity The Activity to handle
     */
    public LinkCodeOption( MainActivity activity, YodoHandler handlerMessages ) {
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
                        mRequestManager.setListener( LinkCodeOption.this );
                        mRequestManager.invoke( new QueryRequest(
                                QUERY_LNK_REQ,
                                mHardwareToken,
                                pip,
                                QueryRequest.Record.LINKING_CODE
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
            case QUERY_LNK_REQ:
                if( code.equals( ServerResponse.AUTHORIZED ) ) {
                    // Get the linking code
                    final String linking_code = response.getParam( ServerResponse.LINKING_CODE );

                    // Create the dialog for the code
                    Dialog dialog = new Dialog( mActivity );
                    dialog.requestWindowFeature( Window.FEATURE_NO_TITLE );
                    dialog.setContentView( R.layout.dialog_linking_code );

                    // Setup the elements
                    TextView codeText = (TextView) dialog.findViewById( R.id.codeText );
                    ImageView codeImage = (ImageView) dialog.findViewById( R.id.copyCodeImage );
                    codeText.setText( linking_code );

                    codeImage.setOnClickListener( new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            GUIUtils.copyCode( mActivity, linking_code );
                            ToastMaster.makeText( mActivity, R.string.copied_text, Toast.LENGTH_SHORT ).show();
                        }
                    });

                    dialog.show();
                } else {
                    String message = response.getMessage();
                    YodoHandler.sendMessage( mHandlerMessages, code, message );
                }
                break;
        }
    }
}
