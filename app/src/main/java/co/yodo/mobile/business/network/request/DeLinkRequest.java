package co.yodo.mobile.business.network.request;

import javax.crypto.spec.SecretKeySpec;

import co.yodo.mobile.business.component.cipher.AESCrypt;
import co.yodo.mobile.business.component.cipher.RSACrypt;
import co.yodo.mobile.business.network.ApiClient;
import co.yodo.mobile.business.network.model.ServerResponse;
import co.yodo.mobile.business.network.request.contract.IRequest;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by hei on 12/06/16.
 * Request a de-link from an account to the server
 */
public class DeLinkRequest extends IRequest {
    /** DEBUG */
    @SuppressWarnings( "unused" )
    private static final String TAG = DeLinkRequest.class.getSimpleName();

    /** De-Link request type */
    private static final String DELINK_RT = "11";

    /** De-Link sub-types */
    public enum DeLinkST {
        TO   ( "0" ),
        FROM ( "1" );

        private final String value;

        DeLinkST( String value ) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }
    }

    /** Sub-type of the request */
    private final DeLinkST mRequestST;

    /** Interface for the DE_LINK requests */
    interface IApiEndpoint {
        @GET( YODO_ADDRESS + "{request}" )
        Call<ServerResponse> deLink( @Path( "request" ) String request );
    }

    /**
     * Delinks two linked accounts
     * @param responseCode The code used to respond the caller activity
     * @param hardwareToken The hardware token of the device
     * @param pip The password of the user
     * @param linkedAccount The linked account number
     * @param requestST The type of the account/request (donor or recipient)
     */
    public DeLinkRequest( int responseCode, String hardwareToken, String pip, String linkedAccount, DeLinkST requestST ) {

        this.formattedUsrData =
                hardwareToken + REQ_SEP +
                pip + REQ_SEP +
                linkedAccount;
        this.mRequestST = requestST;
    }

    @Override
    public void execute( RSACrypt cipher, ApiClient manager, ApiClient.RequestCallback callback ) {
        String sEncryptedClientData, pRequest;

        SecretKeySpec key = AESCrypt.generateKey();

        encyptedData = AESCrypt.encrypt( formattedUsrData, key );
        //mEncyptedSignature = MessageIntegrityAttribute.encode( formattedUsrData, key );
        encyptedKey = cipher.encrypt( AESCrypt.encodeKey( key ) );

        // Encrypting to newInstance request
        sEncryptedClientData =
                encyptedKey + REQ_SEP +
                        encyptedData;

        pRequest = buildRequest( DELINK_RT,
                mRequestST.getValue(),
                sEncryptedClientData
        );

        IApiEndpoint iCaller = manager.create( IApiEndpoint.class );
        Call<ServerResponse> sResponse = iCaller.deLink( pRequest );
        manager.sendXMLRequest( sResponse, callback );
    }
}
