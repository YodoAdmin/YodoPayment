package co.yodo.mobile.ui.option;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.support.design.widget.Snackbar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

import javax.inject.Inject;

import co.yodo.mobile.R;
import co.yodo.mobile.YodoApplication;
import co.yodo.mobile.helper.AppConfig;
import co.yodo.mobile.model.db.Coupon;
import co.yodo.mobile.ui.BaseActivity;
import co.yodo.mobile.helper.ProgressDialogHelper;
import co.yodo.mobile.ui.option.contract.IOption;
import co.yodo.mobile.helper.AlertDialogHelper;
import co.yodo.mobile.utils.GuiUtils;
import it.sephiroth.android.library.imagezoom.ImageViewTouch;

/**
 * Created by hei on 14/06/16.
 * Implements the Save Coupon Option for the Main Activity
 */
public class SaveCouponOption extends IOption {
    /** Progress dialog for the requests */
    @Inject
    protected ProgressDialogHelper progressManager;

    /** GUI controllers */
    private ImageViewTouch ivtPromotion;

    /** Extension of the promotions */
    private static final String IMAGE_EXT = ".png";

    /** OnClick action */
    private final DialogInterface.OnClickListener onClick;

    /**
     * Sets up the main elements of the options
     * @param activity The Activity to handle
     */
    public SaveCouponOption( BaseActivity activity ) {
        super( activity );

        // Injection
        YodoApplication.getComponent().inject( this );

        // Create the dialog
        onClick = new DialogInterface.OnClickListener() {
            @Override
            public void onClick( DialogInterface dialog, int item ) {
                // Gets the external storage
                final File directory = new File( Environment.getExternalStorageDirectory(), AppConfig.COUPONS_FOLDER );
                boolean success = true;

                // Verify if the directory exists or create it
                if( !directory.exists() ) {
                    success = directory.mkdir();
                }

                // If the directory doesn't exist and it was no possible to create it, send an alert message
                if( !success ) {
                    Snackbar.make( ivtPromotion, R.string.error_promotion_save, Snackbar.LENGTH_LONG ).show();
                    return;
                }

                new Thread( new Runnable() {
                    public void run() {
                        // Get the number of files and add one to the add
                        final Drawable drawable = ivtPromotion.getDrawable();
                        if( drawable != null ) {
                            File image = new File( directory, UUID.randomUUID().toString() + IMAGE_EXT );
                            Bitmap bitmap = GuiUtils.drawableToBitmap( drawable );
                            final CharSequence description = ivtPromotion.getContentDescription();

                            try {
                                FileOutputStream outStream = new FileOutputStream( image );
                                bitmap.compress( Bitmap.CompressFormat.PNG, 100, outStream );

                                outStream.flush();
                                outStream.close();

                                Coupon coupon = new Coupon( image.getPath(), description.toString() );
                                coupon.save();
                            } catch( IOException e ) {
                                e.printStackTrace();
                            }
                        }
                    }
                } ).start();
            }
        };
    }

    @Override
    public void execute() {
        AlertDialogHelper.show(
                activity,
                R.string.text_promotion,
                onClick
        );
    }

    /**
     * Sets an image to get the promotion information
     * @param ivtPromotion The image view
     */
    public IOption setPromotionImage( ImageViewTouch ivtPromotion ) {
        this.ivtPromotion = ivtPromotion;
        return this;
    }
}
