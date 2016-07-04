package co.yodo.mobile.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.ImageView;

import co.yodo.mobile.R;
import co.yodo.mobile.ui.dialog.contract.IDialog;

/**
 * Created by hei on 16/06/16.
 * builds a sks dialog from data
 */
public class SKSDialog extends IDialog {
    /** Brightness to set after show */
    private final float mBrightness;

    /** Time to dismiss the dialog */
    private final int mTime;

    /** Handler for the dismiss */
    private final Handler mHandler;

    /**
     * Constructor that shows the dialog
     * based in the DialogBuilder
     * @param builder The DialogBuilder
     */
    private SKSDialog( Builder builder ) {
        super( builder );
        // Data
        this.mBrightness = builder.mBrightness;
        this.mTime = builder.mTime;

        // Get the current brightness
        final WindowManager.LayoutParams lp = this.mDialog.getWindow().getAttributes();
        final float brightnessNow = lp.screenBrightness;

        // Set the brightness
        lp.screenBrightness = this.mBrightness;
        mDialog.getWindow().setAttributes( lp );

        // Set onDismiss listener to restore brightness
        this.mDialog.setOnDismissListener( new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss( DialogInterface dialog ) {
                lp.screenBrightness = brightnessNow;
                mDialog.getWindow().setAttributes( lp );
            }
        });

        // Create dismiss handler
        this.mHandler = new Handler();
        if( this.mTime >= 0 ) {
            this.mHandler.postDelayed( new Runnable() {
                @Override
                public void run() {
                    if( mDialog.isShowing() )
                        mDialog.dismiss();
                }
            }, this.mTime );
        }
    }

    /**
     * Builder for the receipts
     */
    public static class Builder extends DialogBuilder {
        /** GUI Controllers */
        private ImageView ivCode;

        /** Brightness to set after show */
        private float mBrightness;

        /** Time to dismiss the dialog */
        private int mTime = -1; // Negative - No dismiss

        /**
         * Builder constructor with the mandatory elements
         * @param context The application context
         */
        public Builder( Context context ) {
            super( context, R.layout.dialog_sks );
            // Data
            this.ivCode = (ImageView) mDialog.findViewById( R.id.sks );
        }

        @Override
        public Builder cancelable( boolean cancelable ) {
            this.mCancelable = cancelable;
            return this;
        }

        public Builder code( Bitmap code ) {
            this.ivCode.setImageBitmap( code );
            return this;
        }

        public Builder brightness( float brightness ) {
            this.mBrightness = brightness;
            return this;
        }

        public Builder dismiss( int time ) {
            this.mTime = time;
            return this;
        }

        public Builder dismissKey( final int key ) {
            this.mDialog.setOnKeyListener( new Dialog.OnKeyListener() {
                @Override
                public boolean onKey( DialogInterface dialog, int keyCode, KeyEvent event ) {
                    if( key == keyCode )
                        dialog.dismiss();
                    return true;
                }
            });
            return this;
        }

        @Override
        public SKSDialog build() {
            return new SKSDialog( this );
        }
    }
}
