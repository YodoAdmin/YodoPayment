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
 * Request a link of accounts to the server
 */
public class LinkRequest extends IRequest {
    /** DEBUG */
    @SuppressWarnings( "unused" )
    private static final String TAG = LinkRequest.class.getSimpleName();

    /** Link request type */
    private static final String LINK_RT = "10";

    /** Link sub-types */
    private enum LinkST {
        ACC ( "0" );

        private final String value;

        LinkST( String value ) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }
    }

    /** Sub-type of the request */
    private final LinkST mRequestST;

    /** Interface for the DE_LINK requests */
    interface IApiEndpoint {
        @GET( YODO_ADDRESS + "{request}" )
        Call<ServerResponse> link( @Path( "request" ) String request );
    }

    /**
     * Link two accounts for heart transactions
     * @param responseCode The code used to respond the caller activity
     * @param hardwareToken The hardware token of the device
     * @param linkCode The code used to link accounts
     */
    public LinkRequest( int responseCode, String hardwareToken, String linkCode ) {
        this.formattedUsrData =
                hardwareToken + REQ_SEP +
                linkCode + REQ_SEP +
                System.currentTimeMillis() / 1000L;
        this.mRequestST = LinkST.ACC;
    }

    @Override
    public void execute( RSACrypt cipher, ApiClient manager, ApiClient.RequestCallback callback ) {
        String sEncryptedClientData, pRequest;

        SecretKeySpec key = AESCrypt.generateKey();

        encyptedData = AESCrypt.encrypt( formattedUsrData, key );
        encyptedKey = cipher.encrypt( AESCrypt.encodeKey( key ) );

        // Encrypting to newInstance request
        sEncryptedClientData =
                encyptedKey + REQ_SEP +
                        encyptedData;

        pRequest = buildRequest( LINK_RT,
                mRequestST.getValue(),
                sEncryptedClientData
        );

        IApiEndpoint iCaller = manager.create( IApiEndpoint.class );
        Call<ServerResponse> sResponse = iCaller.link( pRequest );
        manager.sendXMLRequest( sResponse, callback );
    }
}
