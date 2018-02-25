package co.yodo.mobile.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import co.yodo.mobile.R;
import co.yodo.mobile.ui.dialog.contract.IDialog;

/**
 * Created by hei on 16/06/16.
 * builds a sks dialog from data
 */
public class SKSDialog extends IDialog {
    /**
     * Constructor that shows the dialog
     * based in the DialogBuilder
     * @param builder The DialogBuilder
     */
    private SKSDialog( Builder builder ) {
        super( builder );
        // Brightness to set after show
        float brightness = builder.brightness;

        //Time to dismiss the dialog
        int timeToDismiss = builder.timeToDismiss;

        // Verify the window
        final Window window = this.dialog.getWindow();
        if( window == null ) {
            throw new NullPointerException( "Window shouldn't be null" );
        }

        // Get the current brightness
        final WindowManager.LayoutParams lp = this.dialog.getWindow().getAttributes();
        final float brightnessNow = lp.screenBrightness;

        // Set the brightness
        lp.screenBrightness = brightness;
        window.setAttributes( lp );

        // Set onDismiss listener to restore brightness
        this.dialog.setOnDismissListener( new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss( DialogInterface dialog ) {
                lp.screenBrightness = brightnessNow;
                window.setAttributes( lp );
            }
        });

        // Create dismiss handler
        Handler handler = new Handler();
        if( timeToDismiss >= 0 ) {
            handler.postDelayed( new Runnable() {
                @Override
                public void run() {
                    if( dialog.isShowing() ) {
                        dialog.dismiss();
                    }
                }
            }, timeToDismiss );
        }
    }

    /**
     * Builder for the receipts
     */
    public static class Builder extends DialogBuilder {
        /** GUI Controllers */
        private ImageView ivCode;

        /** Brightness to set after show */
        private float brightness;

        /** Time to dismiss the dialog */
        private int timeToDismiss = -1; // Negative - No dismiss

        /**
         * Builder constructor with the mandatory elements
         * @param context The application context
         */
        public Builder( Context context ) {
            super( context, R.layout.dialog_with_image );
            // Data
            this.ivCode = dialog.findViewById( R.id.ivSks );
        }

        @Override
        public Builder cancelable( boolean cancelable ) {
            this.cancelable = cancelable;
            return this;
        }

        public Builder code( Bitmap code ) {
            this.ivCode.setImageBitmap( code );
            return this;
        }

        public Builder brightness( float brightness ) {
            this.brightness = brightness;
            return this;
        }

        public Builder dismiss( int time ) {
            this.timeToDismiss = time;
            return this;
        }

        public Builder dismissKey( final int key ) {
            this.dialog.setOnKeyListener( new Dialog.OnKeyListener() {
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
