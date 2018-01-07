package co.yodo.mobile.utils;

import android.content.Context;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import co.yodo.mobile.R;

/**
 * Created by hei on 14/06/16.
 * Handles all the actions related with the pip
 */
public class PipUtils {
    /** The minimum size of the pip */
    private static final long PIP_MIN_LENGTH = 4;

    /**
     * Validates the pip and its confirmation to be equal and exists (min 4 length)
     * @param etPip The EditText to validate
     * @param etPipConfirm The EditText to confirm the pip
     * @return true if the verification was successful, or false if something failed
     */
    public static boolean validate(Context context, EditText etPip, EditText etPipConfirm) {
        // Get PIP parents, reset and get value
        TextInputLayout pipLayout = (TextInputLayout) etPip.getParent().getParent();
        pipLayout.setError(null);
        final String pip = etPip.getText().toString();

        // Get confirmation PIP parents, reset and get value
        TextInputLayout pipConfirmLayout = null;
        String pipConfirm = null;
        if (etPipConfirm != null) {
            pipConfirmLayout = (TextInputLayout) etPipConfirm.getParent().getParent();
            pipConfirmLayout.setError(null);
            pipConfirm = etPipConfirm.getText().toString();
        }

        String errorMessage = null;
        boolean valid = true;
        View focusView = null;

        // PIP validations
        if (TextUtils.isEmpty(pip)) {
            errorMessage = context.getString(R.string.error_required_field);
        } else if (pip.length() < PIP_MIN_LENGTH) {
            errorMessage = context.getString(R.string.error_pip_length);
        }

        if (errorMessage != null) {
            pipLayout.setError(errorMessage);
            errorMessage = null;
            focusView = etPip;
            valid = false;
        }

        // PIP confirm validation
        if (etPipConfirm != null) {
            if (TextUtils.isEmpty(pipConfirm)) {
                errorMessage = context.getString(R.string.error_required_field);
            } else if (!pip.equals(pipConfirm)) {
                errorMessage = context.getString(R.string.error_pip_match);
            }

            if (errorMessage != null) {
                pipConfirmLayout.setError(errorMessage);
                focusView = etPipConfirm;
                valid = false;
            }
        }

        if (!valid) {
            // There was an error
            focusView.requestFocus();
        }

        return valid;
    }
}
