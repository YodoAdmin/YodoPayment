package co.yodo.mobile.helper;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;

import com.google.android.gms.nearby.messages.Strategy;

/**
 * Created by luis on 15/12/14.
 * Utilities for the App, Mainly shared preferences
 */
public class PrefUtils {
    @SuppressWarnings( "unused" )
    private static final String TAG = PrefUtils.class.getSimpleName();

    /**
     * A helper class just o obtain the config file for the Shared Preferences
     * using the default values for this Shared Preferences app.
     * @param c The Context of the Android system.
     * @return Returns the shared preferences with the default values.
     */
    private static SharedPreferences getSPrefConfig( Context c ) {
        return c.getSharedPreferences( AppConfig.SHARED_PREF_FILE, Context.MODE_PRIVATE );
    }

    /**
     * Register a listener for the preferences
     * @param c The Context of the Android system
     * @param listener The listener that will be registered to the preferences
     */
    public static void registerSPListener( Context c, SharedPreferences.OnSharedPreferenceChangeListener listener ) {
        getSPrefConfig( c ).registerOnSharedPreferenceChangeListener( listener );
    }

    /**
     * Unregisters a listener to the preferences
     * @param c The Context of the Android system
     * @param listener The listener that will be unregistered to the preferences
     */
    public static void unregisterSPListener( Context c, SharedPreferences.OnSharedPreferenceChangeListener listener ) {
        getSPrefConfig( c ).unregisterOnSharedPreferenceChangeListener( listener );
    }

    /**
     * Clear all the preferences
     * @param c The Context of the Android system
     * @return True if it was a success otherwise false
     */
    public static boolean clearPrefConfig( Context c ) {
        return getSPrefConfig( c ).edit().clear().commit();
    }

    /**
     * Generates the mobile hardware identifier either
     * from the Phone (IMEI) or the Bluetooth (MAC)
     * @param c The Context of the Android system.
     * @return A new hardware token
     */
    public static String generateHardwareToken( Context c ) {
        String HARDWARE_TOKEN = null;

        TelephonyManager telephonyManager  = (TelephonyManager) c.getSystemService( Context.TELEPHONY_SERVICE );
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Try to get the IMEI
        if( telephonyManager != null ) {
            String tempMAC = telephonyManager.getDeviceId();
            if( tempMAC != null )
                HARDWARE_TOKEN = tempMAC.replace( "/", "" );
        }

        // Try to get the Bluetooth identifier if this device doesn't have IMEI
        if( HARDWARE_TOKEN == null && mBluetoothAdapter != null ) {
            if( mBluetoothAdapter.isEnabled() ) {
                String tempMAC = mBluetoothAdapter.getAddress();
                HARDWARE_TOKEN = tempMAC.replaceAll( ":", "" );
            }
        }

        return HARDWARE_TOKEN;
    }

    /**
     * Saves the hardware token in the preferences
     * @param c The Context of the Android system
     * @param hardwareToken The hardware token
     * @return If it was a success true otherwise false
     */
    public static Boolean saveHardwareToken( Context c, String hardwareToken ) {
        SharedPreferences config = getSPrefConfig( c );
        SharedPreferences.Editor writer = config.edit();
        writer.putString( AppConfig.SPREF_HARDWARE_TOKEN, hardwareToken );
        return writer.commit();
    }

    /**
     * Gets the store hardware token if exists
     * @param c The Context of the Android system
     * @return The stored token if exists
     */
    public static String getHardwareToken( Context c ) {
        SharedPreferences config = getSPrefConfig( c );
        String token = config.getString( AppConfig.SPREF_HARDWARE_TOKEN, "" );
        return ( token.equals( "" ) ) ? null : token;
    }

    /**
     * It savesthe current language
     * @param c The Context of the Android system.
     * @param language It is the language used by the app
     * @return true  The flag was saved successfully.
     *         false The flag was not saved successfully.
     */
    public static boolean saveLanguage( Context c, String language ) {
        SharedPreferences config = getSPrefConfig( c );
        SharedPreferences.Editor writer = config.edit();
        writer.putString( AppConfig.SPREF_CURRENT_LANGUAGE, language );
        return writer.commit();
    }

    /**
     * It gets the language
     * @param c The Context of the Android system
     * @return String It returns the language
     */
    public static String getLanguage( Context c ) {
        SharedPreferences config = getSPrefConfig( c );
        return config.getString( AppConfig.SPREF_CURRENT_LANGUAGE, null );
    }

