package co.yodo.mobile.ui.option;

import android.view.View;
import android.widget.Toast;

import co.yodo.mobile.R;
import co.yodo.mobile.business.component.totp.TOTPUtils;
import co.yodo.mobile.business.network.ApiClient;
import co.yodo.mobile.business.network.model.ServerResponse;
import co.yodo.mobile.business.network.request.ResetPIPRequest;
import co.yodo.mobile.helper.AlertDialogHelper;
import co.yodo.mobile.ui.BaseActivity;
import co.yodo.mobile.ui.notification.ToastMaster;
import co.yodo.mobile.ui.option.contract.IRequestOption;
import co.yodo.mobile.utils.ErrorUtils;
import co.yodo.mobile.utils.PipUtils;

/**
 * Created by hei on 04/08/16.
 * Implements the Reset pip Option of the ResetPIPActivity
 */
public class ResetPipOption extends IRequestOption  {
    /** The new pip */
    private String newPip;

    public ResetPipOption( final BaseActivity activity ) {
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
                            new ResetPIPRequest( hardwareToken, pip, newPip ),
                            new ApiClient.RequestCallback() {
                                @Override
                                public void onResponse( ServerResponse response ) {
                                    progressManager.dismiss();
                                    final String code = response.getCode();

                                    switch( code ) {
                                        case ServerResponse.AUTHORIZED:
                                            alertDialog.dismiss();
                                            activity.finish();
                                            ToastMaster.makeText( activity, R.string.text_update_successful, Toast.LENGTH_LONG ).show();
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
                                    progressManager.dismiss();
                                    ErrorUtils.handleError(
                                            activity,
                                            message,
                                            false
                                    );
                                }
                            } );
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

    /**
     * Sets the new pip that will be updated
     * @param newPip The new secret pip
     */
    public IRequestOption setNewPip( String newPip ) {
        this.newPip = newPip;
        return this;
    }
}
