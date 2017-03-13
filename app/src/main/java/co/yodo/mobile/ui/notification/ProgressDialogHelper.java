package co.yodo.mobile.ui.notification;

import android.app.Activity;
import android.app.ProgressDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import co.yodo.mobile.R;

/**
 * Created by hei on 16/05/16.
 * Handles a progress dialog
 */
public class ProgressDialogHelper {
    /** Progress dialog */
    private ProgressDialog progressDialog = null;

    /**
     * Creates a new progress dialog on a respective activity
     * @param activity The activity that will show the dialog
     * @param message A custom message to show in progress bar
     */
    public void create( Activity activity, String message ) {
        if( progressDialog != null ) {
            throw new ExceptionInInitializerError( "There is already a progress dialog in front" );
        }

        // Generate the view
        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate( R.layout.dialog_progress, new LinearLayout( activity ), false );
        TextView loadingTextView = (TextView) view.findViewById( R.id.text_loading );

        // Create the dialog
        progressDialog = new ProgressDialog( activity, R.style.TransparentProgressDialog );
        progressDialog.setCancelable( false );
        progressDialog.show();

        if( message != null ) {
            loadingTextView.setText( message );
        }

        progressDialog.setContentView( view );
    }

    /**
     * Creates a new progress dialog on a respective activity
     * @param activity The activity that will show the dialog
     */
    public void create( Activity activity ) {
        create( activity, null );
    }

    /**
     * Creates a new progress dialog on a respective activity
     * @param activity The activity that will show the dialog
     * @param message A custom message to show in progress bar
     */
    public void create( Activity activity, int message ) {
        create( activity, activity.getString( message ) );
    }

    /**
     * Verifies if the dialog is being showed
     * @return A boolean that shows if the progress dialog is showing
     */
    public boolean isShowing() {
        return progressDialog != null && progressDialog.isShowing();
    }

    /**
     * Destroys the current progress dialog
     */
    public void destroy() {
        if( isShowing() ) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }
}
