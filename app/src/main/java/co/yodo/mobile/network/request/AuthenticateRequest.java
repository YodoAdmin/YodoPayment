package co.yodo.mobile.network.request;

import co.yodo.mobile.component.Encrypter;
import co.yodo.mobile.helper.SystemUtils;
import co.yodo.mobile.network.YodoRequest;
import co.yodo.mobile.network.contract.IRequest;

/**
 * Created by hei on 10/06/16.
 * Request an authentication from the server
 */
public class AuthenticateRequest extends IRequest {
    /** DEBUG */
    @SuppressWarnings( "unused" )
    private static final String TAG = AuthenticateRequest.class.getSimpleName();

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
    private AuthST mRequestST;

    /**
     * Authentication with just the hardware token
     * @param responseCode The code used to respond the caller activity
     * @param hardwareToken The hardware token of the device
     */
    public AuthenticateRequest( int responseCode, String hardwareToken ) {
        super( responseCode );
        this.mFormattedUsrData = hardwareToken;
        this.mRequestST = AuthST.HW;
    }

    /**
     * Authentication with just the hardware token
     * @param responseCode The code used to respond the caller activity
     * @param hardwareToken The hardware token of the device
     * @param pip The password of the user
     */
    public AuthenticateRequest( int responseCode, String hardwareToken, String pip ) {
        super( responseCode );
        this.mFormattedUsrData =
                hardwareToken + PCLIENT_SEP +
                pip + PCLIENT_SEP +
                System.currentTimeMillis() / 1000L;
        this.mRequestST = AuthST.HW_PIP;
    }

    @Override
    public void execute( Encrypter oEncrypter, YodoRequest manager ) {
        String sEncryptedClientData, pRequest;

        // Encrypting to create request
        oEncrypter.setsUnEncryptedString( this.mFormattedUsrData );
        oEncrypter.rsaEncrypt();
        sEncryptedClientData = oEncrypter.bytesToHex();

        pRequest = buildRequest( AUTH_RT,
                this.mRequestST.getValue(),
                sEncryptedClientData
        );

        SystemUtils.Logger( TAG, pRequest );
        manager.sendXMLRequest( pRequest, responseCode );
    }
}
