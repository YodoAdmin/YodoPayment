package co.yodo.mobile.helper;

import android.app.Activity;
import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import co.yodo.mobile.component.YodoHandler;

/**
 * Created by luis on 15/12/14.
 * Utilities for the App, Mainly shared preferences
 */
public class AppUtils {
    /** Accounts Separato */
    public static final String ACC_SEP = ",";

    /**
     * A helper class just o obtain the config file for the Shared Preferences
     * using the default values for this Shared Preferences app.
     * @param c The Context of the Android system.
     * @return Returns the shared preferences with the default values.
     */
    private static SharedPreferences getSPrefConfig(Context c) {
        return c.getSharedPreferences( AppConfig.SHARED_PREF_FILE, Context.MODE_PRIVATE );
    }

    public static boolean clearPrefConfig(Context c) {
        return getSPrefConfig( c ).edit().clear().commit();
    }

    /**
     * It gets the language.
     * @param c The Context of the Android system.
     * @return String It returns the language.
     */
    public static String getLanguage(Context c) {
        SharedPreferences config = getSPrefConfig( c );
        return config.getString(AppConfig.SPREF_CURRENT_LANGUAGE, AppConfig.DEFAULT_LANGUAGE);
    }

    /**
     * It saves if it is the first login.
     * @param c The Context of the Android system.
     * @param flag If it is the first login or not.
     * @return true  The flag was saved successfully.
     *         false The flag was not saved successfully.
     */
    public static Boolean saveFirstLogin(Context c, Boolean flag) {
        SharedPreferences config = getSPrefConfig( c );
        SharedPreferences.Editor writer = config.edit();
        writer.putBoolean( AppConfig.SPREF_FIRST_LOGIN, flag );
        return writer.commit();
    }

    /**
     * It gets if it is the first login.
     * @param c The Context of the Android system.
     * @return true  It is logged in.
     *         false It is not logged in.
     */
    public static Boolean isFirstLogin(Context c) {
        SharedPreferences config = getSPrefConfig( c );
        return config.getBoolean(AppConfig.SPREF_FIRST_LOGIN, true);
    }

    /**
     * It saves if it is the user accepted the EULA.
     * @param c The Context of the Android system.
     * @param flag Value if accepted or not the EULA
     * @return true  The flag was saved successfully.
     *         false The flag was not saved successfully.
     */
    public static Boolean saveEulaAccepted(Context c, Boolean flag) {
        SharedPreferences config = getSPrefConfig( c );
        SharedPreferences.Editor writer = config.edit();
        writer.putBoolean( AppConfig.SPREF_EULA_ACCEPTED, flag );
        return writer.commit();
    }

    /**
     * It gets if the user accepted the EULA.
     * @param c The Context of the Android system.
     * @return true  The user accepted the EULA.
     *         false The user didn't accept the EULA.
     */
    public static Boolean isEulaAccepted(Context c) {
        SharedPreferences config = getSPrefConfig( c );
        return config.getBoolean(AppConfig.SPREF_EULA_ACCEPTED, false);
    }

    /**
     * It saves the advertising service state.
     * @param c The Context of the Android system.
     * @param flag If it is advertising or not.
     * @return true  The flag was saved successfully.
     *         false The flag was not saved successfully.
     */
    public static Boolean saveAdvertising(Context c, Boolean flag) {
        SharedPreferences config = getSPrefConfig( c );
        SharedPreferences.Editor writer = config.edit();
        writer.putBoolean( AppConfig.SPREF_ADVERTISING_SERVICE, flag );
        return writer.commit();
    }

    /**
     * It gets if the advertising service is active.
     * @param c The Context of the Android system.
     * @return true  The advertising service is executing
     *         false The advertising service is off.
     */
    public static Boolean isAdvertising(Context c) {
        SharedPreferences config = getSPrefConfig( c );
        return config.getBoolean( AppConfig.SPREF_ADVERTISING_SERVICE, false );
    }

    /**
     * It gets the linked accounts
     *
     * @param c The Context of the Android system.
     * @return String The linked account numbers
     *         null    If there is no value set;
     */
    public static String getLinkedAccount(Context c) {
        SharedPreferences config = getSPrefConfig( c );
        return config.getString( AppConfig.SPREF_LINKED_ACCOUNTS, "" );
    }

    /**
     * It saves the linked accounts to the Shared Preferences.
     *
     * @param c The Context of the Android system.
     * @param n The new account
     * @return true  The account was saved successfully.
     *         false The account was not saved successfully.
     */
    public static Boolean saveLinkedAccount(Context c, String n) {
        SharedPreferences config = getSPrefConfig( c );
        SharedPreferences.Editor writer = config.edit();

        String accounts = getLinkedAccount( c ) + n + ACC_SEP;
        writer.putString( AppConfig.SPREF_LINKED_ACCOUNTS, accounts );

        return writer.commit();
    }

