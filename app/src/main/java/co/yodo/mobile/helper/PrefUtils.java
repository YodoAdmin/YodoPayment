package co.yodo.mobile.helper;

import android.content.Context;
import android.content.SharedPreferences;

import com.orhanobut.hawk.Hawk;

/**
 * Created by luis on 15/12/14.
 * Utilities for the App, Mainly shared preferences
 */
public class PrefUtils {
    /**
     * Saves the hardware token in the preferences
     * @param hardwareToken The hardware token
     * @return If it was a success true otherwise false
     */
    public static Boolean saveHardwareToken( String hardwareToken ) {
        return Hawk.put( AppConfig.SPREF_HARDWARE_TOKEN, hardwareToken );
    }

    /**
     * Gets the store hardware token if exists
     * @return The stored token if exists
     */
    public static String getHardwareToken() {
        return Hawk.get( AppConfig.SPREF_HARDWARE_TOKEN );
    }

    /**
     * It saves if it is the user accepted the EULA.
     * @param flag Value if accepted or not the EULA
     * @return true  The flag was saved successfully.
     *         false The flag was not saved successfully.
     */
    public static Boolean saveEulaAccepted( Boolean flag ) {
        return Hawk.put( AppConfig.SPREF_EULA, flag );
    }

    /**
     * It gets if the user accepted the EULA.
     * @return true  The user accepted the EULA.
     *         false The user didn't accept the EULA.
     */
    static Boolean isEulaAccepted() {
        return Hawk.get( AppConfig.SPREF_EULA, false );
    }

    /**
     * It saves the authnumber of the pip registration to the Shared Preferences.
     * @param authNumber The authNumber of the registration
     * @return true  The account was saved successfully.
     *         false The account was not saved successfully.
     */
    public static Boolean saveAuthNumber( String authNumber ) {
        if( authNumber == null ) {
            return Hawk.delete( AppConfig.SPREF_AUTH_NUMBER );
        }
        return Hawk.put( AppConfig.SPREF_AUTH_NUMBER, authNumber );
    }

    /**
     * It gets authnumber of the pip registration
     * @return String The authnumber of the pip registration
     *         null    If there is no value set;
     */
    public static String getAuthNumber() {
        return Hawk.get( AppConfig.SPREF_AUTH_NUMBER );
    }

    /**
     * It saves if the token was successfully sent to the server in the Shared Preferences.
     * @param sent If the token was sent or not
     * @return true  The boolean was saved successfully.
     *         false The boolean was not saved successfully.
     */
    public static Boolean saveGCMTokenSent( boolean sent ) {
        return Hawk.put( AppConfig.SPREF_TOKEN_TO_SERVER, sent );
    }

    /**
     * It gets if the token was sent to the server
     * @return boolean If the token was sent to the server
     *         null    If there is no value set;
     */
    public static boolean isGCMTokenSent() {
        return Hawk.get( AppConfig.SPREF_TOKEN_TO_SERVER, false );
    }

    /**
     * It saves the current user balance
     * @param balance It is the user balance
     * @return true  The flag was saved successfully.
     *         false The flag was not saved successfully.
     */
    public static boolean saveBalance( String balance ) {
        return Hawk.put( AppConfig.SPREF_BALANCE, balance );
    }

    /**
     * It gets the user balance
     * @return String It returns the balance
     */
    public static String getCurrentBalance() {
        return Hawk.get( AppConfig.SPREF_BALANCE, "*.**" );
    }

    /**
     * Saves the nickname for a hardware token
     * @param hardware The hardware token
     * @param nickname The nickname
     * @return True if it saved correctly
     */
    public static boolean saveNickname( String hardware, String nickname ) {
        if( nickname == null ) {
            return Hawk.delete( AppConfig.SPREF_NICKNAME + hardware );
        }
        return Hawk.put( AppConfig.SPREF_NICKNAME + hardware, nickname );
    }

    /**
     * Gets the nickname of a hardware token
     * @param hardware The hardware token
     * @return The nickname -- if it doesn't have a nickname, then it returns
     * the last 5 digits of the hardware token
     */
    public static String getNickname( String hardware ) {
        return Hawk.get(
                AppConfig.SPREF_NICKNAME + hardware,
                "...." + hardware.substring( hardware.length() - 6 )
        );
    }

    /**
     * A helper class just o obtain the config file for the Shared Preferences
     * using the default values for this Shared Preferences app.
     * @param c The Context of the Android system.
     * @return Returns the shared preferences with the default values.
     */
    private static SharedPreferences getSPrefConfig( Context c ) {
        return c.getSharedPreferences( AppConfig.SHARED_PREF_FILE, Context.MODE_PRIVATE );
    }

    private static SharedPreferences getHawkSPrefConfig( Context c ) {
        return c.getSharedPreferences( "Hawk2", Context.MODE_PRIVATE );
    }

    /**
     * Register a listener for the preferences
     * @param c The Context of the Android system
     * @param listener The listener that will be registered to the preferences
     */
    public static void registerSPListener( Context c, SharedPreferences.OnSharedPreferenceChangeListener listener ) {
        getSPrefConfig( c ).registerOnSharedPreferenceChangeListener( listener );
        getHawkSPrefConfig( c ).registerOnSharedPreferenceChangeListener( listener );
    }

    /**
     * Unregisters a listener to the preferences
     * @param c The Context of the Android system
     * @param listener The listener that will be unregistered to the preferences
     */
    public static void unregisterSPListener( Context c, SharedPreferences.OnSharedPreferenceChangeListener listener ) {
        getSPrefConfig( c ).unregisterOnSharedPreferenceChangeListener( listener );
        getHawkSPrefConfig( c ).unregisterOnSharedPreferenceChangeListener( listener );
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
     * It saves the current language
     * @param c The Context of the Android system.
     * @param language It is the language used by the app
     * @return true  The flag was saved successfully.
     *         false The flag was not saved successfully.
     */
    public static boolean saveLanguage( Context c, String language ) {
        SharedPreferences config = getSPrefConfig( c );
        SharedPreferences.Editor writer = config.edit();
        writer.putString( AppConfig.SPREF_LANGUAGE, language );
        return writer.commit();
    }

    /**
     * It gets the language
     * @param c The Context of the Android system
     * @return String It returns the language
     */
    public static String getLanguage( Context c ) {
        SharedPreferences config = getSPrefConfig( c );
        return config.getString( AppConfig.SPREF_LANGUAGE, null );
    }

    /**
     * Get the current task for the subscription
     * @param c The Context of the Android system
     * @return The task
     */
    public static Boolean isSubscribing( Context c ) {
        SharedPreferences config = getSPrefConfig( c );
        return config.getBoolean( AppConfig.SPREF_SUBSCRIPTION, false );
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
        writer.putBoolean( AppConfig.SPREF_SUBSCRIPTION, value );
        return writer.commit();
    }

    /**
     * Get the current task for the subscription
     * @param c The Context of the Android system
     * @return The task
     */
    public static Boolean isTipping( Context c ) {
        SharedPreferences config = getSPrefConfig( c );
        return config.getBoolean( AppConfig.SPREF_TIPPING, true );
    }

    /**
     * Clear all the preferences
     * @param context The application context
     * @return True if it was a success otherwise false
     */
    public static boolean clearPrefConfig( Context context ) {
        Hawk.deleteAll();
        return getSPrefConfig( context ).edit().clear().commit();
    }
}
