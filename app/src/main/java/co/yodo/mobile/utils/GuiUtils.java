package co.yodo.mobile.utils;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;

import java.util.Locale;

import co.yodo.mobile.R;
import co.yodo.mobile.helper.PrefUtils;

/**
 * Created by hei on 10/06/16.
 * Utils used for the interface
 */
public class GuiUtils {
    /**
     * Copies a String to the clipboard
     * @param c The Context of the Android system.
     * @param text The text to be copied
     */
    public static void copyCode( Context c, String text ) {
        ClipboardManager clipboard = (ClipboardManager) c.getSystemService( Context.CLIPBOARD_SERVICE );
        ClipData clip = ClipData.newPlainText( c.getString( R.string.text_link_account_copied ), text );
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
     */
    public static void setActionBar( AppCompatActivity act ) {
        ActionBar actionBar = act.getSupportActionBar();
        if( actionBar != null )
            actionBar.setDisplayHomeAsUpEnabled( true );
    }

    /**
     * Gets an activity from a view if it exists
     * @param v A view with a parent activity
     * @return The activity that holds the view
     */
    public static Activity getActivity( View v ) {
        Context context = v.getContext();
        while( context instanceof ContextWrapper ) {
            if( context instanceof Activity ) {
                return (Activity )context;
            }
            context = ( (ContextWrapper) context ).getBaseContext();
        }
        return null;
    }
}
