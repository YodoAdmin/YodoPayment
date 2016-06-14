package co.yodo.mobile.network.request;

import co.yodo.mobile.component.Encrypter;
import co.yodo.mobile.helper.SystemUtils;
import co.yodo.mobile.network.YodoRequest;
import co.yodo.mobile.network.contract.IRequest;

/**
 * Created by hei on 12/06/16.
 * Request a de-link from an account to the server
 */
public class DeLinkRequest extends IRequest {
    /** DEBUG */
    @SuppressWarnings( "unused" )
    private static final String TAG = DeLinkRequest.class.getSimpleName();

    /** De-Link request type */
    private static final String DELINK_RT = "11";

    /** De-Link sub-types */
    public enum DeLinkST {
        TO   ( "0" ),
        FROM ( "1" );

        private final String value;

        DeLinkST( String value ) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }
    }

    /** Sub-type of the request */
    private DeLinkST mRequestST;

    /**
     * Delinks two linked accounts
     * @param responseCode The code used to respond the caller activity
     * @param hardwareToken The hardware token of the device
     * @param pip The password of the user
     * @param linkedAccount The linked account number
     * @param requestST The type of the account/request (donor or recipient)
     */
    public DeLinkRequest( int responseCode, String hardwareToken, String pip, String linkedAccount, DeLinkST requestST ) {
        super( responseCode );

        this.mFormattedUsrData =
                hardwareToken + REQ_SEP +
                pip + REQ_SEP +
                linkedAccount;
        this.mRequestST = requestST;
    }

    @Override
    public void execute( Encrypter oEncrypter, YodoRequest manager ) {
        String sEncryptedClientData, pRequest;

        // Encrypting to create request
        oEncrypter.setsUnEncryptedString( this.mFormattedUsrData );
        oEncrypter.rsaEncrypt();
        sEncryptedClientData = oEncrypter.bytesToHex();

        pRequest = buildRequest( DELINK_RT,
                this.mRequestST.getValue(),
                sEncryptedClientData
        );

        SystemUtils.Logger( TAG, pRequest );
        manager.sendXMLRequest( pRequest, responseCode );
    }
}
