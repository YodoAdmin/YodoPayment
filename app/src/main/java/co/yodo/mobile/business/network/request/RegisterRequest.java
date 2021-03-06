package co.yodo.mobile.business.network.request;

import java.util.HashMap;
import java.util.Map;

import javax.crypto.spec.SecretKeySpec;

import co.yodo.mobile.YodoApplication;
import co.yodo.mobile.business.component.cipher.AESCrypt;
import co.yodo.mobile.business.component.cipher.RSACrypt;
import co.yodo.mobile.helper.AppConfig;
import co.yodo.mobile.business.network.ApiClient;
import co.yodo.mobile.business.network.model.ServerResponse;
import co.yodo.mobile.business.network.request.contract.IRequest;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Url;

/**
 * Created by hei on 12/06/16.
 * Request a register of a secondary account to the server
 */
public class RegisterRequest extends IRequest {
    /** Register request type */
    private static final String REG_RT = "9";

    /** Register sub-types */
    public enum RegST {
        CLIENT    ( "0" ),
        BIOMETRIC ( "3" ),
        GCM       ( "4" );

        private final String value;

        RegST( String value ) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }
    }

    /** Type for gcm (1: Android, 2: iOS) */
    private static final String DEV_TYPE = "1";

    /** Data for the request */
    private final String userIdentifier;
    private final String token;

    /** Sub-type of the request */
    private final RegST requestST;

    /** Interface for the REG requests */
    interface IApiEndpoint {
        @GET( YODO_ADDRESS + "{request}" )
        Call<ServerResponse> register( @Path( "request" ) String request );

        @FormUrlEncoded
        @POST
        Call<ResponseBody> register( @Url String url,
                @Field( "prt" ) String prt,
                @Field( "req" ) String req,
                @Field( "par" ) String par
        );
    }

    /**
     * Registers a new user with his/her pip
     * @param userIdentifier The hardware token of the device, or the authnumber
     * @param token The password of the user, biometric token or the gcm id
     * @param requestST The sub-type of the request
     */
    public RegisterRequest( String userIdentifier, String token, RegST requestST ) {
        this.userIdentifier = userIdentifier;
        this.token = token;
        this.requestST = requestST;
    }

    /**
     * The default constructor, which automatically creates a client register request
     * @param userIdentifier The hardware token of the device, or the authnumber
     * @param token The password of the user, biometric token or the gcm id
     */
    public RegisterRequest( String userIdentifier, String token ) {
        this( userIdentifier, token, RegST.CLIENT );
    }

    /**
     * Creates an registration switch request
     * @param sUsrData	Encrypted user's data
     * @param iRegST Sub-type of the request
     * @return Map Request for getting the registration code
     */
    private static Map<String, String> createJSONRequest( String sUsrData, String iRegST ) {
        Map<String, String> params = new HashMap<>();
        // The POST parameters:
        params.put( "prt", PROTOCOL_VERSION );
        params.put( "req", REG_RT + REQ_SEP + iRegST );
        params.put( "par", sUsrData );

        return params;
    }

    @Override
    public void execute( RSACrypt cipher, ApiClient manager, ApiClient.RequestCallback callback ) {
        String encryptedClientData, requestData;
        SecretKeySpec key = AESCrypt.generateKey();
        encyptedKey = cipher.encrypt( AESCrypt.encodeKey( key ) );

        switch( this.requestST ) {
            case CLIENT:
                this.formattedUsrData =
                        AppConfig.YODO_BIOMETRIC + USR_SEP +
                        this.token + USR_SEP +
                        this.userIdentifier + USR_SEP +
                        System.currentTimeMillis() / 1000L;

                encyptedData = AESCrypt.encrypt( formattedUsrData, key );

                encryptedClientData =
                        encyptedKey + REQ_SEP +
                                encyptedData;

                requestData = buildRequest( REG_RT,
                        requestST.getValue(),
                        encryptedClientData
                );

                IApiEndpoint iCaller = manager.create( IApiEndpoint.class );
                Call<ServerResponse> request = iCaller.register( requestData );
                manager.sendXMLRequest( request, callback );
                break;

            case BIOMETRIC:
                this.formattedUsrData =
                        this.userIdentifier + REQ_SEP +
                        this.token;

                requestData = buildRequest( REG_RT,
                        requestST.getValue(),
                        formattedUsrData
                );

                iCaller = manager.create( IApiEndpoint.class );
                request = iCaller.register( requestData );
                manager.sendXMLRequest( request, callback );
                break;

            case GCM:
                encryptedClientData = cipher.encrypt( userIdentifier );

                this.formattedUsrData =
                        encryptedClientData + REQ_SEP +
                        this.token + REQ_SEP +
                        DEV_TYPE;

                iCaller = manager.create( IApiEndpoint.class );
                Call<ResponseBody> jsonRequest = iCaller.register(
                        YodoApplication.IP + ":8081/yodo",
                        PROTOCOL_VERSION,
                        REG_RT + REQ_SEP + requestST.getValue(),
                        formattedUsrData
                );
                manager.sendJSONRequest( jsonRequest, callback );
                break;

            default:
                throw new IllegalArgumentException( "type no supported" );
        }
    }
}
