package co.yodo.mobile.ui.option;

import android.view.View;

import co.yodo.mobile.R;
import co.yodo.mobile.business.component.totp.TOTPUtils;
import co.yodo.mobile.business.network.ApiClient;
import co.yodo.mobile.business.network.model.ServerResponse;
import co.yodo.mobile.business.network.request.QueryRequest;
import co.yodo.mobile.helper.AlertDialogHelper;
import co.yodo.mobile.helper.FormatUtils;
import co.yodo.mobile.helper.PrefUtils;
import co.yodo.mobile.ui.BaseActivity;
import co.yodo.mobile.ui.option.contract.IRequestOption;
import co.yodo.mobile.utils.ErrorUtils;
import co.yodo.mobile.utils.PipUtils;

/**
 * Created by hei on 14/06/16.
 * Implements the Balance Option of the MainActivity
 */
public class BalanceOption extends IRequestOption {
    /**
     * Sets up the main elements of the options
     * @param activity The Activity to handle
     */
    public BalanceOption( final BaseActivity activity ) {
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
                            new QueryRequest( hardwareToken, pip ),
                            new ApiClient.RequestCallback() {
                                @Override
                                public void onResponse( ServerResponse response ) {
                                    progressManager.destroy();
                                    final String code = response.getCode();

                                    switch( code ) {
                                        case ServerResponse.AUTHORIZED_BALANCE:
                                            alertDialog.dismiss();

                                            // Trim the balance
                                            PrefUtils.saveBalance( String.format( "%s %s",
                                                    FormatUtils.truncateDecimal( response.getParams().getBalance() ),
                                                    response.getParams().getCurrency()
                                            ) );
                                            activity.updateData();
                                            break;

                                        case ServerResponse.ERROR_NO_BALANCE:
                                            ErrorUtils.handleError(
                                                    activity,
                                                    activity.getString( R.string.error_balance ),
                                                    false
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
