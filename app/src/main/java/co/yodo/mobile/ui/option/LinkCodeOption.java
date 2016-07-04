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
import co.yodo.mobile.network.YodoRequest;
import co.yodo.mobile.network.model.ServerResponse;
import co.yodo.mobile.network.request.QueryRequest;
import co.yodo.mobile.ui.MainActivity;
import co.yodo.mobile.ui.option.contract.IOption;
import co.yodo.mobile.ui.notification.AlertDialogHelper;
import co.yodo.mobile.ui.notification.ProgressDialogHelper;
import co.yodo.mobile.ui.notification.ToastMaster;
import co.yodo.mobile.ui.notification.YodoHandler;
import co.yodo.mobile.ui.validator.PIPValidator;

/**
 * Created by hei on 14/06/16.
 * Implements the Generate Link Code Option of the MainActivity
 */
public class LinkCodeOption extends IOption implements YodoRequest.RESTListener {
    /** Elements for the request */
    private final YodoRequest mRequestManager;
    private final YodoHandler mHandlerMessages;
    private final String mHardwareToken;

    /** Elements for the AlertDialog */
    private final String mTitle;
    private final PIPValidator mValidator;

    /** Response codes for the server requests */
    private static final int QUERY_LNK_REQ = 0x04;

    /**
     * Sets up the main elements of the options
     * @param activity The Activity to handle
     */
    public LinkCodeOption( MainActivity activity, YodoRequest requestManager, YodoHandler handlerMessages ) {
        super( activity );
        // Request
        this.mRequestManager  = requestManager;
        this.mHandlerMessages = handlerMessages;
        this.mHardwareToken   = PrefUtils.getHardwareToken( this.mActivity );

        // AlertDialog
        this.mTitle     = this.mActivity.getString( R.string.input_pip );
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
                    mRequestManager.setListener( LinkCodeOption.this );
                    mRequestManager.invoke( new QueryRequest(
                            QUERY_LNK_REQ,
                            mHardwareToken,
                            pip,
                            QueryRequest.Record.LINKING_CODE
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
            case QUERY_LNK_REQ:
                if( code.equals( ServerResponse.AUTHORIZED ) ) {
                    // Get the linking code
                    final String linking_code = response.getParam( ServerResponse.LINKING_CODE );

                    // Create the dialog for the code
                    Dialog dialog = new Dialog( this.mActivity );
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
                    YodoHandler.sendMessage( this.mHandlerMessages, code, message );
                }
                break;
        }
    }
}
