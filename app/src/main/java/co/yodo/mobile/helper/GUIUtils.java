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
import android.text.InputType;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;

import java.util.Locale;

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
     * Gets the SKS size for the screen
     * @param activity The Context of the Android system (as activity)
     * @return int The size
     */
    public static int getSKSSize( Activity activity ) {
        int screenLayout = activity.getResources().getConfiguration().screenLayout;
        screenLayout &= Configuration.SCREENLAYOUT_SIZE_MASK;

        Rect displayRectangle = new Rect();
        Window window = activity.getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame( displayRectangle );
        int size, currentOrientation = activity.getResources().getConfiguration().orientation;

        if( currentOrientation == Configuration.ORIENTATION_LANDSCAPE )
            size = displayRectangle.height();
        else
            size = displayRectangle.width();

        switch( screenLayout ) {
            case Configuration.SCREENLAYOUT_SIZE_SMALL:
                return (int)( size * 0.7f );

            case Configuration.SCREENLAYOUT_SIZE_NORMAL:
                return (int)( size * 0.7f );

            case Configuration.SCREENLAYOUT_SIZE_LARGE:
                return (int)( size * 0.4f );

            case Configuration.SCREENLAYOUT_SIZE_XLARGE:
                return (int)( size * 0.3f );

            default:
                return 300;
        }
    }
}
