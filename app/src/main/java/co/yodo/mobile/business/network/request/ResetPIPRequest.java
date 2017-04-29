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
 * Request a pip reset from the server
 */
public class ResetPIPRequest extends IRequest {
    /** ResetPIP request type */
    private static final String RESET_RT = "3";

    /** ResetPIP sub-types */
    public enum ResetST {
        PIP     ( "1" ),
        PIP_BIO ( "2" );

        private final String value;

        ResetST( String value ) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }
    }

    /** Data for the request */
    private final String hardwareToken;
    private final String userToken;
    private final String newPip;

    /** Sub-type of the request */
    private final ResetST requestST;

    /** Interface for the RESET_PIP requests */
    interface IApiEndpoint {
        @GET( YODO_ADDRESS + "{request}" )
        Call<ServerResponse> resetPIP( @Path( "request" ) String request );
    }

    /**
     * Request a change of PIP using the current pip or a biometric token
     * @param hardwareToken The hardware token of the device
     * @param identifier The user's password or the authnumber of the biometric token
     * @param newPip The new user password
     * @param requestST The sub-type identifier
     */
    public ResetPIPRequest( String hardwareToken, String identifier, String newPip, ResetST requestST ) {
        this.hardwareToken = hardwareToken;
        this.userToken = identifier;
        this.newPip = newPip;
        this.requestST = requestST;
    }

    /**
     * Request a change of PIP using the current pip by default
     * @param hardwareToken The hardware token of the device
     * @param identifier The user's password or the authnumber of the biometric token
     * @param newPip The new user password
     */
    public ResetPIPRequest( String hardwareToken, String identifier, String newPip ) {
        this( hardwareToken, identifier, newPip, ResetST.PIP );
    }

    @Override
    public void execute( RSACrypt cipher, ApiClient manager, ApiClient.RequestCallback callback ) {
        String encryptedClientData;

        // Create the AES secret key
        SecretKeySpec key = AESCrypt.generateKey();
        encyptedKey = cipher.encrypt( AESCrypt.encodeKey( key ) );

        switch( requestST ) {
            case PIP:
                // Encrypting to newInstance request
                final String encryptedHardwareToken = AESCrypt.encrypt( hardwareToken, key );
                final String encryptedToken = AESCrypt.encrypt( userToken, key );
                final String encryptedNewPip = AESCrypt.encrypt( newPip, key );

                encryptedClientData =
                        encyptedKey + REQ_SEP +
                        encryptedHardwareToken + REQ_SEP +
                        encryptedToken + REQ_SEP +
                        encryptedNewPip;
                break;

            case PIP_BIO:
                formattedUsrData =
                        userToken + REQ_SEP +
                                hardwareToken + REQ_SEP +
                                newPip;
                encyptedData = AESCrypt.encrypt( formattedUsrData, key );

                // Encrypting to newInstance request
                encryptedClientData =
                        encyptedKey + REQ_SEP +
                        encyptedData;
                break;

            default:
                throw new IllegalArgumentException( "type not supported" );
        }

        final String requestData = buildRequest( RESET_RT,
                requestST.getValue(),
                encryptedClientData
        );

        IApiEndpoint iCaller = manager.create( IApiEndpoint.class );
        Call<ServerResponse> request = iCaller.resetPIP( requestData );
        manager.sendXMLRequest( request, callback );
    }
}
