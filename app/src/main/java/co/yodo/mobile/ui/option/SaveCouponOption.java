package co.yodo.mobile.ui.option;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;

import co.yodo.mobile.R;
import co.yodo.mobile.helper.AppConfig;
import co.yodo.mobile.ui.MainActivity;
import co.yodo.mobile.ui.option.contract.IOption;
import co.yodo.mobile.ui.notification.AlertDialogHelper;

/**
 * Created by hei on 14/06/16.
 * Implements the Save Coupon Option for the Main Activity
 */
public class SaveCouponOption extends IOption {
    /**
     * Sets up the main elements of the options
     * @param activity The Activity to handle
     */
    public SaveCouponOption( Activity activity ) {
        super( activity );
    }

    @Override
    public void execute() {
         DialogInterface.OnClickListener onClick = new DialogInterface.OnClickListener() {
            @Override
            public void onClick( DialogInterface dialog, int item ) {
                // Gets the external storage
                File directory = new File( Environment.getExternalStorageDirectory(), AppConfig.COUPONS_FOLDER );
                boolean success = true;

                // Verify if the directory exists
                if( !directory.exists() )
                    success = directory.mkdir();

                // If the directory doesn't exist and it was no possible to create, send an alert message
                if( !success ) {
                    Toast.makeText( mActivity, R.string.image_saved_failed, Toast.LENGTH_SHORT ).show();
                    return;
                }

                // Get the number of files and add one to the add
                int files = directory.listFiles().length;
                File image = new File( directory, "ad" + (files + 1) + ".png" );
                ( ( MainActivity) mActivity ).saveCoupon( image );
            }
        };

        AlertDialogHelper.showAlertDialog(
                this.mActivity,
                R.string.save_image,
                onClick
        );
    }
}
