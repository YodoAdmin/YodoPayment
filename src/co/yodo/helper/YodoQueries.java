package co.yodo.helper;

import android.app.Activity;
import android.util.Log;
import co.yodo.sks.Encrypter;

public class YodoQueries {
	// DEBUG
	private static final boolean DEBUG = true;
	private static final String TAG    = YodoQueries.class.getName();
	
	/*!< Object used to encrypt user's information */
    private static Encrypter oEncrypter;
    
    /*!< Separators */
    private static final String	USR_SEP = "**";
    private static final String	REQ_SEP = ",";
    
	/**
     * Gets object used to encrypt user's information
     * @return	Encrypter
     */
    private static Encrypter getEncrypter(){
        if(oEncrypter == null)
            oEncrypter = new Encrypter();
        return oEncrypter;
    }
    
    //Authentication Strings
    public static String requestHardwareAuthorization(Activity activity, String hrdwToken) {
        String sEncryptedUsrData;
        
        // Encrypting user's  to create request
        getEncrypter().setsUnEncryptedString(hrdwToken);
        getEncrypter().rsaEncrypt(activity);
        sEncryptedUsrData = getEncrypter().bytesToHex();

        return sEncryptedUsrData;
    }
    
    public static String requestPIPHardwareAuthentication(Activity activity, String hrdwToken, String pip) {
        String sEncryptedUsrData, sFormattedUsrData;
        sFormattedUsrData = Utils.formatUsrData(hrdwToken, pip);

        // Encrypting user's data to create request
        getEncrypter().setsUnEncryptedString(sFormattedUsrData);
        getEncrypter().rsaEncrypt(activity);
        sEncryptedUsrData = getEncrypter().bytesToHex();

        return sEncryptedUsrData;
    }
    
    // Balance String
    public static String requestBalance(Activity activity, String hrdwToken, String pip) {
        String sEncryptedUsrData, sFormattedUsrData ;
        sFormattedUsrData = Utils.formatUsrData(hrdwToken, pip);

        // Encrypting user's data to create request
        getEncrypter().setsUnEncryptedString(sFormattedUsrData);
        getEncrypter().rsaEncrypt(activity);
        sEncryptedUsrData = getEncrypter().bytesToHex();

        return sEncryptedUsrData;
    }
    
    // Receipt String
    public static String requestReceipt(Activity activity, String hrdwToken, String pip) {
        StringBuilder userData = new StringBuilder();
        String sEncryptedUsrData;

        userData.append(hrdwToken).append(REQ_SEP);
        userData.append(pip).append(REQ_SEP);
        userData.append(YodoGlobals.RECORD_LOCATOR);

        /// Encrypting user's data to create request
        getEncrypter().setsUnEncryptedString(userData.toString());
        getEncrypter().rsaEncrypt(activity);
        sEncryptedUsrData = getEncrypter().bytesToHex();

        return sEncryptedUsrData;
    }
    
    // Close Account String
    public static String requestCloseAccount(Activity activity, String hrdwToken, String pip) {
        String sEncryptedUsrData;
        StringBuilder sUsrData = new StringBuilder();

        long time = System.currentTimeMillis();
        String timeStamp = String.valueOf(time);

        sUsrData.append(pip).append(USR_SEP);
        sUsrData.append(hrdwToken).append(USR_SEP);
        sUsrData.append(timeStamp).append(REQ_SEP);
        sUsrData.append("0").append(REQ_SEP);
        sUsrData.append("0");

        // Encrypting user's data to create request
        getEncrypter().setsUnEncryptedString(sUsrData.toString());
        getEncrypter().rsaEncrypt(activity);
        sEncryptedUsrData = getEncrypter().bytesToHex();

        return sEncryptedUsrData;
    }
    
    // Advertising String
    public static String requestAdvertising(Activity activity, String hrdwToken, String merch) {
		String sEncryptedUsrData;
		StringBuilder sAdvertisingData = new StringBuilder();
		
		sAdvertisingData.append(hrdwToken).append(REQ_SEP);
		sAdvertisingData.append(merch).append(REQ_SEP);
		sAdvertisingData.append(YodoGlobals.QUERY_ADS);
		
		// Encrypting user's data to create request
		getEncrypter().setsUnEncryptedString(sAdvertisingData.toString());
		getEncrypter().rsaEncrypt(activity);
		sEncryptedUsrData = getEncrypter().bytesToHex();
		
		return sEncryptedUsrData;
	}
    
