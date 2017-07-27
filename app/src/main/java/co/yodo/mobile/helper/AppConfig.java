package co.yodo.mobile.helper;

import co.yodo.mobile.BuildConfig;
import co.yodo.mobile.YodoApplication;

/**
 * Created by luis on 15/12/14.
 * Keys and defaults
 */
public class AppConfig {
    /** DEBUG flag */
    public static final boolean DEBUG = true;

    /** ID of the shared preferences file */
    public static final String SHARED_PREF_FILE = "YodoPaymentSharedPref";

    /**
     * Keys used with the Shared Preferences (SP) and default values.
     * {{ ======================================================================
     */

    /* Hardware token for the account
     * type -- String
     */
    static final String SPREF_HARDWARE_TOKEN = "SPREF_HARDWARE_TOKEN";

    /* EULA Accepted.
	 * type -- Boolean
	 *
	 * __Values__
	 * true  -- The user accepted the EULA
	 * false -- The user didn't accept the EULA
	 */
    static final String SPREF_EULA = "SPREF_EULA" + BuildConfig.VERSION_NAME;

    /* First Login status.
	 * type -- Boolean
	 *
	 * __Values__
	 * true  -- First time that the user is logged in
	 * false -- It was already logged in several times
	 */
    static final String SPREF_FIRST_LOGIN = "SPREF_FIRST_LOGIN";

    /* The current user balance.
     * type -- String
     */
    public static final String SPREF_BALANCE = "SPREF_BALANCE";

    /* Registration authnumber
	 * type -- String
	 */
    static final String SPREF_AUTH_NUMBER = "SPREF_AUTH_NUMBER";

    /* If the token was successfully sent to the server
	 * type -- boolean
	 */
    static final String SPREF_TOKEN_TO_SERVER = "SPREF_TOKEN_TO_SERVER" + YodoApplication.getSwitch();

    /* Nickname for the links
     * type -- String
     */
    static final String SPREF_NICKNAME = "SPREF_NICKNAME";

    /* If the tip option should appear at payment time
     * type -- String
     */
    static final String SPREF_TIPPING = "SPREF_TIPPING";

    /* The current language.
     * type -- String
     */
    public static final String SPREF_LANGUAGE = "SPREF_LANGUAGE";

    /* Action to be taken
     * type -- String
     */
    public static final String SPREF_SUBSCRIPTION = "SPREF_SUBSCRIPTION";

    /**
     * Configuration
     * {{ ======================================================================
     */

    /* Biometric Default */
    public static final String YODO_BIOMETRIC = "BiometricTest";

    /* Coupons folder */
    public static final String COUPONS_FOLDER = "Yodo";
}
