package co.yodo.mobile.business.network.request;

import co.yodo.mobile.business.network.ApiClient;
import co.yodo.mobile.business.network.encryption.IEncryption;
import co.yodo.mobile.business.network.model.ServerResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by hei on 12/06/16.
 * Request a de-link from an account to the server
 */
public class DeLinkRequest extends IRequest {
    /** De-Link request type */
    private static final String DELINK_RT = "11";

    /** De-Link sub-types */
    public enum DeLinkST {
        TO   ("0"),
        FROM ("1");

        private final String value;

        DeLinkST(String value) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }
    }

    /** Sub-type of the request */
    private final DeLinkST requestST;

    /** Interface for the DE_LINK requests */
    interface IApiEndpoint {
        @GET(YODO_ADDRESS + "{request}")
        Call<ServerResponse> deLink(@Path("request") String request );
    }

    /**
     * Delinks two linked accounts
     * @param hardwareToken The hardware token of the device
     * @param pip The password of the user
     * @param linkedAccount The linked account number
     * @param requestST The type of the account/request (donor or recipient)
     */
    public DeLinkRequest(String hardwareToken, String pip, String linkedAccount, DeLinkST requestST) {
        this.formattedUsrData = hardwareToken + REQ_SEP + pip + REQ_SEP + linkedAccount;
        this.requestST = requestST;
    }

    @Override
    public void execute(IEncryption encryption, ApiClient manager, ApiClient.RequestCallback callback) {
        // Encrypting to newInstance request
        final String encryptedClientData = encryption.apply(formattedUsrData);
        final String requestData = buildRequest( DELINK_RT,
                requestST.getValue(),
                encryptedClientData
        );

        IApiEndpoint iCaller = manager.create( IApiEndpoint.class );
        Call<ServerResponse> request = iCaller.deLink( requestData );
        manager.sendXMLRequest( request, callback );
    }
}
