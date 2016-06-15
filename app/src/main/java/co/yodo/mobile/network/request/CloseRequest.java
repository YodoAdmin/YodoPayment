package co.yodo.mobile.network.request;

import co.yodo.mobile.component.Encrypter;
import co.yodo.mobile.helper.SystemUtils;
import co.yodo.mobile.network.YodoRequest;
import co.yodo.mobile.network.request.contract.IRequest;

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
    public enum CloseST {
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
    public void execute( Encrypter oEncrypter, YodoRequest manager ) {
        String sEncryptedClientData, pRequest;

        // Encrypting to create request
        oEncrypter.setsUnEncryptedString( this.mFormattedUsrData );
        oEncrypter.rsaEncrypt();
        sEncryptedClientData = oEncrypter.bytesToHex();

        pRequest = buildRequest( CLOSE_RT,
                this.mRequestST.getValue(),
                sEncryptedClientData
        );

        SystemUtils.Logger( TAG, pRequest );
        manager.sendXMLRequest( pRequest, responseCode );
    }
}