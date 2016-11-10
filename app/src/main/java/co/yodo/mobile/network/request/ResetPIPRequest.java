package co.yodo.mobile.network.request;

import javax.crypto.spec.SecretKeySpec;

import co.yodo.mobile.component.cipher.AESCrypt;
import co.yodo.mobile.component.cipher.RSACrypt;
import co.yodo.mobile.network.ApiClient;
import co.yodo.mobile.network.model.ServerResponse;
import co.yodo.mobile.network.request.contract.IRequest;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by hei on 12/06/16.
 * Request a pip reset from the server
 */
public class ResetPIPRequest extends IRequest {
    /** DEBUG */
    @SuppressWarnings( "unused" )
    private static final String TAG = ResetPIPRequest.class.getSimpleName();

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
    private final String mHardwareToken;
    private final String mIdentifier;
    private final String mNewPIP;

    /** Sub-type of the request */
    private final ResetST mRequestST;

    /** Interface for the RESET_PIP requests */
    interface IApiEndpoint {
        @GET( YODO_ADDRESS + "{request}" )
        Call<ServerResponse> resetPIP( @Path( "request" ) String request );
    }

    /**
     * Request a change of PIP using the current pip or a biometric token
     * @param responseCode The code used to respond the caller activity
     * @param hardwareToken The hardware token of the device
     * @param identifier The user's password or the authnumber of the biometric token
     * @param newPip The new user password
     * @param requestST The sub-type identifier
     */
    public ResetPIPRequest( int responseCode, String hardwareToken, String identifier, String newPip, ResetST requestST ) {
        super( responseCode );
        this.mHardwareToken = hardwareToken;
        this.mIdentifier = identifier;
        this.mNewPIP = newPip;
        this.mRequestST = requestST;
    }

    /**
     * Request a change of PIP using the current pip by default
     * @param responseCode The code used to respond the caller activity
     * @param hardwareToken The hardware token of the device
     * @param identifier The user's password or the authnumber of the biometric token
     * @param newPip The new user password
     */
    public ResetPIPRequest( int responseCode, String hardwareToken, String identifier, String newPip ) {
        this( responseCode, hardwareToken, identifier, newPip, ResetST.PIP );
    }

    @Override
    public void execute( RSACrypt oEncrypter, ApiClient oManager ) {
        String sEncryptedClientData, sEncryptedHardwareToken, sEncryptedId, sEncryptedNewPIP,
                pRequest;

        SecretKeySpec key = AESCrypt.generateKey();

        //mEncyptedSignature = MessageIntegrityAttribute.encode( mFormattedUsrData, key );
        mEncyptedKey = oEncrypter.encrypt( AESCrypt.encodeKey( key ) );

        switch( this.mRequestST ) {
            case PIP:
                // Encrypting to create request
                sEncryptedHardwareToken = AESCrypt.encrypt( mHardwareToken, key );
                sEncryptedId = AESCrypt.encrypt( mIdentifier, key );
                sEncryptedNewPIP = AESCrypt.encrypt( mNewPIP, key );

                sEncryptedClientData =
                        mEncyptedKey            + REQ_SEP +
                        sEncryptedHardwareToken + REQ_SEP +
                        sEncryptedId            + REQ_SEP +
                        sEncryptedNewPIP;
                break;

            case PIP_BIO:
                this.mFormattedUsrData =
                        this.mIdentifier + REQ_SEP +
                        this.mHardwareToken + REQ_SEP +
                        this.mNewPIP;

                mEncyptedData = AESCrypt.encrypt( mFormattedUsrData, key );

                // Encrypting to create request
                sEncryptedClientData =
                        mEncyptedKey + REQ_SEP +
                        mEncyptedData;
                break;

            default:
                throw new IllegalArgumentException( "type not supported" );
        }

        pRequest = buildRequest( RESET_RT,
                mRequestST.getValue(),
                sEncryptedClientData
        );

        IApiEndpoint iCaller = oManager.create( IApiEndpoint.class );
        Call<ServerResponse> sResponse = iCaller.resetPIP( pRequest );
        oManager.sendRequest( sResponse, mResponseCode );
    }
}
