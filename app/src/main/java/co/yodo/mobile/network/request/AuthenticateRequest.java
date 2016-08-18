package co.yodo.mobile.network.request;

import javax.crypto.spec.SecretKeySpec;

import co.yodo.mobile.component.cipher.AESCrypt;
import co.yodo.mobile.component.cipher.RSACrypt;
import co.yodo.mobile.component.signature.MessageIntegrityAttribute;
import co.yodo.mobile.helper.SystemUtils;
import co.yodo.mobile.network.ApiClient;
import co.yodo.mobile.network.request.contract.IRequest;

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
    private final AuthST mRequestST;

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
    public void execute( RSACrypt oEncrypter, ApiClient manager ) {
        String sEncryptedClientData, pRequest;

        SecretKeySpec key = AESCrypt.generateKey();

        mEncyptedData = AESCrypt.encrypt( mFormattedUsrData, key );
        mEncyptedSignature = MessageIntegrityAttribute.encode( mFormattedUsrData, key );
        mEncyptedKey = oEncrypter.encrypt( AESCrypt.encodeKey( key ) );

        // Encrypting to create request
        //sEncryptedClientData = oEncrypter.encrypt( mFormattedUsrData );
        sEncryptedClientData =
                mEncyptedData      + REQ_SEP +
                mEncyptedSignature + REQ_SEP +
                mEncyptedKey;

        pRequest = buildRequest( AUTH_RT,
                this.mRequestST.getValue(),
                sEncryptedClientData
        );

        SystemUtils.Logger( TAG, pRequest );
        manager.sendXMLRequest( pRequest, responseCode );
    }
}