    /**
     * It clears the linked accounts to the Shared Preferences.
     *
     * @param c The Context of the Android system.
     * @return true  The account was saved successfully.
     *         false The account was not saved successfully.
     */
    public static Boolean clearLinkedAccount(Context c) {
        SharedPreferences config = getSPrefConfig( c );
        SharedPreferences.Editor writer = config.edit();

        writer.putString( AppConfig.SPREF_LINKED_ACCOUNTS, "" );

        return writer.commit();
    }

    /**
     * Gets the mobile hardware identifier
     * @param c The Context of the Android system.
     */
    public static String getHardwareToken(Context c) {
        String HARDWARE_TOKEN = null;

        TelephonyManager telephonyManager  = (TelephonyManager) c.getSystemService( Context.TELEPHONY_SERVICE );
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //WifiManager wifiManager            = (WifiManager) c.getSystemService( Context.WIFI_SERVICE );

        if( telephonyManager != null ) {
            HARDWARE_TOKEN = telephonyManager.getDeviceId();
        }

        if( HARDWARE_TOKEN == null && mBluetoothAdapter != null ) {
            if( mBluetoothAdapter.isEnabled() ) {
                String tempMAC = mBluetoothAdapter.getAddress();
                HARDWARE_TOKEN = tempMAC.replaceAll( ":", "" );
            }
        }

		/*if( HARDWARE_TOKEN == null && wifiManager != null ) {
			if( wifiManager.isWifiEnabled() ) {
				WifiInfo wifiInf = wifiManager.getConnectionInfo();
				String tempMAC = wifiInf.getMacAddress();
				HARDWARE_TOKEN = tempMAC.replaceAll( ":", "" );
			}
		}*/

        return HARDWARE_TOKEN;
    }

    /**
     * Show or hide the password depending on the checkbox
     * @param state The checkbox
     * @param password The EditText for the password
     */
    public static void showPassword(CheckBox state, EditText password) {
        if( state.isChecked() )
            password.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        else
            password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        password.setTypeface( Typeface.MONOSPACE );
    }

    /**
     * Hides the soft keyboard
     * @param a The activity where the keyboard is open
     */
    public static void hideSoftKeyboard(Activity a) {
        View v = a.getCurrentFocus();
        if( v != null ) {
            InputMethodManager imm = (InputMethodManager) a.getSystemService( Context.INPUT_METHOD_SERVICE );
            imm.hideSoftInputFromWindow( v.getWindowToken(), 0 );
        }
    }

    public static void setLanguage(Context c) {
        Locale appLoc = new Locale( getLanguage( c ) );

        Resources res = c.getResources();
        DisplayMetrics dm = res.getDisplayMetrics();

        Locale.setDefault( appLoc );
        Configuration config = new Configuration( res.getConfiguration() );
        config.locale = appLoc;

        res.updateConfiguration( config, dm );
    }

    /**
     * Verify if a service is running
     * @param c The Context of the Android system.
     * @param serviceName The name of the service.
     * @return Boolean true if is running otherwise false
     */
    public static boolean isMyServiceRunning(Context c, String serviceName) {
        ActivityManager manager = (ActivityManager) c.getSystemService( Context.ACTIVITY_SERVICE );
        for( ActivityManager.RunningServiceInfo service : manager.getRunningServices( Integer.MAX_VALUE ) )  {
            if( serviceName.equals( service.service.getClassName() ) )
                return true;
        }
        return false;
    }

    /**
     * Transforms bytes to String
     * @param data bytes
     * @return String
     */
    public static String bytesToHex(byte[] data) {
        if( data == null )
            return null;

        String str = "";
        for( byte aData : data ) {
            if( ( aData & 0xFF ) < 16 )
                str = str + "0" + Integer.toHexString( aData & 0xFF );
            else
                str = str + Integer.toHexString( aData & 0xFF );
        }
        return str;
    }

    /**
     * Transforms a String to bytes
     * @param str The String
     * @return bytes
     */
    public static byte[] hexToBytes(String str) {
        if( str == null ) {
            return null;
        } else if( str.length() < 2 ) {
            return null;
        } else {
            int len = str.length() / 2;
            byte[] buffer = new byte[ len ];
            for( int i = 0; i < len; i++ ) {
                buffer[i] = (byte) Integer.parseInt( str.substring( i * 2, i * 2 + 2 ), 16 );
            }
            return buffer;
        }
    }

