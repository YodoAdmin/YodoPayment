package co.yodo.mobile.ui.extension;

import android.app.Activity;
import android.view.View;

import co.yodo.mobile.R;
import co.yodo.mobile.ui.MainActivity;
import co.yodo.mobile.ui.extension.contract.IOption;
import co.yodo.mobile.ui.notification.AlertDialogHelper;

/**
 * Created by hei on 14/06/16.
 * Implements the Input Linking Code Option of the MainActivity
 */
public class LinkAccountOption extends IOption {
    /**
     * Sets up the main elements of the options
     * @param activity The Activity to handle
     */
    public LinkAccountOption( Activity activity ) {
        super( activity );
    }

    @Override
    public void execute() {
        final String title = this.mActivity.getString( R.string.input_linking_code );

        View.OnClickListener positiveClick = new View.OnClickListener() {
            @Override
            public void onClick( View view ) {
                mAlertDialog.dismiss();
                ( (MainActivity ) mActivity).linkAccount( etInput );
            }
        };

        AlertDialogHelper.showAlertDialog(
                this.mActivity,
                title,
                R.string.show_linking_code,
                this.etInput,
                buildOnClick( positiveClick )
        );
    }
}
