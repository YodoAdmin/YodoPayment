package co.yodo.helper;

public class YodoGlobals {
	/*!< DEBUG */
	public static final boolean DEBUG = false;
	
    /*!< Dummy */
    public static final String USER_BIOMETRIC = "BiometricTest";

    /*!< Preferences */
    public static final String PREFERENCES      = "user_preferences";
    public static final String ID_LANGUAGE      = "value_language";
    public static final String ID_ADVERTISING   = "value_advertising";
    public static final String ID_AUTHORIZATION = "value_authorization";
    public static final String ID_FIRST_USE     = "value_first_use";
    public static final int DEFAULT_LANGUAGE    = -1;
    public static final boolean DEFAULT_ADS     = true;
    public static final boolean DEFAULT_USE     = true;
    
    /*!< Eula Preferences */
    public static final String PREFERENCES_EULA         = "eula";
    public static final String PREFERENCE_EULA_ACCEPTED = "eula.accepted";
    
    /*!< Languages */
    public static final CharSequence[] languages = {"English", "Spanish", "Chinese", "Japanese", "French"};
    public static final CharSequence[] lang_code = {"en", "es", "zh", "ja", "fr"};
    
    /*!< Network Time Server */
    public static final String[] TIME_SERVERS = {"time.nist.gov", "time-a.nist.gov", "time-nw.nist.gov", "time-a.timefreq.bldrdoc.gov"};
    public static final int TIMEOUT_SERVER    = 10000;

	/*!< ID for messages */
	public static final String AUTHORIZED		       = "AU00";
	public static final String AUTHORIZED_REGISTRATION = "AU01";
	public static final String AUTHORIZED_BALANCE      = "AU55";
	public static final String AUTHORIZED_ALTERNATE    = "AU69";
	public static final String AUTHORIZED_TRANSFER     = "AU88";
	public static final String LOGIC_TEST              = "AU89";
	
	/*!< ID for error response */
    public static final String ERROR_INTERNET      = "ERIN";
	public static final String ERROR_FAILED        = "ER00";
	public static final String ERROR_MAX_LIM       = "ER13";
	public static final String ERROR_INSUFF_FUNDS  = "ER25";
	public static final String ERROR_INCORRECT_PIP = "ER22";
	public static final String ERROR_INCORRECT_GPS = "ER28";

    /*!< Id for Messages */
    public static final int NO_INTERNET   = 0;
    public static final int GENERAL_ERROR = 1;
    public static final int UNKOWN_ERROR  = 2;
    public static final int SUCCESS       = 3;

    /*!< Minimun lenght of the PIP */
    public static final int MIN_PIP_LENGTH = 4;

    /*!< Biometric Activity */
    public static final String FACE_DATA = "value_face";
    public static final String ID_TOKEN  = "value_token";
    public static final String ID_ALLOW  = "value_allow";

    /*!< Bluetooth Yodo POS */
    public static final String NO_ADS   = "Authorized";
    
    /*!< ACTION */
	public static final String DEVICES_BT  = "Devices_BT";
	public static final String DATA_DEVICE = "Device";

    /*!< Database */
    public static final String DB_NAME = "DBReceipts";
    
    /*!< Permitted Extensions */
    public static final String VIDEO_EXT[] = {".mp4", ".3gp"};
    public static final String IMG_EXT[]   = {".jpg", ".jpeg", "png", "gif"};
    
    /*!< Saved Instance State */
    public static final String DIALOG = "dialog";
    
    /*!< Fragment Information */
    public static final String TAG_TASK_FRAGMENT = "task_fragment";
    public static final String KEY_IS_SHOWING    = "dialog_is_showing";
    public static final String KEY_SKS_SHOWING   = "sks_is_showing";
    public static final String KEY_MESSAGE       = "dialog_message";
    
    // ID of the shared preferences file.
 	public static final String SHARED_PREF_FILE = "LinkingSharedPref";
 	
 	// Accounts Separato
 	public static final String ACC_SEP = ",";
 	
 	/* The Set of linked accounts
	 * type -- String
	 */
	public static final String SPREF_LINKED_ACC = "SPLinkedAccounts";
}
