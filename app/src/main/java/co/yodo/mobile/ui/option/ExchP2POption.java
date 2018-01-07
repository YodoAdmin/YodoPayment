package co.yodo.mobile.ui.option;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import co.yodo.mobile.R;
import co.yodo.mobile.business.component.totp.TOTPUtils;
import co.yodo.mobile.business.network.ApiClient;
import co.yodo.mobile.business.network.model.ServerResponse;
import co.yodo.mobile.business.network.request.AuthenticateRequest;
import co.yodo.mobile.business.network.request.ExchRequest;
import co.yodo.mobile.helper.AlertDialogHelper;
import co.yodo.mobile.helper.FormatUtils;
import co.yodo.mobile.helper.PreferencesHelper;
import co.yodo.mobile.helper.ProgressDialogHelper;
import co.yodo.mobile.model.db.P2PTransfer;
import co.yodo.mobile.ui.BaseActivity;
import co.yodo.mobile.ui.option.contract.IRequestOption;
import co.yodo.mobile.utils.ErrorUtils;
import co.yodo.mobile.utils.PhoneNumberUtils;
import co.yodo.mobile.utils.PipUtils;
import timber.log.Timber;

/**
 * Created by yodop on 2017-07-22.
 * Implements the P2P Option of the MainActivity
 */
