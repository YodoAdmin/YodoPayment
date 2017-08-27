package co.yodo.mobile.ui.option;

import android.view.View;

import co.yodo.mobile.R;
import co.yodo.mobile.business.component.totp.TOTPUtils;
import co.yodo.mobile.business.network.ApiClient;
import co.yodo.mobile.business.network.model.ServerResponse;
import co.yodo.mobile.business.network.request.QueryRequest;
import co.yodo.mobile.helper.AlertDialogHelper;
import co.yodo.mobile.ui.BaseActivity;
import co.yodo.mobile.ui.LinkedAccountsActivity;
import co.yodo.mobile.ui.option.contract.IRequestOption;
import co.yodo.mobile.utils.ErrorUtils;
import co.yodo.mobile.utils.PipUtils;

/**
 * Created by hei on 14/06/16.
 * Implements the DeLink Account Option of the MainActivity
 */
public class LinkedAccountsOption extends IRequestOption {
    /**
     * Sets up the main elements of the options
     * @param activity The Activity to handle
     */
    public LinkedAccountsOption( final BaseActivity activity ) {
        super( activity );

        // Dialog
        final View layout = buildLayout();
        final View.OnClickListener okClick = new View.OnClickListener() {
            @Override
            public void onClick( View view  ) {
                if( PipUtils.validate( activity, etInput, null ) ) {
                    final String pip = etInput.getText().toString();
                    final String otp = TOTPUtils.defaultOTP( pip );

                    progressManager.create( activity );
                    requestManager.invoke(
                            new QueryRequest( hardwareToken, otp, QueryRequest.Record.LINKED_ACCOUNTS ),
                            new ApiClient.RequestCallback() {
                                @Override
                                public void onResponse( ServerResponse response ) {
                                    progressManager.dismiss();
                                    final String code = response.getCode();

                                    switch( code ) {
                                        case ServerResponse.AUTHORIZED:
                                            alertDialog.dismiss();
                                            final String to = response.getParams().getLinkedTo();
                                            final String from = response.getParams().getLinkedFrom();
                                            LinkedAccountsActivity.newInstance( activity, to, from, pip );
                                            break;

                                        case ServerResponse.ERROR_INCORRECT_PIP:
                                            tilPip.setError( activity.getString( R.string.error_pip ) );
                                            break;

                                        case ServerResponse.ERROR_NO_LINKS:
                                            ErrorUtils.handleError(
                                                    activity,
                                                    activity.getString( R.string.error_linked_accounts ),
                                                    false
                                            );
                                            break;

                                        case ServerResponse.ERROR_FAILED:
                                            ErrorUtils.handleError(
                                                    activity,
                                                    activity.getString( R.string.error_server ),
                                                    false
                                            );
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
                                    progressManager.dismiss();
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
