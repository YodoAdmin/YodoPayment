package co.yodo.mobile.ui.dialog.contract;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;

/**
 * Created by hei on 16/06/16.
 * implements the Dialog abstract class
 */
public abstract class IDialog {
    /** Dialog to be build */
    protected final Dialog mDialog;

    /**
     * Constructor that shows the dialog
     * based in the DialogBuilder
     * @param builder The DialogBuilder
     */
    protected IDialog( DialogBuilder builder ) {
        this.mDialog = builder.mDialog;
        this.mDialog.setCancelable( builder.mCancelable );
        this.mDialog.show();
    }

    /**
     * Show the inner dialog
     */
    public void show() {
        this.mDialog.show();
    }

    /**
     * Dismiss the inner dialog
     */
    public void dismiss() {
        this.mDialog.dismiss();
    }

    /**
     * Abstract class for the Dialog Builders
     */
    protected static abstract class DialogBuilder {
        /** Context object */
        protected final Context mContext;

        /** Dialog to be build */
        protected final Dialog mDialog;

        /** Optional parameters */
        protected boolean mCancelable = false;

        /**
         * Builder constructor with the mandatory elements
         * @param context The application context
         * @param layout The layout for the dialog
         */
        protected DialogBuilder( Context context, int layout ) {
            this.mContext = context;
            mDialog = new Dialog( this.mContext );
            mDialog.requestWindowFeature( Window.FEATURE_NO_TITLE );
            mDialog.setContentView( layout );
        }

        /**
         * Builder constructor with the mandatory elements and a style
         * @param context The application context
         * @param layout The layout for the dialog
         */
        protected DialogBuilder( Context context, int layout, int style ) {
            this.mContext = context;
            mDialog = new Dialog( this.mContext, style );
            mDialog.requestWindowFeature( Window.FEATURE_NO_TITLE );
            mDialog.setContentView( layout );
        }

        /**
         * Sets the dialog cancelable or not
         * @param cancelable A boolean, true if cancelable
         * @return The DialogBuilder
         */
        public abstract DialogBuilder cancelable( boolean cancelable );

        /**
         * Builds the IDialog
         * @return an IDialog
         */
        public abstract IDialog build();
    }

}