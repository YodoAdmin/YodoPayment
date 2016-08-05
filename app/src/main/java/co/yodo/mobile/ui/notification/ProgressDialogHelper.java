package co.yodo.mobile.ui.notification;

import android.app.ProgressDialog;
import android.content.Context;

import co.yodo.mobile.R;

/**
 * Created by hei on 16/05/16.
 * Handles a progress dialog
 */
public class ProgressDialogHelper {
    /** Progress dialog */
    private ProgressDialog mProgressDialog = null;

    /**
     * Creates a new progress dialog on a respective activity
     * @param context This context must be an activity (e.g. MainActivity.this)
     */
    public void createProgressDialog( Context context ) {
        if( mProgressDialog != null )
            throw new ExceptionInInitializerError( "There is already a progress dialog in front" );

        mProgressDialog = new ProgressDialog( context, R.style.TransparentProgressDialog );
        mProgressDialog.setCancelable( false );
        mProgressDialog.show();
        mProgressDialog.setContentView( R.layout.custom_progressdialog );
    }

    /**
     * Verifies if the dialog is being showed
     * @return A boolean that shows if the progress dialog is showing
     */
    public boolean isProgressDialogShowing() {
        return mProgressDialog != null && mProgressDialog.isShowing();
    }

    /**
     * Destroys the current progress dialog
     */
    public void destroyProgressDialog() {
        if( mProgressDialog != null && mProgressDialog.isShowing() ) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }
}
