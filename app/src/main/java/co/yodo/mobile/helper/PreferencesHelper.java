package co.yodo.mobile.helper;

import android.content.Context;
import android.content.SharedPreferences;

import com.orhanobut.hawk.Hawk;

import co.yodo.mobile.BuildConfig;
import co.yodo.mobile.YodoApplication;
import co.yodo.mobile.utils.AppConfig;

/**
 * Created by luis on 15/12/14.
 * Utilities for the App, Mainly shared userPreferences
 */
public class PreferencesHelper {
    /** ID of the shared userPreferences file */
    public static final String PREF_USER_FILE = "YodoPaymentSharedPref";
    public static final String PREF_HAWK_FILE = "Hawk2";

    /** Keys used with the Shared Preferences (SP) and default values */
    private static final String SPREF_UUID_TOKEN     = "SPREF_UUID_TOKEN"; // UUID for auth
    private static final String SPREF_PHONE_NUMBER   = "SPREF_PHONE_NUMBER"; // Phone number of the user
    private static final String SPREF_HARDWARE_TOKEN = "SPREF_HARDWARE_TOKEN"; // Hardware token
    private static final String SPREF_EULA           = "SPREF_EULA" + BuildConfig.VERSION_NAME; // Eula
    private static final String SPREF_FIRST_LOGIN    = "SPREF_FIRST_LOGIN"; // First login
    public static final String SPREF_BALANCE         = "SPREF_BALANCE"; // User balance
    private static final String SPREF_AUTH_NUMBER    = "SPREF_AUTH_NUMBER"; // Auth registration number
    private static final String SPREF_GCM_TOKEN      = "SPREF_GCM_TOKEN" + YodoApplication.getSwitch(); // GCM token
    private static final String SPREF_NICKNAME       = "SPREF_NICKNAME"; // Nickname for the links
    private static final String SPREF_TIPPING        = "SPREF_TIPPING"; // Tipping options
    public static final String SPREF_LANGUAGE        = "SPREF_LANGUAGE"; // Language
    public static final String SPREF_SUBSCRIPTION    = "SPREF_SUBSCRIPTION"; // Promotions

    /** Helper instance */
    private static PreferencesHelper instance;

    /** Preferences instance */
    private static SharedPreferences userPreferences;
    private static SharedPreferences hawkPreferences;

    /** Avoid instantiation */
    private PreferencesHelper(Context context) {
        Hawk.init(context).build();
        userPreferences = context.getSharedPreferences(PREF_USER_FILE, Context.MODE_PRIVATE);
        hawkPreferences = context.getSharedPreferences(PREF_HAWK_FILE, Context.MODE_PRIVATE);
    }

    /**
     * Initialize the preference helper
     * @param context The application context
     * @return The Preferences helper
     */
    public static PreferencesHelper init(Context context) {
        if (instance == null) {
            instance = new PreferencesHelper(context.getApplicationContext());
        }
        return instance;
    }

