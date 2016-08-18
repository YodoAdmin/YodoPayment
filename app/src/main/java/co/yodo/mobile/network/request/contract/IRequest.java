package co.yodo.mobile.network.request.contract;

import co.yodo.mobile.component.cipher.RSACrypt;
import co.yodo.mobile.network.ApiClient;

/**
 * Created by hei on 10/06/16.
 * Class used to implement the Command Design Pattern for the requests
 */
public abstract class IRequest {
     /** Formatted data for the request */
    protected String mFormattedUsrData;

    /** Encrypted data for the request */
    protected String mEncyptedData;
    protected String mEncyptedSignature;
    protected String mEncyptedKey;

    /** The code for the response */
    protected final int responseCode;

    /** Protocol version used in the requests */
    protected static final String PROTOCOL_VERSION = "1.1.5";

    /** User's data separator */
    protected static final String USR_SEP     = "**";
    protected static final String REQ_SEP     = ",";
    protected static final String PCLIENT_SEP = "/";

    protected IRequest( int responseCode ) {
        this.responseCode = responseCode;
    }

    /**
     * Builds the request string
     * @param pRequestType The request type
     * @param pSubType The request sub-type
     * @param pUserData Encrypted user's data
     * @return The request string that is send to the server
     */
    protected static String buildRequest( String pRequestType, String pSubType, String pUserData ) {
        return PROTOCOL_VERSION + REQ_SEP +
               pRequestType     + REQ_SEP +
               pSubType         + REQ_SEP +
               pUserData;
    }

    public abstract void execute( RSACrypt oEncrypter, ApiClient manager );
}
