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
 * Created by yodop on 2017-07-22.
 * Implements the basic logic for the exchange
 */
public final class ExchRequest extends IRequest {
    /** Exchange request type */
    private static final String EXCH_RT = "7";

    /** Exchange sub-types */
    private enum ExchST {
        P2P ( "7" );

        private final String value;

        ExchST( String value ) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }
    }

    /** Sub-type of the request */
    private final ExchST requestST;

    /** Interface for the Exchange requests */
    interface IApiEndpoint {
        @GET( YODO_ADDRESS + "{request}" )
        Call<ServerResponse> p2p(@Path( "request" ) String request );
    }

    /**
     * Transfer money from one account to another
     * @param hardwareFrom The hardware token of the account that will send the money
     * @param hardwareTo The hardware token of the account to transfer
     * @param amount The amount of money to transfer
     */
    public ExchRequest( String hardwareFrom, String hardwareTo, String amount ) {
        this.formattedUsrData = hardwareFrom + REQ_SEP + hardwareTo + REQ_SEP + amount;
        this.requestST = ExchST.P2P;
    }

    @Override
    public void execute(RSACrypt cipher, ApiClient manager, ApiClient.RequestCallback callback) {
        // Generate AES Key
        SecretKeySpec key = AESCrypt.generateKey();
        encyptedData = AESCrypt.encrypt( formattedUsrData, key );
        encyptedKey = cipher.encrypt( AESCrypt.encodeKey( key ) );

        // Encrypting to newInstance request
        final String encryptedClientData = encyptedKey + REQ_SEP + encyptedData;
        final String requestData = buildRequest( EXCH_RT,
                requestST.getValue(),
                encryptedClientData
        );

        DeLinkRequest.IApiEndpoint iCaller = manager.create( DeLinkRequest.IApiEndpoint.class );
        Call<ServerResponse> request = iCaller.deLink( requestData );
        manager.sendXMLRequest( request, callback );
    }
}