public class ExchP2POption extends IRequestOption {
    /** Regex validation */
    private static final String NUMBER_REGEX = "^\\d+$";

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
                if (PipUtils.validate(activity, etInput, null)) {
                    final String pip = TOTPUtils.defaultOTP(etInput.getText().toString());

                    ProgressDialogHelper.create(activity);
                    requestManager.invoke(
                            new AuthenticateRequest(uuidToken, pip),
                            new ApiClient.RequestCallback() {
                                @Override
                                public void onResponse(ServerResponse response) {
                                    ProgressDialogHelper.dismiss();
                                    final String code = response.getCode();

                                    switch (code) {
                                        case ServerResponse.AUTHORIZED:
                                            alertDialog.dismiss();
                                            showP2PDialog();
                                            break;

                                        case ServerResponse.ERROR_INCORRECT_PIP:
                                            tilPip.setError(activity.getString( R.string.error_pip));
                                            break;

                                        default:
                                            ErrorUtils.handleError(
                                                    activity,
                                                    activity.getString(R.string.error_server),
                                                    false
                                            );
                                    }
                                }

                                @Override
                                public void onError(String message) {
                                    ProgressDialogHelper.dismiss();
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
                buildOnClick(okClickAuth)
        );
    }

    @Override
    public void execute() {
        alertDialog.show();
        clearGUI();
    }

    private void showP2PDialog() {
        // Build dialog
        LayoutInflater inflater = this.activity.getLayoutInflater();
        final View layout = inflater.inflate(R.layout.dialog_p2p, new LinearLayout(this.activity), false);

        // Setup UI
        final AutoCompleteTextView etTransferAccount = layout.findViewById(R.id.tietTransferAccount);
        final EditText etTransferAmount = layout.findViewById(R.id.tietTransferAmount);
        final TextInputLayout tilTransferAccount = (TextInputLayout) etTransferAccount.getParent().getParent();
        final TextInputLayout tilTransferAmount = (TextInputLayout) etTransferAmount.getParent().getParent();

        // Build adapter
        List<P2PTransfer> p2PTransfers = P2PTransfer.find(P2PTransfer.class, null, null, null, null, "3");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                activity,
                android.R.layout.simple_dropdown_item_1line,
                p2pAccountsDBtoArray(p2PTransfers)
        );
        etTransferAccount.setAdapter(adapter);
        etTransferAccount.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    etTransferAccount.showDropDown();
                }
            }
        });

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
                    tilTransferAccount.setError(activity.getString(R.string.error_required_field));
                    error = true;
                }
                else if (!transferAccount.matches(NUMBER_REGEX)) {
                    tilTransferAccount.setError(activity.getString(R.string.error_wrong_field_format));
                    error = true;
                }

                if (transferAmount.isEmpty()) {
                    tilTransferAmount.setError(activity.getString(R.string.error_required_field));
                    error = true;
                }

                if (!error) {
                    ProgressDialogHelper.create(activity);
                    requestManager.invoke(
                            // Add 00 to the account (phone number)
                            new ExchRequest(uuidToken, String.format("00%s", transferAccount), transferAmount),
                            new ApiClient.RequestCallback() {
                                @Override
                                public void onResponse(ServerResponse response) {
                                    ProgressDialogHelper.dismiss();
                                    final String code = response.getCode();

                                    switch (code) {
                                        case ServerResponse.AUTHORIZED:
                                            List<P2PTransfer> accounts = P2PTransfer.find(P2PTransfer.class, "phone_number = ?", transferAccount);
                                            if (accounts.isEmpty()) {
                                                new P2PTransfer(transferAccount).save();
                                            }

                                            adP2PDialog.dismiss();
                                            Snackbar.make(
                                                    activity.findViewById(android.R.id.content),
                                                    R.string.text_transfer_successful,
                                                    Snackbar.LENGTH_SHORT
                                            ).show();

                                            // Trim the balance
                                            PreferencesHelper.saveBalance(String.format("%s %s",
                                                    FormatUtils.truncateDecimal(response.getParams().getBalance()),
                                                    response.getParams().getCurrency()
                                            ));
                                            activity.updateData();
                                            break;

                                        case ServerResponse.ERROR_PRIMARY_ACCOUNT:
                                            ErrorUtils.handleError(
                                                    activity,
                                                    activity.getString(R.string.error_account_primary),
                                                    false
                                            );
                                            break;

                                        case ServerResponse.ERROR_SECONDARY_ACCOUNT:
                                            ErrorUtils.handleError(
                                                    activity,
                                                    activity.getString(R.string.error_account_secondary),
                                                    false
                                            );
                                            break;

                                        case ServerResponse.ERROR_INSUFFICIENT_FUNDS:
                                            ErrorUtils.handleError(
                                                    activity,
                                                    activity.getString(R.string.error_insufficient_funds),
                                                    false
                                            );
                                            break;

                                        case ServerResponse.ERROR_AMOUNT_EXCEEDED:
                                            ErrorUtils.handleError(
                                                    activity,
                                                    activity.getString(R.string.error_exceeded_funds),
                                                    false
                                            );
                                            break;

                                        default:
                                            ErrorUtils.handleError(
                                                    activity,
                                                    activity.getString(R.string.error_server),
                                                    false
                                            );
                                    }
                                }

                                @Override
                                public void onError(String message) {
                                    ProgressDialogHelper.dismiss();
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
                buildP2PButton(okClick)
        ).show();
    }

    /**
     * Builds a listener for the positive button
     * @param onPositive The new procedure for the positive button
     * @return The listener
     */
    private DialogInterface.OnShowListener buildP2PButton(final View.OnClickListener onPositive) {
        return new DialogInterface.OnShowListener() {
            @Override
            public void onShow( DialogInterface dialog ) {
                // Get the AlertDialog and the positive Button
                adP2PDialog = AlertDialog.class.cast(dialog);
                final Button button = adP2PDialog.getButton(AlertDialog.BUTTON_POSITIVE);

                // Sets the action for the positive Button
                button.setOnClickListener(onPositive);
            }
        };
    }

    private String[] p2pAccountsDBtoArray(List<P2PTransfer> p2pAccounts) {
        String[] arrayOfAccounts = new String[p2pAccounts.size()];
        for (int i = 0; i < p2pAccounts.size(); i++) {
            arrayOfAccounts[i] = p2pAccounts.get(i).getPhoneNumber();
        }
        return arrayOfAccounts;
    }

    private static class P2PAdapter extends ArrayAdapter<P2PTransfer> {
        /** Data */
        private List<P2PTransfer> data;

        /** Layout for the dropdown */
        private int layout;

        P2PAdapter(@NonNull Context context, int layout, List<P2PTransfer> data) {
            super(context, layout);
            this.data = data;
            this.layout = layout;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            Timber.e("Position" + data.get(position).toString());
            ViewHolderItem viewHolder;
            if (convertView == null) {

                // inflate the layout
                LayoutInflater inflater = ((Activity)  getContext()).getLayoutInflater();
                convertView = inflater.inflate(layout, parent, false);

                // well set up the ViewHolder
                viewHolder = new ViewHolderItem();
                viewHolder.textViewItem = convertView.findViewById(android.R.id.text1);

                // store the holder with the view.
                convertView.setTag(viewHolder);

            } else {
                viewHolder = (ViewHolderItem) convertView.getTag();
            }

            P2PTransfer p2pTransfer = data.get(position);
            if (p2pTransfer != null) {
                viewHolder.textViewItem.setText(p2pTransfer.getPhoneNumber());
            }

            return convertView;
        }

        static class ViewHolderItem {
            TextView textViewItem;
        }
    }
}