    /**
     * Register a listener for the userPreferences
     * @param listener The listener that will be registered to the userPreferences
     */
    public static void registerListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        userPreferences.registerOnSharedPreferenceChangeListener(listener);
        hawkPreferences.registerOnSharedPreferenceChangeListener(listener);
    }

    /**
     * Unregisters a listener to the userPreferences
     * @param listener The listener that will be unregistered to the userPreferences
     */
    public static void unregisterListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        userPreferences.unregisterOnSharedPreferenceChangeListener(listener);
        hawkPreferences.unregisterOnSharedPreferenceChangeListener(listener);
    }

    /**
     * Sets the uuid token for the account
     * @param uuidToken The uuid received from the server
     */
    public static void setUuidToken(String uuidToken) {
        Hawk.put(SPREF_UUID_TOKEN, uuidToken);
    }

    /**
     * Gets the uuid token for the server authentication
     * @return A String as the token
     */
    public static String getUuidToken() {
        return Hawk.get(SPREF_UUID_TOKEN);
        // Hardware token is for legacy authentication
        //String uuid = Hawk.get(SPREF_UUID_TOKEN);
        //return (uuid == null) ? (String) Hawk.get(SPREF_HARDWARE_TOKEN) : uuid;
    }

    /**
     * Sets the phone number for the account
     * @param phoneNumber The phone number validated from firebase
     */
    public static void setPhoneNumber(String phoneNumber) {
        Hawk.put(SPREF_PHONE_NUMBER, phoneNumber);
    }

    /**
     * Gets the user's phone number
     * @return A String as the phone number
     */
    public static String getPhoneNumber() {
        return Hawk.get(SPREF_PHONE_NUMBER);
    }

    /**
     * It saves if it is the user accepted the EULA.
     * @param flag Value if accepted or not the EULA
     * @return true  The flag was saved successfully.
     *         false The flag was not saved successfully.
     */
    public static Boolean saveEulaAccepted(Boolean flag) {
        return Hawk.put(SPREF_EULA, flag);
    }

    /**
     * It gets if the user accepted the EULA.
     * @return true  The user accepted the EULA.
     *         false The user didn't accept the EULA.
     */
    static Boolean isEulaAccepted() {
        return Hawk.get(SPREF_EULA, false);
    }

    /**
     * It saves the authnumber of the pip registration to the Shared Preferences.
     * @param authNumber The authNumber of the registration
     * @return true  The account was saved successfully.
     *         false The account was not saved successfully.
     */
    public static Boolean saveAuthNumber(String authNumber) {
        if (authNumber == null) {
            return Hawk.delete(SPREF_AUTH_NUMBER);
        }
        return Hawk.put(SPREF_AUTH_NUMBER, authNumber);
    }

    /**
     * It gets authnumber of the pip registration
     * @return String The authnumber of the pip registration
     *         null    If there is no value set;
     */
    public static String getAuthNumber() {
        return Hawk.get(SPREF_AUTH_NUMBER);
    }













    /**
     * Saves the hardware token in the userPreferences
     * @param hardwareToken The hardware token
     * @return If it was a success true otherwise false
     */
    public static Boolean saveHardwareToken(String hardwareToken) {
        return Hawk.put(SPREF_HARDWARE_TOKEN, hardwareToken);
    }

    /**
     * Gets the store hardware token if exists
     * @return The stored token if exists
     */
    public static String getHardwareToken() {
        return Hawk.get(SPREF_HARDWARE_TOKEN);
    }

    /**
     * It saves if the token was successfully sent to the server in the Shared Preferences.
     * @param sent If the token was sent or not
     * @return true  The boolean was saved successfully.
     *         false The boolean was not saved successfully.
     */
    public static Boolean saveGCMTokenSent( boolean sent ) {
        return Hawk.put(SPREF_GCM_TOKEN, sent );
    }

    /**
     * It gets if the token was sent to the server
     * @return boolean If the token was sent to the server
     *         null    If there is no value set;
     */
    public static boolean isGCMTokenSent() {
        return Hawk.get(SPREF_GCM_TOKEN, false );
    }

    /**
     * It saves the current user balance
     * @param balance It is the user balance
     * @return true  The flag was saved successfully.
     *         false The flag was not saved successfully.
     */
    public static boolean saveBalance( String balance ) {
        return Hawk.put(SPREF_BALANCE, balance);
    }

    /**
     * It gets the user balance
     * @return String It returns the balance
     */
    public static String getCurrentBalance() {
        return Hawk.get(SPREF_BALANCE, "*.**");
    }

    /**
     * Saves the nickname for a hardware token
     * @param hardware The hardware token
     * @param nickname The nickname
     * @return True if it saved correctly
     */
    public static boolean saveNickname( String hardware, String nickname ) {
        if( nickname == null ) {
            return Hawk.delete(SPREF_NICKNAME + hardware);
        }
        return Hawk.put(SPREF_NICKNAME + hardware, nickname );
    }

    /**
     * Gets the nickname of a hardware token
     * @param hardware The hardware token
     * @return The nickname -- if it doesn't have a nickname, then it returns
     * the last 5 digits of the hardware token
     */
    public static String getNickname( String hardware ) {
        return Hawk.get(SPREF_NICKNAME + hardware,
                "...." + hardware.substring( hardware.length() - 6 )
        );
    }

    /**
     * A helper class just o obtain the config file for the Shared Preferences
     * using the default values for this Shared Preferences app.
     * @param c The Context of the Android system.
     * @return Returns the shared userPreferences with the default values.
     */
    private static SharedPreferences getSPrefConfig( Context c ) {
        return c.getSharedPreferences(PREF_USER_FILE, Context.MODE_PRIVATE);
    }

    private static SharedPreferences getHawkSPrefConfig( Context c ) {
        return c.getSharedPreferences( "Hawk2", Context.MODE_PRIVATE );
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
        writer.putBoolean( SPREF_FIRST_LOGIN, flag );
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
        return config.getBoolean( SPREF_FIRST_LOGIN, true );
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
        writer.putString( SPREF_LANGUAGE, language );
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
        return config.getBoolean( SPREF_SUBSCRIPTION, false );
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
        writer.putBoolean( SPREF_SUBSCRIPTION, value );
        return writer.commit();
    }

    /**
     * Get the current task for the subscription
     * @param c The Context of the Android system
     * @return The task
     */
    public static Boolean isTipping( Context c ) {
        SharedPreferences config = getSPrefConfig( c );
        return config.getBoolean( SPREF_TIPPING, true );
    }

    /**
     * Clear all the userPreferences
     * @param context The application context
     * @return True if it was a success otherwise false
     */
    public static boolean clearPrefConfig( Context context ) {
        Hawk.deleteAll();
        return getSPrefConfig( context ).edit().clear().commit();
    }
}
