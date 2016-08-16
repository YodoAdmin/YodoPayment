package co.yodo.mobile.helper;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;

import java.util.Locale;

import co.yodo.mobile.R;

/**
 * Created by hei on 10/06/16.
 * Utils used for the interface
 */
public class GUIUtils {
    /**
     * Show or hide the password depending on the checkbox
     * @param state The checkbox
     * @param password The EditText for the password
     */
    public static void showPassword( CheckBox state, EditText password ) {
        if( state.isChecked() )
            password.setInputType( InputType.TYPE_TEXT_VARIATION_PASSWORD );
        else
            password.setInputType( InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD );
        password.setTypeface( Typeface.MONOSPACE );
    }

    /**
     * Hides the soft keyboard
     * @param activity The activity where the keyboard is open
     */
    public static void hideSoftKeyboard( Activity activity ) {
        View view = activity.getCurrentFocus();
        if( view != null ) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService( Context.INPUT_METHOD_SERVICE );
            imm.hideSoftInputFromWindow( view.getWindowToken(), 0 );
        }
    }

    /**
     * Copies a String to the clipboard
     * @param c The Context of the Android system.
     * @param text The text to be copied
     */
    public static void copyCode( Context c, String text ) {
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) c.getSystemService( Context.CLIPBOARD_SERVICE );
        android.content.ClipData clip = android.content.ClipData.newPlainText( "Copied", text );
        clipboard.setPrimaryClip( clip );
    }

    /**
     * Sets the stored language for the application
     * @param ac The Context of the Android system.
     */
    public static void setLanguage( Context ac ) {
        final String language = PrefUtils.getLanguage( ac );
        if( language != null ) {
            Locale appLoc = new Locale( language );
            Locale.setDefault( appLoc );

            Resources res = ac.getResources();
            DisplayMetrics dm = res.getDisplayMetrics();

            Configuration config = new Configuration( res.getConfiguration() );
            config.locale = appLoc;

            res.updateConfiguration( config, dm );
        } else {
            final String appLang = ac.getResources().getConfiguration().locale.getLanguage();
            PrefUtils.saveLanguage( ac, appLang );
        }
    }

    /**
     * Gets a drawable from the bitmap
     * @param drawable The drawable
     * @return The bitmap from the drawable
     */
    public static Bitmap drawableToBitmap( Drawable drawable) {
        if( drawable instanceof BitmapDrawable ) {
            return ( ( BitmapDrawable) drawable ).getBitmap();
        }

        int width = drawable.getIntrinsicWidth();
        width = width > 0 ? width : 1;
        int height = drawable.getIntrinsicHeight();
        height = height > 0 ? height : 1;

        Bitmap bitmap = Bitmap.createBitmap( width, height, Bitmap.Config.ARGB_8888 );
        Canvas canvas = new Canvas( bitmap );
        drawable.setBounds( 0, 0, canvas.getWidth(), canvas.getHeight() );
        drawable.draw( canvas );

        return bitmap;
    }

    /**
     * Sets the action bar and title to the activity
     * @param act      The activity to be updated
     * @param title    The integer that represents the resource title
     * @return Toolbar The toolbar found for the activity
     */
    public static Toolbar setActionBar( AppCompatActivity act, int title ) {
        // Only used at creation
        Toolbar toolbar = (Toolbar) act.findViewById( R.id.actionBar );

        // Setup the toolbar
        act.setTitle( title );
        act.setSupportActionBar( toolbar );
        ActionBar actionBar = act.getSupportActionBar();
        if( actionBar != null )
            actionBar.setDisplayHomeAsUpEnabled( true );

        return toolbar;
    }
}
