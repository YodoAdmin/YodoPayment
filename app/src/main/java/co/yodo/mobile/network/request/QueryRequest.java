package co.yodo.mobile.network.request;

import co.yodo.mobile.component.Encrypter;
import co.yodo.mobile.network.YodoRequest;
import co.yodo.mobile.network.contract.IRequest;

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
    private QueryST mRequestST;

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
                other + REQ_SEP +
                record.getValue();
        this.mRequestST = QueryST.ACC;
    }

    @Override
    public void execute( Encrypter oEncrypter, YodoRequest manager ) {
        String sEncryptedClientData, pRequest;

        // Encrypting to create request
        oEncrypter.setsUnEncryptedString( this.mFormattedUsrData );
        oEncrypter.rsaEncrypt();
        sEncryptedClientData = oEncrypter.bytesToHex();

        pRequest = buildRequest( QUERY_RT,
                this.mRequestST.getValue(),
                sEncryptedClientData
        );

        manager.sendXMLRequest( pRequest, responseCode );
    }
}
