package co.yodo.mobile.network.request;

import co.yodo.mobile.component.cipher.RSACrypt;
import co.yodo.mobile.helper.SystemUtils;
import co.yodo.mobile.network.ApiClient;
import co.yodo.mobile.network.request.contract.IRequest;

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
    public void execute( RSACrypt oEncrypter, ApiClient manager ) {
        String sEncryptedClientData, sEncryptedHardwareToken, sEncryptedId, sEncryptedNewPIP,
                pRequest;

        switch( this.mRequestST ) {
            case PIP:
                // Encrypting to create request
                sEncryptedHardwareToken = oEncrypter.encrypt( mHardwareToken );
                sEncryptedId = oEncrypter.encrypt( mIdentifier );
                sEncryptedNewPIP = oEncrypter.encrypt( mNewPIP );

                sEncryptedClientData =
                        sEncryptedHardwareToken + REQ_SEP +
                        sEncryptedId + REQ_SEP +
                        sEncryptedNewPIP;
                break;

            case PIP_BIO:
                this.mFormattedUsrData =
                        this.mIdentifier + REQ_SEP +
                        this.mHardwareToken + REQ_SEP +
                        this.mNewPIP;

                // Encrypting to create request
                sEncryptedClientData = oEncrypter.encrypt( mFormattedUsrData );
                break;

            default:
                throw new IllegalArgumentException( "type no supported" );
        }

        pRequest = buildRequest( RESET_RT,
                this.mRequestST.getValue(),
                sEncryptedClientData
        );

        SystemUtils.Logger( TAG, pRequest );
        manager.sendXMLRequest( pRequest, responseCode );
    }
}
