package co.yodo.mobile.ui.option;

import android.app.Dialog;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import co.yodo.mobile.R;
import co.yodo.mobile.business.component.totp.TOTPUtils;
import co.yodo.mobile.business.network.ApiClient;
import co.yodo.mobile.business.network.model.ServerResponse;
import co.yodo.mobile.business.network.request.QueryRequest;
import co.yodo.mobile.helper.AlertDialogHelper;
import co.yodo.mobile.ui.BaseActivity;
import co.yodo.mobile.ui.option.contract.IRequestOption;
import co.yodo.mobile.utils.ErrorUtils;
import co.yodo.mobile.utils.GuiUtils;
import co.yodo.mobile.utils.PipUtils;

/**
 * Created by hei on 14/06/16.
 * Implements the Generate Link Code Option of the MainActivity
 */
public class LinkingCodeOption extends IRequestOption {
    /**
     * Sets up the main elements of the options
     * @param activity The Activity to handle
     */
    public LinkingCodeOption( final BaseActivity activity ) {
        super( activity );

        // Dialog
        final View layout = buildLayout();
        final View.OnClickListener okClick = new View.OnClickListener() {
            @Override
            public void onClick( View view  ) {
                if( PipUtils.validate( activity, etInput, null ) ) {
                    final String pip = TOTPUtils.defaultOTP( etInput.getText().toString() );

                    progressManager.create( activity );
                    requestManager.invoke(
                            new QueryRequest( hardwareToken, pip, QueryRequest.Record.LINKING_CODE ),
                            new ApiClient.RequestCallback() {
                                @Override
                                public void onResponse( ServerResponse response ) {
                                    progressManager.destroy();
                                    final String code = response.getCode();

                                    switch( code ) {
                                        case ServerResponse.AUTHORIZED:
                                            alertDialog.dismiss();

                                            // Get the linking code
                                            final String linking_code = response.getParams().getLinkingCode();

                                            // Create the dialog for the code
                                            Dialog dialog = new Dialog( activity );
                                            dialog.requestWindowFeature( Window.FEATURE_NO_TITLE );
                                            dialog.setContentView( R.layout.dialog_linking_code );

                                            // Setup the elements
                                            TextView codeText = (TextView) dialog.findViewById( R.id.tvCode );
                                            ImageView codeImage = (ImageView) dialog.findViewById( R.id.ivCopy );
                                            codeText.setText( linking_code );

                                            codeImage.setOnClickListener( new View.OnClickListener() {
                                                @Override
                                                public void onClick( View v ) {
                                                    GuiUtils.copyCode( activity, linking_code );
                                                    Snackbar.make( v, R.string.text_link_code_clipboard, Toast.LENGTH_SHORT ).show();
                                                }
                                            });

                                            dialog.show();
                                            break;

                                        case ServerResponse.ERROR_INCORRECT_PIP:
                                            tilPip.setError( activity.getString( R.string.error_pip ) );
                                            break;

                                        default:
                                            ErrorUtils.handleError(
                                                    activity,
                                                    activity.getString( R.string.error_server ),
                                                    false
                                            );
                                            break;
                                    }
                                }

                                @Override
                                public void onError( String message ) {
                                    progressManager.destroy();
                                    ErrorUtils.handleError(
                                            activity,
                                            message,
                                            false
                                    );
                                }
                            }
                    );
                }
            }
        };

        alertDialog = AlertDialogHelper.create(
                activity,
                layout,
                buildOnClick( okClick )
        );
    }

    @Override
    public void execute() {
        alertDialog.show();
        clearGUI();
    }
}
