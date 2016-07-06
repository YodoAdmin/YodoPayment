package co.yodo.mobile.helper;

import co.yodo.mobile.BuildConfig;

/**
 * Created by luis on 15/12/14.
 * Keys and defaults
 */
public class AppConfig {
    /** DEBUG flag */
    public static final boolean DEBUG = false;

    /** ID of the shared preferences file */
    public static final String SHARED_PREF_FILE = "YodoPaymentSharedPref";

    /**
     * Keys used with the Shared Preferences (SP) and default values.
     * {{ ======================================================================
     */

    /* Hardware token for the account
     * type -- String
     */
    public static final String SPREF_HARDWARE_TOKEN = "SPHardwareToken";

    /* EULA Accepted.
	 * type -- Boolean
	 *
	 * __Values__
	 * true  -- The user accepted the EULA
	 * false -- The user didn't accept the EULA
	 */
    public static final String SPREF_EULA_ACCEPTED = "SPEulaAccepted" + BuildConfig.VERSION_NAME;

    /* First Login status.
	 * type -- Boolean
	 *
	 * __Values__
	 * true  -- First time that the user is logged in
	 * false -- It was already logged in several times
	 */
    public static final String SPREF_FIRST_LOGIN = "SPFirstLogin";

    /* The current language.
	 * type -- String
	 */
    public static final String SPREF_CURRENT_LANGUAGE = "SPCurrentLanguage";

    /* Registration authnumber
	 * type -- String
	 */
    public static final String SPREF_AUTH_NUMBER = "SPAuthNumber";

    /* If the token was successfully sent to the server
	 * type -- boolean
	 */
    public static final String SPREF_TOKEN_TO_SERVER  = "SPTokenToServer";

    /* Action to be taken
     * type -- String
     */
    public static final String SPREF_SUBSCRIPTION_TASK = "subscription_task";

    /* If the main activity is in foreground
	 * type -- boolean
	 */
    public static final String SPREF_FOREGROUND = "SPForeground";

    /* The strategy time for nearby (promotions)
	 * type -- Integer
	 */
    public static final String SPREF_PROMOTION_TIME = "SPPromotionTime";

    /**
     * Default values
     * {{ ======================================================================
     */

    /*
	 * Default value for the language
	 *
	 * Default: en (English)
	 */
    public static final String DEFAULT_LANGUAGE = "en";

    /*
	 * Default value for the promotions
	 *
	 * Default: 300 (5 minutes)
	 */
    public static final String DEFAULT_PROMOTION = "300";

    /* Biometric Default */
    public static final String YODO_BIOMETRIC = "BiometricTest";

    /* Coupons folder */
    public static final String COUPONS_FOLDER = "Yodo";

    /* Minimum length for the PIP */
    public static final int MIN_PIP_LENGTH = 4;

    /* Progress Dialog */
    public static final String IS_SHOWING = "is_showing";
}
