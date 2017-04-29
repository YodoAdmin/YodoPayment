package co.yodo.mobile.business.network.request.contract;

import co.yodo.mobile.business.component.cipher.RSACrypt;
import co.yodo.mobile.business.network.ApiClient;

/**
 * Created by hei on 10/06/16.
 * Class used to implement the Command Design Pattern for the requests
 */
public abstract class IRequest {
    /** DEBUG */
    @SuppressWarnings( "unused" )
    private static final String TAG = IRequest.class.getSimpleName();

     /** Formatted data for the request */
    protected String formattedUsrData;

    /** Encrypted data for the request */
    protected String encyptedData;
    protected String encyptedKey;

    /** Protocol version used in the requests */
    protected static final String PROTOCOL_VERSION = "1.1.6";

    /** Two paths used for the requests */
    protected static final String YODO_ADDRESS = "/yodo/yodoswitchrequest/getRequest/";

    /** User's data separator */
    protected static final String USR_SEP     = "**";
    protected static final String REQ_SEP     = ",";
    protected static final String PCLIENT_SEP = "/";

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

    public abstract void execute( RSACrypt cipher, ApiClient manager, ApiClient.RequestCallback callback );
}
