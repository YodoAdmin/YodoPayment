package co.yodo.mobile.helper;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import co.yodo.mobile.R;

/**
 * Created by luis on 16/12/14.
 * Helper to newInstance alert dialogs
 */
public class AlertDialogHelper {
    /**
     * Shows a dialog for alert messages with one button
     * @param ac The activity
     * @param message A message to show
     * @param onClick click for the positive button
     */
    public static void show( Activity ac, String title, String message, DialogInterface.OnClickListener onClick ) {
        AlertDialog.Builder builder = new AlertDialog.Builder( ac );
        if( title != null ) {
            builder.setTitle( title );
        }

        builder.setMessage( message );
        builder.setCancelable( false );
        builder.setPositiveButton( ac.getString( R.string.text_ok ), onClick );
        builder.show();
    }

    /**
     * Shows a dialog for alert messages with one button
     * @param ac The activity
     * @param message A message to show
     * @param onClick click for the positive button
     */
    public static void show( Activity ac, String message, DialogInterface.OnClickListener onClick ) {
        show( ac, null, message, onClick );
    }

    /**
     * Shows an alert dialog with an EditText with two buttons (permission)
     * @param ac The activity
     * @param message A message to show
     * @param onClick Action for the selection
     */
    public static void show( Activity ac, Integer title, Integer message, View layout,
                             DialogInterface.OnClickListener onClick ) {
        AlertDialog.Builder builder = new AlertDialog.Builder( ac );

        if( title != null )
            builder.setTitle( title );

        if( message != null )
            builder.setMessage( message );

        if( layout != null )
            builder.setView( layout );

        builder.setCancelable( false );
        builder.setPositiveButton( R.string.text_ok, onClick );

        if( onClick != null ) {
            builder.setNegativeButton( R.string.text_cancel, null );
        }

        builder.show();
    }

    /**
     * Shows an alert dialog with two buttons
     * @param c The activity
     * @param message A message to show
     * @param okClick Action for the ok click
     */
    public static void show( Activity ac, Integer message, DialogInterface.OnClickListener okClick ) {
        show( ac, null, message, null, okClick );
    }

    /**
     * Shows an alert dialog for a list
     * @param c The activity
     * @param title The title of the dialog
     * @param values Values to be shown
     */
    public static void show( Activity ac, Integer title, CharSequence[] values,
                             DialogInterface.OnClickListener itemClick) {
        AlertDialog.Builder builder = new AlertDialog.Builder( ac );

        builder.setTitle( title );
        builder.setCancelable( false );

        builder.setItems( values, itemClick );
        builder.setNegativeButton( R.string.text_cancel, null );

        builder.show();
    }

    /**
     * Creates an alert dialog with a predefined layout, and a click function
     * @param ac The activity
     * @param message The message of the dialog
     * @param layout The layout to be displayed
     * @param okClick The click action
     * @return The AlertDialog object
     */
    public static AlertDialog create( Activity ac, Integer title, Integer message, View layout,
                                    DialogInterface.OnShowListener okClick ) {
        AlertDialog.Builder builder = new AlertDialog.Builder( ac );

        if( title != null )
            builder.setTitle( title );

        if( message != null )
            builder.setMessage( message );

        if( layout != null )
            builder.setView( layout );

        builder.setCancelable( false );
        builder.setPositiveButton( R.string.text_ok, null );

        if( okClick != null )
            builder.setNegativeButton( R.string.text_cancel, null );

        final AlertDialog dialog = builder.create();
        final Window window = dialog.getWindow();

        if( okClick != null && window != null ) {
            dialog.setOnShowListener( okClick );
            window.setSoftInputMode( WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE );
        }

        return dialog;
    }

    /**
     * Creates an alert dialog with a predefined layout, and a click function
     * @param ac The activity
     * @param layout The layout to be displayed
     * @param okClick The click action
     * @return The AlertDialog object
     */
    public static AlertDialog create( Activity ac, View layout, DialogInterface.OnShowListener okClick ) {
        return create( ac, null, layout, okClick );
    }

    /**
     * Creates an alert dialog with a predefined layout, and a click function
     * @param ac The activity
     * @param message The message to display
     * @param layout The layout to be displayed
     * @param okClick The click action
     * @return The AlertDialog object
     */
    public static AlertDialog create( Activity ac, Integer message, View layout, DialogInterface.OnShowListener okClick ) {
        return create( ac, null, message, layout, okClick );
    }

    /**
     * Creates an alert dialog with a predefined layout, and a click function
     * @param ac The activity
     * @param message The message to display
     * @param layout The layout to be displayed
     * @return The AlertDialog object
     */
    public static AlertDialog create( Activity ac, Integer message, View layout ) {
        return create( ac, null, message, layout, null );
    }
}
