package co.yodo.mobile.network.request;

import co.yodo.mobile.component.Encrypter;
import co.yodo.mobile.helper.SystemUtils;
import co.yodo.mobile.network.YodoRequest;
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
    public void execute( Encrypter oEncrypter, YodoRequest manager ) {
        String sEncryptedClientData, pRequest;

        // Encrypting to create request
        oEncrypter.setsUnEncryptedString( this.mFormattedUsrData );
        oEncrypter.rsaEncrypt();
        sEncryptedClientData = oEncrypter.bytesToHex();

        pRequest = buildRequest( LINK_RT,
                this.mRequestST.getValue(),
                sEncryptedClientData
        );

        SystemUtils.Logger( TAG, pRequest );
        manager.sendXMLRequest( pRequest, responseCode );
    }
}
