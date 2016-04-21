package co.yodo.mobile.helper;

import android.support.v7.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;

import co.yodo.mobile.R;

/**
 * Created by luis on 16/12/14.
 * Helper to create alert dialogs
 */
public class AlertDialogHelper {

    /**
     * Shows an alert dialog for a list
     * @param c The context of the application
     * @param title The title of the dialog
     * @param values Values to be shown
     */
    public static void showAlertDialog(final Context c, final String title,
                                       final CharSequence[] values,
                                       final DialogInterface.OnClickListener clickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder( c, R.style.AppCompatAlertDialogStyle );
        builder.setIcon( R.drawable.ic_launcher );
        builder.setTitle( title );
        builder.setCancelable( false );

        builder.setItems( values, clickListener );
        builder.setNegativeButton( c.getString( R.string.cancel ), null );

        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Shows an alert dialog with an EditText
     * @param c The context of the application
     * @param message The message of the dialog
     * @param clickListener Action for the selection
     */
    public static void showAlertDialog(final Context c, final int message,
                                       final DialogInterface.OnClickListener clickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder( c, R.style.AppCompatAlertDialogStyle );
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
    public static void showAlertDialog(final Context c, final String title, final String message,
                                       final DialogInterface.OnClickListener clickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder( c, R.style.AppCompatAlertDialogStyle );
        builder.setIcon( R.drawable.ic_launcher );
        builder.setTitle( title );
        builder.setMessage( message );
        builder.setCancelable( false );

        builder.setPositiveButton( c.getString( R.string.ok ), clickListener );

        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Shows an alert dialog with an EditText
     * @param c The context of the application
     * @param title The title of the dialog
     * @param view The view of the dialog
     */
    public static void showAlertDialog(final Context c, final String title, final View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder( c, R.style.AppCompatAlertDialogStyle );
        builder.setIcon( R.drawable.ic_launcher );
        builder.setTitle( title );
        builder.setView( view );
        builder.setCancelable( false );

        builder.setPositiveButton( c.getString( R.string.ok ), null );

        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Shows an alert dialog with an EditText
     * @param c The context of the application
     * @param title The title of the dialog
     * @param input The edit text for the password
     * @param clickListener Action attached to the dialog
     */
    public static void showAlertDialog(final Context c, final String title,
                                       final String message, final String showText,
                                       final EditText input,
                                       final DialogInterface.OnClickListener clickListener) {
        input.setInputType( InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD );
        input.setOnFocusChangeListener( new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                input.post( new Runnable() {
                    @Override
                    public void run() {
                        InputMethodManager inputMethodManager = (InputMethodManager) c.getSystemService( Context.INPUT_METHOD_SERVICE );
                        inputMethodManager.showSoftInput( input, InputMethodManager.SHOW_IMPLICIT );
                    }
                });
            }
        });
        input.requestFocus();

        LayoutInflater inflater = (LayoutInflater) c.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        View layout = inflater.inflate( R.layout.dialog_with_password, new LinearLayout( c ), false );
        ((LinearLayout) layout).addView( input, 0 );

        CheckBox showPassword = (CheckBox) layout.findViewById( R.id.showPassword );
        showPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(((CheckBox)v).isChecked())
                    input.setInputType( InputType.TYPE_TEXT_VARIATION_PASSWORD );
                else
                    input.setInputType( InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD );
            }
        });

        if( showText != null )
            showPassword.setText( showText );

        AlertDialog.Builder builder = new AlertDialog.Builder( c, R.style.AppCompatAlertDialogStyle );
        builder.setIcon( R.drawable.ic_launcher );
        builder.setTitle( title );
        builder.setMessage( message );
        builder.setView( layout );
        builder.setCancelable( false );

        builder.setPositiveButton( c.getString( R.string.ok ), clickListener );
        builder.setNegativeButton( c.getString( R.string.cancel ), null );

        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Shows an alert dialog with an EditText
     * @param c The context of the application
     * @param title The title of the dialog
     * @param input The edit text for the password
     * @param clickListener Action attached to the dialog
     */
    public static void showAlertDialog(final Context c, final String title,
                                       final String message, final EditText input,
                                       final DialogInterface.OnClickListener clickListener,
                                       final DialogInterface.OnClickListener dismissListener) {
        input.setInputType( InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD );
        input.setOnFocusChangeListener( new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                input.post( new Runnable() {
                    @Override
                    public void run() {
                        InputMethodManager inputMethodManager = (InputMethodManager) c.getSystemService( Context.INPUT_METHOD_SERVICE );
                        inputMethodManager.showSoftInput( input, InputMethodManager.SHOW_IMPLICIT );
                    }
                });
            }
        });
        input.requestFocus();

        LayoutInflater inflater = (LayoutInflater) c.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        View layout = inflater.inflate( R.layout.dialog_with_password, new LinearLayout( c ), false );
        ((LinearLayout) layout).addView( input, 0 );

        CheckBox showPassword = (CheckBox) layout.findViewById( R.id.showPassword );
        showPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(((CheckBox)v).isChecked())
                    input.setInputType( InputType.TYPE_TEXT_VARIATION_PASSWORD );
                else
                    input.setInputType( InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD );
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder( c, R.style.AppCompatAlertDialogStyle );
        builder.setIcon( R.drawable.ic_launcher );
        builder.setTitle( title );
        builder.setMessage( message );
        builder.setView( layout );
        builder.setCancelable( false );

        builder.setPositiveButton( c.getString( R.string.ok ), clickListener );
        builder.setNegativeButton( c.getString( R.string.cancel ), dismissListener );

        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public static AlertDialog showAlertDialog(final Context c, View layout, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder( c, R.style.AppCompatAlertDialogStyle );
        builder.setView( layout );
        builder.setMessage( message );

        builder.setCancelable( true );

        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
        return alertDialog;
    }
}
