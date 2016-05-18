package co.yodo.mobile.ui.component;

import android.app.ProgressDialog;
import android.content.Context;

import co.yodo.mobile.R;

/**
 * Created by hei on 16/05/16.
 * Handles a progress dialog
 */
public class ProgressDialogHelper {
    /** Singleton instance */
    private static ProgressDialogHelper instance = null;

    /** Progress dialog */
    private ProgressDialog progressDialog;

    /**
     * Private constructor needed for the singleton
     */
    private ProgressDialogHelper() {
    }

    /**
     * The initializer for the singleton
     * @return The instance
     */
    public static synchronized ProgressDialogHelper getInstance() {
        if( instance == null )
            instance = new ProgressDialogHelper();
        return instance;
    }

    /**
     * Creates a new progress dialog on a respective activity
     * @param context This context must be an activity (e.g. MainActivity.this)
     */
    public void createProgressDialog( Context context ) {
        if( progressDialog != null )
            throw new ExceptionInInitializerError( "There is already a progress dialog in front" );

        progressDialog = new ProgressDialog( context, R.style.TransparentProgressDialog );
        progressDialog.setCancelable( false );
        progressDialog.show();
        progressDialog.setContentView( R.layout.custom_progressdialog );
    }

    /**
     * Verifies if the dialog is being showed
     * @return A boolean that shows if the progress dialog is showing
     */
    public boolean isProgressDialogShowing() {
        return progressDialog != null && progressDialog.isShowing();
    }

    /**
     * Destroys the current progress dialog
     */
    public void destroyProgressDialog() {
        if( progressDialog != null && progressDialog.isShowing() ) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }
}