    /**
     * It saves if it is the first login.
     * @param c The Context of the Android system.
     * @param flag If it is the first login or not.
     * @return true  The flag was saved successfully.
     *         false The flag was not saved successfully.
     */
    public static Boolean saveFirstLogin( Context c, Boolean flag ) {
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
    public static Boolean isFirstLogin( Context c ) {
        SharedPreferences config = getSPrefConfig( c );
        return config.getBoolean( AppConfig.SPREF_FIRST_LOGIN, true );
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
        return config.getBoolean( AppConfig.SPREF_EULA_ACCEPTED, false );
    }

    /**
     * It saves the authnumber of the pip registration to the Shared Preferences.
     * @param c The Context of the Android system.
     * @param authnumber The authnumber of the registration
     * @return true  The account was saved successfully.
     *         false The account was not saved successfully.
     */
    public static Boolean saveAuthNumber( Context c, String authnumber ) {
        SharedPreferences config = getSPrefConfig( c );
        SharedPreferences.Editor writer = config.edit();

        writer.putString( AppConfig.SPREF_AUTH_NUMBER, authnumber );

        return writer.commit();
    }

    /**
     * It gets authnumber of the pip registration
     * @param c The Context of the Android system.
     * @return String The authnumber of the pip registration
     *         null    If there is no value set;
     */
    public static String getAuthNumber( Context c ) {
        SharedPreferences config = getSPrefConfig( c );
        return config.getString( AppConfig.SPREF_AUTH_NUMBER, "" );
    }

    /**
     * It saves if the token was successfully sent to the server in the Shared Preferences.
     * @param c The Context of the Android system.
     * @param sent If the token was sent or not
     * @return true  The boolean was saved successfully.
     *         false The boolean was not saved successfully.
     */
    public static Boolean saveGCMTokenSent( Context c, boolean sent ) {
        SharedPreferences config = getSPrefConfig( c );
        SharedPreferences.Editor writer = config.edit();
        writer.putBoolean( AppConfig.SPREF_TOKEN_TO_SERVER, sent );
        return writer.commit();
    }

    /**
     * It gets if the token was sent to the server
     * @param c The Context of the Android system.
     * @return boolean If the token was sent to the server
     *         null    If there is no value set;
     */
    public static boolean isGCMTokenSent( Context c ) {
        SharedPreferences config = getSPrefConfig( c );
        return config.getBoolean( AppConfig.SPREF_TOKEN_TO_SERVER, false );
    }

    /**
     * Get the current task for the subscription
     * @param c The Context of the Android system
     * @return The task
     */
    public static Boolean isSubscribing( Context c ) {
        SharedPreferences config = getSPrefConfig( c );
        return config.getBoolean( AppConfig.SPREF_SUBSCRIPTION_TASK, false );
    }

    /**
     * Sets the tasks for the subscription
     * @param c The Context of the Android system
     * @param value The task
     * @return true  The flag was saved successfully.
     *         false The flag was not saved successfully.
     */
    public static boolean setSubscribing( Context c, Boolean value ) {
        SharedPreferences config = getSPrefConfig( c );
        SharedPreferences.Editor writer = config.edit();
        writer.putBoolean( AppConfig.SPREF_SUBSCRIPTION_TASK, value );
        return writer.commit();
    }

    /**
     * It saves if the main activity is in the foreground to the Shared Preferences
     * @param c The Context of the Android system
     * @param foreground If the MainActivity is paused or resumed
     * @return true  The boolean was saved successfully
     *         false The boolean was not saved successfully
     */
    public static Boolean saveIsForeground( Context c, boolean foreground ) {
        SharedPreferences config = getSPrefConfig( c );
        SharedPreferences.Editor writer = config.edit();
        writer.putBoolean( AppConfig.SPREF_FOREGROUND, foreground );
        return writer.commit();
    }

    /**
     * It gets if the MainActivity is in the foreground
     * @param c The Context of the Android system
     * @return boolean If the token was sent to the server
     *         null    If there is no value set;
     */
    public static boolean isForeground( Context c ) {
        SharedPreferences config = getSPrefConfig( c );
        return config.getBoolean( AppConfig.SPREF_FOREGROUND, false );
    }

    /**
     * It gets if the token was sent to the server
     * @param c The Context of the Android system
     * @return Integer
     */
    public static Integer getPromotionsTime( Context c ) {
        SharedPreferences config = getSPrefConfig( c );
        String value = config.getString( AppConfig.SPREF_PROMOTION_TIME, AppConfig.DEFAULT_PROMOTION );

        if( value.equals( "999" ) ) // 999 represents infinite in the array
            return Strategy.TTL_SECONDS_INFINITE;
        return Integer.parseInt( value );
    }
}
