package co.yodo.mobile.business.network.request;

import co.yodo.mobile.business.network.ApiClient;
import co.yodo.mobile.business.network.encryption.IEncryption;
import co.yodo.mobile.business.network.model.ServerResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by hei on 12/06/16.
 * Request a close account from the server
 */
public class CloseRequest extends IRequest {
    /** Close request type */
    private static final String CLOSE_RT = "8";

    /** Close sub-types */
    private enum CloseST {
        CLIENT ("1");

        private final String value;

        CloseST(String value) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }
    }

    /** Sub-type of the request */
    private final CloseST requestST;

    /** Interface for the CLOSE requests */
    interface IApiEndpoint {
        @GET(YODO_ADDRESS + "{request}")
        Call<ServerResponse> closeAcc(@Path("request") String request);
    }

    /**
     * Request to close a user account
     * @param hardwareToken The hardware token of the device
     * @param otp The one time password of the user
     */
    public CloseRequest(String hardwareToken, String otp) {
        this.formattedUsrData =
                otp + USR_SEP +
                hardwareToken + USR_SEP +
                System.currentTimeMillis() / 1000L + REQ_SEP +
                "0" + REQ_SEP + "0"; // Mock GPS, not needed any more
        this.requestST = CloseST.CLIENT;
    }

    @Override
    public void execute(IEncryption encryption, ApiClient manager, ApiClient.RequestCallback callback) {
        // Encrypting to newInstance request
        final String encryptedClientData = encryption.apply(formattedUsrData);
        final String requestData = buildRequest( CLOSE_RT,
                requestST.getValue(),
                encryptedClientData
        );

        IApiEndpoint iCaller = manager.create(IApiEndpoint.class);
        Call<ServerResponse> request = iCaller.closeAcc(requestData);
        manager.sendXMLRequest(request, callback);
    }
}