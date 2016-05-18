package co.yodo.mobile.network.builder;

import co.yodo.mobile.helper.AppUtils;

public class ServerRequest {
	/** DEBUG */
	private final static String TAG = ServerRequest.class.getSimpleName();
	
	/** Protocol version used in the request */
	private static final String PROTOCOL_VERSION = "1.1.5";
	
	/** Parameters used for creating an authenticate request */
	private static final String AUTH_RT              = "0";
    public static final String AUTH_HW_ST            = "1";
    public static final String AUTH_HW_PIP_ST        = "2";
    //public static final String AUTH_HW_PIP_EIFACE_ST = "3";

    /** Parameters used for creating a reset request */
    private static final String RESET_RT        = "3";
    public static final String RESET_PIP_ST     = "1";
    public static final String RESET_BIO_PIP_ST = "2";

    /** Parameters used for creating a balance request */
    private static final String QUERY_RT    = "4";
    public static final String QUERY_BAL_ST = "1";
    public static final String QUERY_ACC_ST = "3";

    /** Query Records - QUERY_ACC_ST */
    public enum QueryRecord {
        BIOMETRIC_PIP   ( 20 ),
        ADVERTISING     ( 22 ),
        BIOMETRIC       ( 24 ),
        LINKING_CODE    ( 25 ),
        LINKED_ACCOUNTS ( 26 );

        private final int value;

        QueryRecord( int i ) {
            value = i;
        }

        public int getValue() {
            return value;
        }
    }

    /** Parameters used for creating a close request */
    private static final String CLOSE_RT        = "8";
    private static final String CLOSE_CLIENT_ST = "1";

    /** Parameters used for creating a registration request */
    private static final String REG_RT          = "9";
    public static final String REG_CLIENT_ST    = "0";
    public static final String REG_BIOMETRIC_ST = "3";
    //public static final String REG_GCM_SUBREQ   = "4";

    /** Parameters used for the linking requests */
    private static final String LINK_RT    = "10";
    public static final String LINK_ACC_ST = "0";

    /** Parameters used for the de-link requests */
    private static final String DELINK_RT     = "11";
    public static final String DELINK_TO_ST   = "0";
    public static final String DELINK_FROM_ST = "1";
	
	/** Variable that holds request string separator */
	private static final String	REQ_SEP = ",";

    /**
     * Creates an authentication switch request
     * @param pUsrData	Encrypted user's data
     * @param iAuthST Sub-type of the request
     * @return String Request for getting the authentication
     */
    public static String createAuthenticationRequest( String pUsrData, String iAuthST ) {
        StringBuilder sAuthenticationRequest = new StringBuilder();
        sAuthenticationRequest.append( PROTOCOL_VERSION ).append( REQ_SEP );
        sAuthenticationRequest.append( AUTH_RT ).append( REQ_SEP );
        sAuthenticationRequest.append( iAuthST ).append( REQ_SEP );
        sAuthenticationRequest.append( pUsrData );

        AppUtils.Logger( TAG, "Authentication (" + iAuthST + ") Request: " + sAuthenticationRequest.toString() );
        return sAuthenticationRequest.toString();
    }

    /**
     * Creates reset switch request to change a user profile
     * @param sUserData	Encrypted user's data
     * @param iResST Sub-type of the request
     * @return String 	Request to register a user
     */
    public static String createResetRequest( String sUserData, String iResST ) {
        StringBuilder sResetRequest = new StringBuilder();
        sResetRequest.append( PROTOCOL_VERSION ).append( REQ_SEP );
        sResetRequest.append( RESET_RT ).append( REQ_SEP );
        sResetRequest.append( iResST ).append( REQ_SEP );
        sResetRequest.append( sUserData );

        AppUtils.Logger( TAG, "Reset (" + iResST + ") Request: " + sResetRequest.toString() );
        return sResetRequest.toString();
    }

    /**
     * Creates a query request
     * @param sUsrData	Encrypted user's data
     * @param iQueryST Sub-type of the request
     * @return String Request for getting the balance
     */
    public static String createQueryRequest( String sUsrData, String iQueryST ) {
        StringBuilder sQueryRequest = new StringBuilder();
        sQueryRequest.append( PROTOCOL_VERSION ).append( REQ_SEP );
        sQueryRequest.append( QUERY_RT ).append( REQ_SEP );
        sQueryRequest.append( iQueryST ).append( REQ_SEP );
        sQueryRequest.append( sUsrData );

        AppUtils.Logger( TAG, "Third Party (" + iQueryST + ") Request: " + sQueryRequest.toString() );
        return sQueryRequest.toString();
    }

    /**
     * Creates a close switch request to close the account
     * @param sUserData	Encrypted user's data
     * @return String Request to close a user account
     */
    public static String createCloseRequest( String sUserData ) {
        StringBuilder sCloseRequest = new StringBuilder();
        sCloseRequest.append( PROTOCOL_VERSION ).append( REQ_SEP );
        sCloseRequest.append( CLOSE_RT ).append( REQ_SEP );
        sCloseRequest.append( CLOSE_CLIENT_ST ).append( REQ_SEP );
        sCloseRequest.append( sUserData );

        AppUtils.Logger( TAG, "Close Request: " + sCloseRequest.toString() );
        return sCloseRequest.toString();
    }

    /**
     * Creates an registration switch request
     * @param sUsrData	Encrypted user's data
     * @param iRegST Sub-type of the request
     * @return String Request for getting the registration code
     */
    public static String createRegistrationRequest( String sUsrData, String iRegST ) {
        StringBuilder sRegistrationRequest = new StringBuilder();
        sRegistrationRequest.append( PROTOCOL_VERSION ).append( REQ_SEP );
        sRegistrationRequest.append( REG_RT ).append( REQ_SEP );
        sRegistrationRequest.append( iRegST ).append( REQ_SEP );
        sRegistrationRequest.append( sUsrData );

        AppUtils.Logger( TAG, "Registration (" + iRegST + ") Request: " + sRegistrationRequest.toString() );
        return sRegistrationRequest.toString();
    }

    /**
     * Creates a link switch request to link accounts
     * @param sUserData	Encrypted user's data
     * @param iLinkST Sub-type of the request
     * @return String Request to link a user account
     */
    public static String createLinkingRequest( String sUserData, String iLinkST ) {
        StringBuilder sLinkingRequest = new StringBuilder();
        sLinkingRequest.append( PROTOCOL_VERSION ).append( REQ_SEP );
        sLinkingRequest.append( LINK_RT ).append( REQ_SEP );
        sLinkingRequest.append( iLinkST ).append( REQ_SEP );
        sLinkingRequest.append( sUserData );

        AppUtils.Logger( TAG, "Linking (" + iLinkST + ") Request: " + sLinkingRequest.toString() );
        return sLinkingRequest.toString();
    }

    /**
     * Creates a de-link switch request to de-link accounts
     * @param sUserData	Encrypted user's data
     * @param iDeLinkST Sub-type of the request
     * @return String Request to de-link a user account
     */
    public static String createDeLinkRequest( String sUserData, String iDeLinkST ) {
        StringBuilder sDeLinkRequest = new StringBuilder();
        sDeLinkRequest.append( PROTOCOL_VERSION ).append( REQ_SEP );
        sDeLinkRequest.append( DELINK_RT ).append( REQ_SEP );
        sDeLinkRequest.append( iDeLinkST ).append( REQ_SEP );
        sDeLinkRequest.append( sUserData );

        AppUtils.Logger( TAG, "DeLink (" + iDeLinkST + ") Request: " + sDeLinkRequest.toString() );
        return sDeLinkRequest.toString();
    }
}
