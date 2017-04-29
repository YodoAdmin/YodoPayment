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
    protected final Dialog dialog;

    /**
     * Constructor that shows the dialog
     * based in the DialogBuilder
     * @param builder The DialogBuilder
     */
    protected IDialog( DialogBuilder builder ) {
        this.dialog = builder.dialog;
        this.dialog.setCancelable( builder.cancelable );
        this.dialog.setCanceledOnTouchOutside( builder.cancelable );
        this.dialog.show();
    }

    /**
     * Show the inner dialog
     */
    public void show() {
        this.dialog.show();
    }

    /**
     * Dismiss the inner dialog
     */
    public void dismiss() {
        this.dialog.dismiss();
    }

    /**
     * Abstract class for the Dialog Builders
     */
    protected static abstract class DialogBuilder {
        /** Context object */
        protected final Context context;

        /** Dialog to be build */
        protected final Dialog dialog;

        /** Optional parameters */
        protected boolean cancelable = false;

        /**
         * Builder constructor with the mandatory elements
         * @param context The application context
         * @param layout The layout for the dialog
         */
        protected DialogBuilder( Context context, int layout ) {
            this.context = context;
            dialog = new Dialog( this.context );
            dialog.requestWindowFeature( Window.FEATURE_NO_TITLE );
            dialog.setContentView( layout );
        }

        /**
         * Builder constructor with the mandatory elements and a style
         * @param context The application context
         * @param layout The layout for the dialog
         */
        protected DialogBuilder( Context context, int layout, int style ) {
            this.context = context;
            dialog = new Dialog( this.context, style );
            dialog.requestWindowFeature( Window.FEATURE_NO_TITLE );
            dialog.setContentView( layout );
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