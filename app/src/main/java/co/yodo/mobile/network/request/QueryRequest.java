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
 * Request data from the server
 */
public class QueryRequest extends IRequest {
    /** DEBUG */
    @SuppressWarnings( "unused" )
    private static final String TAG = QueryRequest.class.getSimpleName();

    /** Query request type */
    private static final String QUERY_RT = "4";

    /** Query sub-types */
    private enum QueryST {
        BAL ( "1" ),
        ACC ( "3" );

        private final String value;

        QueryST( String value ) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }
    }

    /** Query Records - QUERY_ACC_ST */
    public enum Record {
        ADVERTISING     ( 22 ),
        BIOMETRIC       ( 24 ),
        LINKING_CODE    ( 25 ),
        LINKED_ACCOUNTS ( 26 );

        private final int value;

        Record( int i ) {
            value = i;
        }

        public int getValue() {
            return value;
        }
    }

    /** Sub-type of the request */
    private final QueryST mRequestST;

    /** Interface for the QUERY requests */
    interface IApiEndpoint {
        @GET( YODO_ADDRESS + "{request}" )
        Call<ServerResponse> query( @Path( "request" ) String request );
    }

    /**
     * Request the user's balance
     * @param responseCode The code used to respond the caller activity
     * @param hardwareToken The hardware token of the device
     * @param pip The password of the user
     */
    public QueryRequest( int responseCode, String hardwareToken, String pip ) {
        super( responseCode );
        this.mFormattedUsrData =
                hardwareToken + PCLIENT_SEP +
                pip + PCLIENT_SEP +
                System.currentTimeMillis() / 1000L;
        this.mRequestST = QueryST.BAL;
    }

    /**
     * Request data from the server using only the hardware token and the record of the data
     * @param responseCode The code used to respond the caller activity
     * @param hardwareToken The hardware token of the device
     * @param record A value that describes the type of the data
     */
    public QueryRequest( int responseCode, String hardwareToken, Record record ) {
        super( responseCode );
        this.mFormattedUsrData =
                hardwareToken + REQ_SEP +
                record.getValue();
        this.mRequestST = QueryST.ACC;
    }

    /**
     * Request data from the server using only the hardware token and the record of the data
     * @param responseCode The code used to respond the caller activity
     * @param hardwareToken The hardware token of the device
     * @param other The user password or sometimes another identifier like the merchant name
     * @param record A value that describes the type of the data
     */
    public QueryRequest( int responseCode, String hardwareToken, String other, Record record ) {
        super( responseCode );
        this.mFormattedUsrData =
                hardwareToken + REQ_SEP +
                other         + REQ_SEP +
                record.getValue();
        this.mRequestST = QueryST.ACC;
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

        pRequest = buildRequest( QUERY_RT,
                mRequestST.getValue(),
                sEncryptedClientData
        );

        IApiEndpoint iCaller = oManager.create( IApiEndpoint.class );
        Call<ServerResponse> sResponse = iCaller.query( pRequest );
        oManager.sendRequest( sResponse, mResponseCode );
    }
}
