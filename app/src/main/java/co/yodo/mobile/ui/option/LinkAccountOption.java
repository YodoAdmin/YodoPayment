package co.yodo.mobile.ui.option;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import co.yodo.mobile.R;
import co.yodo.mobile.business.network.ApiClient;
import co.yodo.mobile.business.network.model.ServerResponse;
import co.yodo.mobile.business.network.request.LinkRequest;
import co.yodo.mobile.helper.AlertDialogHelper;
import co.yodo.mobile.helper.ProgressDialogHelper;
import co.yodo.mobile.ui.BaseActivity;
import co.yodo.mobile.ui.option.contract.IRequestOption;
import co.yodo.mobile.utils.ErrorUtils;

/**
 * Created by hei on 14/06/16.
 * Implements the Input Linking Code Option of the MainActivity
 */
public class LinkAccountOption extends IRequestOption {
    /**
     * Sets up the main elements of the options
     * @param activity The Activity to handle
     */
    public LinkAccountOption( final BaseActivity activity ) {
        super( activity );

        // Build dialog
        LayoutInflater inflater = (LayoutInflater) this.activity.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        final View layout = inflater.inflate( R.layout.dialog_with_code, new LinearLayout( this.activity ), false );
        etInput = (TextInputEditText) layout.findViewById( R.id.tietLinkingCode );
        tilPip = (TextInputLayout) etInput.getParent().getParent();
        final View.OnClickListener okClick = new View.OnClickListener() {
            @Override
            public void onClick( View view ) {
                final String linkingCode = etInput.getText().toString();

                // Start the request, and set the listener to this object
                ProgressDialogHelper.create(activity);
                requestManager.invoke(
                        new LinkRequest(uuidToken, linkingCode ),
                        new ApiClient.RequestCallback() {
                            @Override
                            public void onResponse( ServerResponse response ) {
                                ProgressDialogHelper.dismiss();
                                final String code = response.getCode();

                                switch( code ) {
                                    case ServerResponse.AUTHORIZED:
                                        alertDialog.dismiss();
                                        Snackbar.make(
                                                activity.findViewById( android.R.id.content ),
                                                R.string.text_link_account_successful,
                                                Snackbar.LENGTH_SHORT
                                        ).show();
                                        break;

                                    case ServerResponse.ERROR_ALREADY_LINKED:
                                        tilPip.setError( activity.getString( R.string.error_already_linked ) );
                                        break;

                                    default:
                                        tilPip.setError( activity.getString( R.string.error_linking_code ) );
                                        break;
                                }
                            }

                            @Override
                            public void onError( String message ) {
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