    /**
     * Gets the actual date
     * @return	String actual date
     */
    public static String getCurrentDate() {
        final Calendar c = Calendar.getInstance();
        int year  = c.get( Calendar.YEAR );
        int month = c.get( Calendar.MONTH ) + 1;
        int day   = c.get( Calendar.DAY_OF_MONTH );
        String sMonth = ( month < 10 ) ? "0" + month : "" + month;
        String sDay   = ( day   < 10 ) ? "0" + day   : "" + day;
        return year + "/"+ ( sMonth ) + "/" + sDay;
    }

    /**
     * Sends a message to the handler
     * @param handlerMessages The Handler for the app
     * @param title The title for the alert
     * @param message The message for the alert
     */
    public static void sendMessage(YodoHandler handlerMessages, String title, String message) {
        Message msg = new Message();
        msg.what = YodoHandler.SERVER_ERROR;

        Bundle bundle = new Bundle();
        bundle.putString( YodoHandler.CODE, title );
        bundle.putString( YodoHandler.MESSAGE, message );
        msg.setData( bundle );

        handlerMessages.sendMessage( msg );
    }

    /**
     * Check if the device possess bluetooth
     * @return true if it possess bluetooth otherwise false
     */
    public static boolean hasBluetooth() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return mBluetoothAdapter != null;
    }

    /**
     * Gets the SKS size
     * @param context The Context of the Android system.
     * @return int The size
     */
    public static int getSKSSize(Activity context) {
        int screenLayout = context.getResources().getConfiguration().screenLayout;
        screenLayout &= Configuration.SCREENLAYOUT_SIZE_MASK;

        Rect displayRectangle = new Rect();
        Window window = context.getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);
        int size, currentOrientation = context.getResources().getConfiguration().orientation;

        if(currentOrientation == Configuration.ORIENTATION_LANDSCAPE)
            size = displayRectangle.height();
        else
            size = displayRectangle.width();

        switch(screenLayout) {
            case Configuration.SCREENLAYOUT_SIZE_SMALL:
                return (int)(size * 0.7f);

            case Configuration.SCREENLAYOUT_SIZE_NORMAL:
                return (int)(size * 0.7f);

            case Configuration.SCREENLAYOUT_SIZE_LARGE:
                return (int)(size * 0.4f);

            case Configuration.SCREENLAYOUT_SIZE_XLARGE:
                return (int)(size * 0.3f);

            default:
                return 300;
        }
    }

    /**
     * Gets a drawable from the bitmap
     * @param drawable The drawable
     * @return The bitmap from the drawable
     */
    public static Bitmap drawableToBitmap (Drawable drawable) {
        if( drawable instanceof BitmapDrawable ) {
            return ( ( BitmapDrawable) drawable ).getBitmap();
        }

        int width = drawable.getIntrinsicWidth();
        width = width > 0 ? width : 1;
        int height = drawable.getIntrinsicHeight();
        height = height > 0 ? height : 1;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas( bitmap );
        drawable.setBounds( 0, 0, canvas.getWidth(), canvas.getHeight() );
        drawable.draw( canvas );

        return bitmap;
    }

    /**
     * Transforms a UTC date to the cellphone date
     * @param date The date in UTC
     * @return the Date in the cellphone time
     */
    public static String UTCtoCurrent(String date) {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss", Locale.US );

        try {
            TimeZone z = c.getTimeZone();
            int offset = z.getRawOffset();
            if( z.inDaylightTime( new Date() ) )
                offset = offset + z.getDSTSavings();
            int offsetHrs = offset / 1000 / 60 / 60;

            c.setTime( sdf.parse( date ) );
            c.add( Calendar.HOUR_OF_DAY, offsetHrs );
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return sdf.format( c.getTime() );
    }

    /**
     * Copies a String to the clipboard
     * @param c The Context of the Android system.
     * @param text The text to be copied
     */
    public static void copyCode(Context c, String text) {
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) c.getSystemService( Context.CLIPBOARD_SERVICE );
        android.content.ClipData clip = android.content.ClipData.newPlainText( "Copied", text );
        clipboard.setPrimaryClip( clip );
    }

    /**
     * Truncates n number of positions (decimal) from a number
     * @param number The number to be truncated
     * @param positions the n positions
     * @return The truncated number as String
     */
    public static String truncateDouble(Double number, int positions) {
        int factor = (int) Math.pow( 10, positions );
        long temp = (long)( number * factor );
        return String.format( Locale.US, "%.2f", ( (double)temp / factor ) );
    }

    /**
     * Logger for Android
     * @param TAG The String of the TAG for the log
     * @param text The text to print on the log
     */
    public static void Logger(String TAG, String text) {
        if( AppConfig.DEBUG ) {
            if( text == null )
                Log.e( TAG, "Null Text" );
            else
                Log.e( TAG, text );
        }
    }
}
