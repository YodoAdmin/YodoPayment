package co.yodo.mobile.ui.notification;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;

import co.yodo.mobile.R;
import co.yodo.mobile.helper.GUIUtils;

/**
 * Created by luis on 16/12/14.
 * Helper to create alert dialogs
 */
public class AlertDialogHelper {
    /**
     * Shows an alert dialog for a list (linking options)
     * @param c The context of the application
     * @param title The title of the dialog
     * @param values Values to be shown
     */
    public static void showAlertDialog( final Context c, final String title,
                                        final CharSequence[] values,
                                        final DialogInterface.OnClickListener clickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder( c, R.style.AppCompatAlertDialogStyle );
        builder.setIcon( R.drawable.ic_launcher );
        builder.setTitle( title );
        builder.setCancelable( false );

        builder.setItems( values, clickListener );
        builder.setNegativeButton( c.getString( R.string.cancel ), null );

        builder.show();
    }

    /**
     * Shows an alert dialog with an EditText with two buttons (permission)
     * @param c The context of the application
     * @param message The message of the dialog
     * @param clickListener Action for the selection
     */
    public static void showAlertDialog( final Context c, final int message,
                                        final DialogInterface.OnClickListener clickListener ) {
        AlertDialog.Builder builder = new AlertDialog.Builder( c, R.style.AppCompatAlertDialogStyle );
        builder.setIcon( R.drawable.ic_launcher );
        builder.setMessage( message );
        builder.setCancelable( false );
        builder.setPositiveButton( c.getString(R.string.ok ), clickListener );
        builder.setNegativeButton( c.getString( R.string.cancel ), null );

        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Shows an alert dialog with an EditText
     * @param c The context of the application
     * @param title The title of the dialog
     * @param message A message to show
     * @param clickListener click for the negative button
     */
    public static void showAlertDialog( final Context c, final String title,
                                        final String message, final View layout,
                                        final DialogInterface.OnClickListener clickListener ) {
        // Builds the Alert Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder( c, R.style.AppCompatAlertDialogStyle );
        builder.setIcon( R.drawable.ic_launcher );
        builder.setTitle( title );
        builder.setMessage( message );
        builder.setCancelable( false );
        builder.setPositiveButton( c.getString( R.string.ok ), clickListener );

        // Sets a layout if exists
        if( layout != null )
            builder.setView( layout );

        // Create the AlertDialog
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Shows an alert dialog with an EditText
     * @param c The context of the application
     * @param title The title of the dialog
     * @param message A message to show
     * @param clickListener click for the negative button
     */
    public static void showAlertDialog( final Context c, final String title, final String message,
                                        final DialogInterface.OnClickListener clickListener ) {
        showAlertDialog( c, title, message, null, clickListener );
    }

    /**
     * Shows an alert dialog with an EditText
     * @param c The context of the application
     * @param title The title of the dialog
     * @param view The view of the dialog
     */
    public static void showAlertDialog( final Context c, final String title, final View view ) {
        showAlertDialog( c, title, null, view, null );
    }

    /**
     * Builds an AlertDialog from a view (select correct method payment)
     * @param c The application context
     * @param layout The view/layout of the AlertDialog
     * @param message The AlertDialog Message
     * @return The AlertDialog
     */
    public static AlertDialog showAlertDialog( final Context c, final View layout, final String message ) {
        AlertDialog.Builder builder = new AlertDialog.Builder( c, R.style.AppCompatAlertDialogStyle );
        builder.setView( layout );
        builder.setMessage( message );

        builder.setCancelable( true );

        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
        return alertDialog;
    }

    /**
     * Shows an alert dialog with an EditText
     * @param c The context of the application
     * @param title The title of the dialog
     * @param message The message of the dialog
     * @param cbText The message of the CheckBox to show the input
     * @param input The edit text for the password
     * @param clickListener Action attached to the dialog
     */
    public static void showAlertDialog( final Context c,
                                        final String title, final String message,
                                        final String cbText, final EditText input,
                                        final DialogInterface.OnShowListener clickListener ) {
        // Remove the parent if already has one
        if( input.getParent() != null )
            ( (ViewGroup ) input.getParent() ).removeView( input );

        // Find the layout dialog_with password, and add the input
        LayoutInflater inflater = (LayoutInflater) c.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        View layout = inflater.inflate( R.layout.dialog_with_password, new LinearLayout( c ), false );
        ((LinearLayout) layout).addView( input, 0 );

        // Changes the input type to password
        input.setInputType( InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD );

        // Sets the CheckBox function to show the input text
        final CheckBox showPassword = (CheckBox) layout.findViewById( R.id.showPassword );
        showPassword.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                GUIUtils.showPassword( showPassword, input );
            }
        });

        // Change the checkbox text (show the input)
        if( cbText != null )
            showPassword.setText( cbText );

        // Build the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder( c, R.style.AppCompatAlertDialogStyle );
        builder.setIcon( R.drawable.ic_launcher );
        builder.setTitle( title );
        builder.setMessage( message );
        builder.setView( layout );
        builder.setCancelable( false );

        builder.setPositiveButton( c.getString( R.string.ok ), null );
        builder.setNegativeButton( c.getString( R.string.cancel ), null );

        // Create the Alert Dialog
        final AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener( clickListener );
        alertDialog.show();

        // Show the soft keyboard and focus the input
        input.requestFocus();
        input.post( new Runnable() {
            @Override
            public void run() {
                InputMethodManager inputMethodManager = (InputMethodManager) c.getSystemService( Context.INPUT_METHOD_SERVICE );
                inputMethodManager.showSoftInput( input, InputMethodManager.SHOW_IMPLICIT );
            }
        } );
    }

    /**
     * Shows an alert dialog with an EditText
     * @param c The context of the application
     * @param title The title of the dialog
     * @param input The edit text for the password
     * @param clickListener Action attached to the dialog
     */
    public static void showAlertDialog( Context c,
                                        String title, EditText input,
                                        DialogInterface.OnShowListener clickListener ) {
        showAlertDialog( c, title, null, null, input, clickListener );
    }

    /**
     * Shows an alert dialog with an EditText
     * @param c The context of the application
     * @param title The title of the dialog
     * @param message The message of the dialog
     * @param input The edit text for the password
     * @param clickListener Action attached to the dialog
     */
    public static void showAlertDialog( Context c,
                                        String title, String message, EditText input,
                                        DialogInterface.OnShowListener clickListener ) {
        showAlertDialog( c, title, message, null, input, clickListener );
    }

    /**
     * Shows an alert dialog with an EditText
     * @param c The context of the application
     * @param title The title of the dialog
     * @param cbText The id of the message of the CheckBox
     * @param input The edit text for the password
     * @param clickListener Action attached to the dialog
     */
    public static void showAlertDialog( Context c,
                                        String title, int cbText, EditText input,
                                        DialogInterface.OnShowListener clickListener ) {
        showAlertDialog( c, title, null, c.getString( cbText ), input, clickListener );
    }
}
