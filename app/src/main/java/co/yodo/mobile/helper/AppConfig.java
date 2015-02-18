package co.yodo.mobile.helper;

import co.yodo.mobile.BuildConfig;

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

    /* Advertising status.
	 * type -- Boolean
	 *
	 * __Values__
	 * true  -- Advertising service enabled
	 * false -- Advertising service disabled
	 */
    public static final String SPREF_ADVERTISING_SERVICE = "SPAdvertisingService";

    /* The current language.
	 * type -- String
	 */
    public static final String SPREF_CURRENT_LANGUAGE = "SPCurrentLanguage";

    /**
     * Default values
     * {{ ======================================================================
     */

    /*
	 * Default value position for the language
	 *
	 * Default: position 0 (English)
	 */
    public static final String DEFAULT_LANGUAGE = "en";

    /*
	 * Default value of scan interval
	 *
	 * Default: 90 seconds
	 */
    public static final Integer DEFAULT_SCAN_INTERVAL = 1000 * 90;

    /* Biometric Default */
    public static final String YODO_BIOMETRIC = "BiometricTest";

    /* Bluetooth Yodo POS name */
    public static final String YODO_POS = "Yodo-Merch-";

    /* Coupons folder */
    public static final String COUPONS_FOLDER = "Yodo";

    /* Minimum length for the PIP */
    public static final int MIN_PIP_LENGTH = 4;

    /* Progress Dialog */
    public static final String IS_SHOWING = "is_showing";
}
