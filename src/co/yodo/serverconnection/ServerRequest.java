package co.yodo.serverconnection;

import android.util.Log;

public class ServerRequest {
	/*!< DEBUG */
	private final static boolean DEBUG = false;
	
	/*!< Protocol version used in the request */
	private static final String PROTOCOL_VERSION = "1.1.2";
	
	/*!< Parameters used for creating an authentication request */
	private static final String AUTH_REQ                 = "0";
	public static final String AUTH_HW_SUBREQ            = "1";
	public static final String AUTH_HW_PIP_SUBREQ        = "2";
	public static final String AUTH_HW_PIP_EIFACE_SUBREQ = "3";

    /*!< Parameters used for creating a get balance request */
    private static final String QUERY_REQ           = "4";
    public static final String QUERY_BAL_SUBREQ     = "1";
    public static final String QUERY_DATA_SUBREQ    = "3";

    /*!< Parameters used for creating a reset request */
    private static final String RESET_REQ         = "3";
    public static final String RESET_PIP_SUBREQ   = "1";
    public static final String BIO_RST_PIP_SUBREQ = "2";

    /*!< Parameters used for creating a registration request */
    private static final String REG_REQ         = "9";
    public static final String CLIENT_SUBREQ    = "0";
    public static final String BIOMETRIC_SUBREQ = "3";

    /*!< Parameters used for creating a close request */
    private static final String CLOSE_REQ          = "8";
    private static final String CLOSE_CLIENT_SUBREQ = "1";
	
	/*!< Variable that holds request string separator */
	private static final String	REQ_SEP = ",";

	/**
	 * Creates authentication switch request to verify the specified client’s account credentials 
	 * @param pUsrData	   Encrypted user's data
	 * @param iAuthReqType Sub-type of the request
	 * @return String	   Request for getting the authentication
	 */
	public static String createAuthenticationRequest(String pUsrData, int iAuthReqType){
		StringBuilder sAuthRequest = new StringBuilder();
		
		sAuthRequest.append(PROTOCOL_VERSION).append(REQ_SEP);
		sAuthRequest.append(AUTH_REQ).append(REQ_SEP);

		// depending on the request type we would form our request differently 
		switch(iAuthReqType){
			//RT = 0, ST = 1
			case 1: sAuthRequest.append(AUTH_HW_SUBREQ).append(REQ_SEP);
			break;

			//RT = 0, ST = 2
			case 2: sAuthRequest.append(AUTH_HW_PIP_SUBREQ).append(REQ_SEP);
			break;

			//RT = 0, ST = 3
			case 3: sAuthRequest.append(AUTH_HW_PIP_EIFACE_SUBREQ).append(REQ_SEP);
			break;
		}
		sAuthRequest.append(pUsrData);
		
		if(DEBUG)
			Log.e("Authentication Request", sAuthRequest.toString());
		return sAuthRequest.toString();
	}

    /**
     * Creates query switch request using user's information
     * @param pUsrData	Encrypted user's data
     * @return String	Request for getting user's account balance
     */
    public static String createQueryRequest(String pUsrData, int iQueryReqType){
        StringBuilder sQueryRequest = new StringBuilder();

        sQueryRequest.append(PROTOCOL_VERSION).append(REQ_SEP);
        sQueryRequest.append(QUERY_REQ).append(REQ_SEP);

        // depending on the request type we would form our request differently
        switch(iQueryReqType){
            //RT = 4, ST = 1
            case 1: sQueryRequest.append(QUERY_BAL_SUBREQ).append(REQ_SEP);
                break;

            //RT = 4, ST = 3 Receipt and Biometric
            case 3: sQueryRequest.append(QUERY_DATA_SUBREQ).append(REQ_SEP);
                break;
        }
        sQueryRequest.append(pUsrData);
        
        if(DEBUG)
        	Log.e("Query Request", sQueryRequest.toString());
        return sQueryRequest.toString();
    }

    /**
     * Creates reset switch request to change a user profile
     * @param sUserData	Encrypted user's data
     * @param iResReqType Sub-type of the request
     * @return String 	Request to register a user
     */
    public static String createResetRequest(String sUserData, int iResReqType) {
        StringBuilder sResetRequest = new StringBuilder();

        sResetRequest.append(PROTOCOL_VERSION).append(REQ_SEP);
        sResetRequest.append(RESET_REQ).append(REQ_SEP);

        /// depending on the request type we would form our request differently
        switch(iResReqType){
            /// RT = 3, ST = 1
            case 1:
                sResetRequest.append(RESET_PIP_SUBREQ).append(REQ_SEP);
                break;
            /// RT = 3, ST = 2
            case 2:
                sResetRequest.append(BIO_RST_PIP_SUBREQ).append(REQ_SEP);
                break;
        }
        sResetRequest.append(sUserData);
        
        if(DEBUG)
        	Log.e("Reset Request", sResetRequest.toString());
        return sResetRequest.toString();
    }

    /**
     * Creates registration switch request to register a user
     * @param sUserData	Encrypted user's data
     * @param iRegReqType Sub-type of the request
     * @return	String	Request to register a user
     */
    public static String createRegistrationRequest(String sUserData, int iRegReqType) {
        StringBuilder sRegRequest = new StringBuilder();

        sRegRequest.append(PROTOCOL_VERSION).append(REQ_SEP);
        sRegRequest.append(REG_REQ).append(REQ_SEP);

        /// depending on the request type we would form our request differently
        switch(iRegReqType){
            /// RT = 9, ST = 0
            case 0:
                sRegRequest.append(CLIENT_SUBREQ).append(REQ_SEP);
                break;

            /// RT = 9, ST = 3
            case 3:
                sRegRequest.append(BIOMETRIC_SUBREQ).append(REQ_SEP);
                break;
        }
        sRegRequest.append(sUserData);
        
        if(DEBUG)
        	Log.e("Registration Request", sRegRequest.toString());
        return sRegRequest.toString();
    }

    /**
     * Creates close switch request to close the account
     * @param sUserData	Encrypted user's data
     * @return String 	Request to close a user account
     */
    public static String createCloseRequest(String sUserData) {
        StringBuilder sCloseRequest = new StringBuilder();

        sCloseRequest.append(PROTOCOL_VERSION).append(REQ_SEP);
        sCloseRequest.append(CLOSE_REQ).append(REQ_SEP);

        /// RT = 8, ST = 1
        sCloseRequest.append(CLOSE_CLIENT_SUBREQ).append(REQ_SEP);
        sCloseRequest.append(sUserData);
        
        if(DEBUG)
        	Log.e("Close Request", sCloseRequest.toString());
        return sCloseRequest.toString();
    }
}
