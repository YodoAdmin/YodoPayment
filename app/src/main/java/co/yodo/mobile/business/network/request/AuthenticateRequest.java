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
 * Created by hei on 10/06/16.
 * Request an authentication from the server
 */
public class AuthenticateRequest extends IRequest {
    /** Authenticate request type */
    private static final String AUTH_RT = "0";

    /** Authenticate sub-types */
    private enum AuthST {
        HW     ( "1" ),
        HW_PIP ( "2" );

        private final String value;

        AuthST( String value ) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }
    }

    /** Sub-type of the request */
    private final AuthST requestST;

    /** Interface for the AUTH requests */
    interface IApiEndpoint {
        @GET( YODO_ADDRESS + "{request}" )
        Call<ServerResponse> authUser( @Path( "request" ) String request );
    }

    /**
     * Authentication with just the hardware token
     * @param hardwareToken The hardware token of the device
     */
    public AuthenticateRequest( String hardwareToken ) {
        this.formattedUsrData = hardwareToken;
        this.requestST = AuthST.HW;
    }

    /**
     * Authentication with just the hardware token
     * @param hardwareToken The hardware token of the device
     * @param otp The one time password of the user
     */
    public AuthenticateRequest( String hardwareToken, String otp ) {
        this.formattedUsrData =
                hardwareToken + PCLIENT_SEP +
                otp + PCLIENT_SEP +
                System.currentTimeMillis() / 1000L;
        this.requestST = AuthST.HW_PIP;
    }

    @Override
    public void execute(RSACrypt cipher, ApiClient manager, ApiClient.RequestCallback callback) {
        // Generate the AES key
        SecretKeySpec key = AESCrypt.generateKey();
        encyptedKey = cipher.encrypt(AESCrypt.encodeKey(key));
        encyptedData = AESCrypt.encrypt(formattedUsrData, key);

        // Encrypting to newInstance request
        final String encryptedClientData = encyptedKey + REQ_SEP + encyptedData;
        final String requestData = buildRequest(AUTH_RT,
                requestST.getValue(),
                encryptedClientData
        );

        IApiEndpoint iCaller = manager.create(IApiEndpoint.class);
        Call<ServerResponse> request = iCaller.authUser(requestData);
        manager.sendXMLRequest(request, callback);
    }
}
