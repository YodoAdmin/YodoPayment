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
 * Request a close account from the server
 */
public class CloseRequest extends IRequest {
    /** DEBUG */
    @SuppressWarnings( "unused" )
    private static final String TAG = CloseRequest.class.getSimpleName();

    /** Close request type */
    private static final String CLOSE_RT = "8";

    /** Close sub-types */
    private enum CloseST {
        CLIENT ( "1" );

        private final String value;

        CloseST( String value ) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }
    }

    /** Sub-type of the request */
    private final CloseST mRequestST;

    /** Interface for the CLOSE requests */
    interface IApiEndpoint {
        @GET( YODO_ADDRESS + "{request}" )
        Call<ServerResponse> closeAcc( @Path( "request" ) String request );
    }

    /**
     * Request to close a user account
     * @param responseCode The code used to respond the caller activity
     * @param hardwareToken The hardware token of the device
     * @param pip The password of the user
     */
    public CloseRequest( int responseCode, String hardwareToken, String pip ) {
        super( responseCode );

        this.mFormattedUsrData =
                pip + USR_SEP +
                hardwareToken + USR_SEP +
                System.currentTimeMillis() / 1000L + REQ_SEP +
                "0" + REQ_SEP + "0"; // Mock GPS, not needed any more
        this.mRequestST = CloseST.CLIENT;
    }

    @Override
    public void execute( RSACrypt oEncrypter, ApiClient oManager ) {
        String sEncryptedClientData, pRequest;

        SecretKeySpec key = AESCrypt.generateKey();

        mEncyptedData = AESCrypt.encrypt( mFormattedUsrData, key );
        //mEncyptedSignature = MessageIntegrityAttribute.encode( mFormattedUsrData, key );
        mEncyptedKey = oEncrypter.encrypt( AESCrypt.encodeKey( key ) );

        // Encrypting to create request
        sEncryptedClientData =
                mEncyptedKey + REQ_SEP +
                mEncyptedData;

        pRequest = buildRequest( CLOSE_RT,
                mRequestST.getValue(),
                sEncryptedClientData
        );

        IApiEndpoint iCaller = oManager.create( IApiEndpoint.class );
        Call<ServerResponse> sResponse = iCaller.closeAcc( pRequest );
        oManager.sendRequest( sResponse, mResponseCode );
    }
}