package co.yodo.mobile.ui.option;

import android.content.Context;
import android.content.DialogInterface;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import co.yodo.mobile.R;
import co.yodo.mobile.business.component.totp.TOTPUtils;
import co.yodo.mobile.business.network.ApiClient;
import co.yodo.mobile.business.network.model.ServerResponse;
import co.yodo.mobile.business.network.request.AuthenticateRequest;
import co.yodo.mobile.business.network.request.ExchRequest;
import co.yodo.mobile.helper.AlertDialogHelper;
import co.yodo.mobile.helper.FormatUtils;
import co.yodo.mobile.helper.PrefUtils;
import co.yodo.mobile.ui.BaseActivity;
import co.yodo.mobile.ui.option.contract.IRequestOption;
import co.yodo.mobile.utils.ErrorUtils;
import co.yodo.mobile.utils.PipUtils;

/**
 * Created by yodop on 2017-07-22.
 * Implements the P2P Option of the MainActivity
 */
public class ExchP2POption extends IRequestOption {
    /** UI Controllers */
    private AlertDialog adP2PDialog;

    /** Data Containers */
    private String transferAccount;
    private String transferAmount;

    /**
     * Sets up the main elements of the options
     * @param activity The Activity to handle
     */
    public ExchP2POption(final BaseActivity activity) {
        super(activity);

        // Setup Auth Dialog
        final View.OnClickListener okClickAuth = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if( PipUtils.validate( activity, etInput, null ) ) {
                    final String pip = TOTPUtils.defaultOTP(etInput.getText().toString());

                    progressManager.create(activity);
                    requestManager.invoke(
                            new AuthenticateRequest(hardwareToken, pip),
                            new ApiClient.RequestCallback() {
                                @Override
                                public void onResponse(ServerResponse response) {
                                    progressManager.destroy();
                                    final String code = response.getCode();

                                    switch( code ) {
                                        case ServerResponse.AUTHORIZED:
                                            alertDialog.dismiss();
                                            showP2PDialog();
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
                                    }
                                }

                                @Override
                                public void onError(String message) {
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
                buildLayout(),
                buildOnClick( okClickAuth )
        );
    }

    @Override
    public void execute() {
        alertDialog.show();
        clearGUI();
    }

    private void showP2PDialog() {
        // Build dialog
        LayoutInflater inflater = (LayoutInflater) this.activity.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        final View layout = inflater.inflate( R.layout.dialog_p2p, new LinearLayout( this.activity ), false );

        // Setup UI
        final EditText etTransferAccount = (EditText) layout.findViewById(R.id.tietTransferAccount);
        final EditText etTransferAmount = (EditText) layout.findViewById(R.id.tietTransferAmount);
        final TextInputLayout tilTransferAccount = (TextInputLayout) etTransferAccount.getParent().getParent();
        final TextInputLayout tilTransferAmount = (TextInputLayout) etTransferAmount.getParent().getParent();

        // Setup P2p Dialog
        final View.OnClickListener okClick = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tilTransferAccount.setError(null);
                tilTransferAmount.setError(null);

                transferAccount = etTransferAccount.getText().toString();
                transferAmount = etTransferAmount.getText().toString();
                boolean error = false;

                if (transferAccount.isEmpty()) {
                    tilTransferAccount.setError( activity.getString( R.string.error_required_field ) );
                    error = true;
                }

                if (transferAmount.isEmpty()) {
                    tilTransferAmount.setError( activity.getString( R.string.error_required_field ) );
                    error = true;
                }

                if (!error) {
                    progressManager.create(activity);
                    requestManager.invoke(
                            new ExchRequest(hardwareToken, transferAccount, transferAmount),
                            new ApiClient.RequestCallback() {
                                @Override
                                public void onResponse(ServerResponse response) {
                                    progressManager.destroy();
                                    final String code = response.getCode();

                                    switch( code ) {
                                        case ServerResponse.AUTHORIZED:
                                            adP2PDialog.dismiss();
                                            Snackbar.make(
                                                    activity.findViewById( android.R.id.content ),
                                                    R.string.text_transfer_successful,
                                                    Snackbar.LENGTH_SHORT
                                            ).show();

                                            // Trim the balance
                                            PrefUtils.saveBalance( String.format( "%s %s",
                                                    FormatUtils.truncateDecimal( response.getParams().getBalance() ),
                                                    response.getParams().getCurrency()
                                            ) );
                                            activity.updateData();
                                            break;

                                        case ServerResponse.ERROR_INSUFFICIENT_FUNDS:
                                            ErrorUtils.handleError(
                                                    activity,
                                                    activity.getString( R.string.error_insufficient_funds ),
                                                    false
                                            );
                                            break;

                                        case ServerResponse.ERROR_AMOUNT_EXCEEDED:
                                            ErrorUtils.handleError(
                                                    activity,
                                                    activity.getString( R.string.error_exceeded_funds ),
                                                    false
                                            );
                                            break;

                                        default:
                                            ErrorUtils.handleError(
                                                    activity,
                                                    activity.getString( R.string.error_server ),
                                                    false
                                            );
                                    }
                                }

                                @Override
                                public void onError(String message) {
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

        AlertDialogHelper.create(
                activity,
                layout,
                buildP2PButton( okClick )
        ).show();
    }

    /**
     * Builds a listener for the positive button
     * @param onPositive The new procedure for the positive button
     * @return The listener
     */
    private DialogInterface.OnShowListener buildP2PButton(final View.OnClickListener onPositive ) {
        return new DialogInterface.OnShowListener() {
            @Override
            public void onShow( DialogInterface dialog ) {
                // Get the AlertDialog and the positive Button
                adP2PDialog = AlertDialog.class.cast( dialog );
                final Button button = adP2PDialog.getButton( AlertDialog.BUTTON_POSITIVE );

                // Sets the action for the positive Button
                button.setOnClickListener( onPositive );
            }
        };
    }
}
