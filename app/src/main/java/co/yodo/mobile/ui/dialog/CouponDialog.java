package co.yodo.mobile.ui.dialog;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import co.yodo.mobile.R;
import co.yodo.mobile.ui.dialog.contract.IDialog;

/**
 * Created by hei on 30/06/16.
 * builds a dialog for the coupons
 */
public class CouponDialog extends IDialog {
    /**
     * Constructor that shows the dialog
     * based in the DialogBuilder
     * @param builder The DialogBuilder
     */
    private CouponDialog( Builder builder ) {
        super( builder );
    }

    /**
     * Builder for the receipts
     */
    public static class Builder extends DialogBuilder {
        /** GUI Controllers */
        private ImageView ivCoupon;

        public Builder( Context context ) {
            super( context, R.layout.dialog_with_image );
            // Data
            this.ivCoupon = (ImageView) dialog.findViewById( R.id.ivSks );
            this.ivCoupon.setOnTouchListener( new View.OnTouchListener() {
                @Override
                public boolean onTouch( View v, MotionEvent event ) {
                    dialog.dismiss();
                    return false;
                }
            } );
        }

        @Override
        public Builder cancelable( boolean cancelable ) {
            this.cancelable = cancelable;
            return this;
        }

        public Builder image( Bitmap image ) {
            int nh = (int) ( image.getHeight() * ( 512.0 / image.getWidth() ) );
            Bitmap scaled = Bitmap.createScaledBitmap( image, 512, nh, true );
            this.ivCoupon.setImageBitmap( scaled );
            return this;
        }

        @Override
        public IDialog build() {
            return new CouponDialog( this );
        }
    }
}
