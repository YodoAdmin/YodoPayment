package co.yodo.mobile.ui.option;

import android.content.DialogInterface;
import android.view.View;

import co.yodo.mobile.R;
import co.yodo.mobile.business.component.totp.TOTPUtils;
import co.yodo.mobile.business.network.ApiClient;
import co.yodo.mobile.business.network.model.ServerResponse;
import co.yodo.mobile.business.network.request.CloseRequest;
import co.yodo.mobile.helper.AlertDialogHelper;
import co.yodo.mobile.ui.BaseActivity;
import co.yodo.mobile.ui.option.contract.IRequestOption;
import co.yodo.mobile.utils.ErrorUtils;
import co.yodo.mobile.utils.PipUtils;
import co.yodo.mobile.utils.PreferenceUtils;

/**
 * Created by hei on 14/06/16.
 * Implements the Clouse Account Option for the MainActivity
 */
public class CloseAccountOption extends IRequestOption {
    /**
     * Sets up the main elements of the options
     * @param activity The Activity to handle
     */
    public CloseAccountOption( final BaseActivity activity ) {
        super( activity );

        // Dialog
        final View layout = buildLayout();
        final View.OnClickListener okClick = new View.OnClickListener() {
            @Override
            public void onClick( View view  ) {
                if( PipUtils.validate( activity, etInput, null ) ) {
                    final String otp = TOTPUtils.defaultOTP( etInput.getText().toString() );

                    progressManager.create( activity, R.string.text_account_closing );
                    requestManager.invoke(
                            new CloseRequest( hardwareToken, otp ),
                            new ApiClient.RequestCallback() {
                                @Override
                                public void onResponse( ServerResponse response ) {
                                    progressManager.destroy();
                                    final String code = response.getCode();

                                    switch( code ) {
                                        case ServerResponse.AUTHORIZED:
                                            // Destroy the dialog and clears the saved data
                                            alertDialog.dismiss();
                                            PreferenceUtils.clearUserData();

                                            // Setups the AlertDialog
                                            DialogInterface.OnClickListener onClick = new DialogInterface.OnClickListener() {
                                                public void onClick( DialogInterface dialog, int which ) {
                                                    activity.finish();
                                                }
                                            };

                                            // Display the dialog
                                            AlertDialogHelper.show(
                                                    activity,
                                                    activity.getString( R.string.text_farewell_tittle ),
                                                    activity.getString( R.string.text_farewell_message ),
                                                    onClick
                                            );
                                            break;

                                        case ServerResponse.ERROR_INCORRECT_PIP:
                                            tilPip.setError( activity.getString( R.string.error_pip ) );
                                            break;

                                        default:
                                            ErrorUtils.handleError(
                                                    activity,
                                                    activity.getString( R.string.error_unknown ),
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
                R.string.text_account_close_warning,
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
