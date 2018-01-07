package co.yodo.mobile.business.network.request;

import co.yodo.mobile.business.network.ApiClient;
import co.yodo.mobile.business.network.encryption.IEncryption;
import co.yodo.mobile.business.network.model.ServerResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by hei on 12/06/16.
 * Request data from the server
 */
public class QueryRequest extends IRequest {
    /** Query request type */
    private static final String QUERY_RT = "4";

    /** Query sub-types */
    private enum QueryST {
        BAL ("1"),
        ACC ("3");

        private final String value;

        QueryST(String value) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }
    }

    /** Query Records - QUERY_ACC_ST */
    public enum Record {
        ADVERTISING     (22),
        BIOMETRIC       (24),
        LINKING_CODE    (25),
        LINKED_ACCOUNTS (26);

        private final int value;

        Record(int i) {
            value = i;
        }

        public int getValue() {
            return value;
        }
    }

    /** Sub-type of the request */
    private final QueryST requestST;

    /** Interface for the QUERY requests */
    interface IApiEndpoint {
        @GET(YODO_ADDRESS + "{request}")
        Call<ServerResponse> query(@Path("request") String request);
    }

    /**
     * Request the user's balance
     * @param hardwareToken The hardware token of the device
     * @param pip The password of the user
     */
    public QueryRequest(String hardwareToken, String pip) {
        final long currentTime = System.currentTimeMillis() / 1000L;
        this.formattedUsrData = hardwareToken + PCLIENT_SEP + pip + PCLIENT_SEP + currentTime;
        this.requestST = QueryST.BAL;
    }

    /**
     * Request data from the server using only the hardware token and the record of the data
     * @param hardwareToken The hardware token of the device
     * @param record A value that describes the type of the data
     */
    public QueryRequest(String hardwareToken, Record record) {
        this.formattedUsrData = hardwareToken + REQ_SEP + record.getValue();
        this.requestST = QueryST.ACC;
    }

    /**
     * Request data from the server using only the hardware token and the record of the data
     * @param hardwareToken The hardware token of the device
     * @param other The user password or sometimes another identifier like the merchant name
     * @param record A value that describes the type of the data
     */
    public QueryRequest(String hardwareToken, String other, Record record) {
        this.formattedUsrData = hardwareToken + REQ_SEP + other + REQ_SEP + record.getValue();
        this.requestST = QueryST.ACC;
    }

    @Override
    public void execute(IEncryption encryption, ApiClient manager, ApiClient.RequestCallback callback) {
        // Encrypting to newInstance request
        final String encryptedClientData = encryption.apply(formattedUsrData);
        final String requestData = buildRequest(QUERY_RT,
                requestST.getValue(),
                encryptedClientData
        );

        IApiEndpoint iCaller = manager.create(IApiEndpoint.class);
        Call<ServerResponse> request = iCaller.query(requestData);
        manager.sendXMLRequest(request, callback);
    }
}
