package co.yodo.mobile.network.request;

import javax.crypto.spec.SecretKeySpec;

import co.yodo.mobile.component.cipher.AESCrypt;
import co.yodo.mobile.component.cipher.RSACrypt;
import co.yodo.mobile.helper.SystemUtils;
import co.yodo.mobile.network.ApiClient;
import co.yodo.mobile.network.request.contract.IRequest;

/**
 * Created by hei on 12/06/16.
 * Request a link of accounts to the server
 */
public class LinkRequest extends IRequest {
    /** DEBUG */
    @SuppressWarnings( "unused" )
    private static final String TAG = LinkRequest.class.getSimpleName();

    /** Link request type */
    private static final String LINK_RT = "10";

    /** Link sub-types */
    private enum LinkST {
        ACC ( "0" );

        private final String value;

        LinkST( String value ) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }
    }

    /** Sub-type of the request */
    private final LinkST mRequestST;

    /**
     * Link two accounts for heart transactions
     * @param responseCode The code used to respond the caller activity
     * @param hardwareToken The hardware token of the device
     * @param linkCode The code used to link accounts
     */
    public LinkRequest( int responseCode, String hardwareToken, String linkCode ) {
        super( responseCode );
        this.mFormattedUsrData =
                hardwareToken + REQ_SEP +
                linkCode + REQ_SEP +
                System.currentTimeMillis() / 1000L;
        this.mRequestST = LinkST.ACC;
    }

    @Override
    public void execute( RSACrypt oEncrypter, ApiClient manager ) {
        String sEncryptedClientData, pRequest;

        SecretKeySpec key = AESCrypt.generateKey();

        mEncyptedData = AESCrypt.encrypt( mFormattedUsrData, key );
        //mEncyptedSignature = MessageIntegrityAttribute.encode( mFormattedUsrData, key );
        mEncyptedKey = oEncrypter.encrypt( AESCrypt.encodeKey( key ) );

        // Encrypting to create request
        //sEncryptedClientData = oEncrypter.encrypt( mFormattedUsrData );
        sEncryptedClientData =
                mEncyptedKey + REQ_SEP +
                mEncyptedData;

        pRequest = buildRequest( LINK_RT,
                this.mRequestST.getValue(),
                sEncryptedClientData
        );

        SystemUtils.Logger( TAG, pRequest );
        manager.sendXMLRequest( pRequest, responseCode );
    }
}