    // Biometric Request String with PIP
   	public static String requestBiometricTokenPIP(Activity activity, String hrdwToken, String pip) {
   		String sEncryptedUsrData;
		StringBuilder sBiometricData = new StringBuilder();
		
		sBiometricData.append(hrdwToken).append(REQ_SEP);
		sBiometricData.append(pip).append(REQ_SEP);
		sBiometricData.append(YodoGlobals.QUERY_BIO_PIP);
		
		// Encrypting user's data to create request
		getEncrypter().setsUnEncryptedString(sBiometricData.toString());
		getEncrypter().rsaEncrypt(activity);
		sEncryptedUsrData = getEncrypter().bytesToHex();
		
		return sEncryptedUsrData;
   	}
   	
   	// Biometric Request String without PIP
   	public static String requestBiometricToken(Activity activity, String hrdwToken) {
   		String sEncryptedUsrData;
		StringBuilder sBiometricData = new StringBuilder();
		
		sBiometricData.append(hrdwToken).append(REQ_SEP);
		sBiometricData.append(YodoGlobals.QUERY_BIO);
		
		// Encrypting user's data to create request
		getEncrypter().setsUnEncryptedString(sBiometricData.toString());
		getEncrypter().rsaEncrypt(activity);
		sEncryptedUsrData = getEncrypter().bytesToHex();
		
		return sEncryptedUsrData;
   	}
   	
   	// Reset PIP String
    public static String requestPIPReset(Activity activity, String hrdwToken, String pip, String newPip) {
    	String sEncryptedUsrData, sEncryptedOldPip, sEncryptedNewPip;
		StringBuilder sPipResetData = new StringBuilder();
		
		getEncrypter().setsUnEncryptedString(hrdwToken);
        getEncrypter().rsaEncrypt(activity);
        sEncryptedUsrData = getEncrypter().bytesToHex();
        
        getEncrypter().setsUnEncryptedString(pip);
        getEncrypter().rsaEncrypt(activity);
        sEncryptedOldPip = getEncrypter().bytesToHex();

        getEncrypter().setsUnEncryptedString(newPip);
        getEncrypter().rsaEncrypt(activity);
        sEncryptedNewPip = getEncrypter().bytesToHex();
		
		sPipResetData.append(sEncryptedUsrData).append(REQ_SEP);
		sPipResetData.append(sEncryptedOldPip).append(REQ_SEP);
		sPipResetData.append(sEncryptedNewPip);

		return sPipResetData.toString();
    }
    
    // Reset PIP String
    public static String requestPIPResetBio(Activity activity, String authNumber, String hrdwToken, String newPip) {
    	String sEncryptedUsrData;
		StringBuilder sPipResetData = new StringBuilder();
		
		sPipResetData.append(authNumber).append(REQ_SEP);
		sPipResetData.append(hrdwToken).append(REQ_SEP);
		sPipResetData.append(newPip);
		
		if(DEBUG)
			Log.i(TAG, sPipResetData.toString());
		
		getEncrypter().setsUnEncryptedString(sPipResetData.toString());
		getEncrypter().rsaEncrypt(activity);
		sEncryptedUsrData = getEncrypter().bytesToHex();

		return sEncryptedUsrData;
    }
    
    // Registration PIP String
    public static String requestRegistration(Activity activity, String hrdwToken, String pip, String timeStamp) {
    	String sEncryptedUsrData;
		StringBuilder sRegistrationData = new StringBuilder();
		
		sRegistrationData.append(YodoGlobals.USER_BIOMETRIC).append(USR_SEP);
		sRegistrationData.append(pip).append(USR_SEP);
		sRegistrationData.append(hrdwToken).append(USR_SEP);
		sRegistrationData.append(timeStamp);
		
		getEncrypter().setsUnEncryptedString(sRegistrationData.toString());
		getEncrypter().rsaEncrypt(activity);
		sEncryptedUsrData = getEncrypter().bytesToHex();
		
		return sEncryptedUsrData;
    }
    
    // Registration Biometric String
    public static String requestBiometricRegistration(Activity activity, String authNumber, String biometricToken) {
		StringBuilder sRegistrationData = new StringBuilder();
		
		sRegistrationData.append(authNumber).append(REQ_SEP);
		sRegistrationData.append(biometricToken);
		
		return sRegistrationData.toString();
    }
}
