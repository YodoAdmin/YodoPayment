package co.yodo.mobile.network.request;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.spec.SecretKeySpec;

import co.yodo.mobile.YodoApplication;
import co.yodo.mobile.component.cipher.AESCrypt;
import co.yodo.mobile.component.cipher.RSACrypt;
import co.yodo.mobile.helper.AppConfig;
import co.yodo.mobile.helper.SystemUtils;
import co.yodo.mobile.network.ApiClient;
import co.yodo.mobile.network.model.ServerResponse;
import co.yodo.mobile.network.request.contract.IRequest;
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
    /** DEBUG */
    @SuppressWarnings( "unused" )
    private static final String TAG = RegisterRequest.class.getSimpleName();

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
    private final String mUserIdentifier;
    private final String mToken;

    /** Sub-type of the request */
    private final RegST mRequestST;

    /** Interface for the REG requests */
    interface IApiEndpoint {
        @GET( YODO_ADDRESS + "{request}" )
        Call<ServerResponse> register( @Path( "request" ) String request );

        @FormUrlEncoded
        @POST
        Call<ResponseBody> register(
                @Url String url,
                @Field( "prt" ) String prt,
                @Field( "req" ) String req,
                @Field( "par" ) String par
        );
    }

    /**
     * Registers a new user with his/her pip
     * @param responseCode The code used to respond the caller activity
     * @param userIdentifier The hardware token of the device, or the authnumber
     * @param token The password of the user, biometric token or the gcm id
     * @param requestST The sub-type of the request
     */
    public RegisterRequest( int responseCode, String userIdentifier, String token, RegST requestST ) {
        super( responseCode );
        this.mUserIdentifier = userIdentifier;
        this.mToken = token;
        this.mRequestST = requestST;
    }

    /**
     * The default constructor, which automatically creates a client register request
     * @param responseCode The code used to respond the caller activity
     * @param userIdentifier The hardware token of the device, or the authnumber
     * @param token The password of the user, biometric token or the gcm id
     */
    public RegisterRequest( int responseCode, String userIdentifier, String token ) {
        this( responseCode, userIdentifier, token, RegST.CLIENT );
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
    public void execute( RSACrypt oEncrypter, ApiClient oManager ) {
        String sEncryptedClientData, pRequest;

        SecretKeySpec key = AESCrypt.generateKey();

        //mEncyptedSignature = MessageIntegrityAttribute.encode( mFormattedUsrData, key );
        mEncyptedKey = oEncrypter.encrypt( AESCrypt.encodeKey( key ) );

        switch( this.mRequestST ) {
            case CLIENT:
                this.mFormattedUsrData =
                        AppConfig.YODO_BIOMETRIC + USR_SEP +
                        this.mToken + USR_SEP +
                        this.mUserIdentifier + USR_SEP +
                        System.currentTimeMillis() / 1000L;

                mEncyptedData = AESCrypt.encrypt( mFormattedUsrData, key );

                sEncryptedClientData =
                        mEncyptedKey + REQ_SEP +
                        mEncyptedData;

                pRequest = buildRequest( REG_RT,
                        mRequestST.getValue(),
                        sEncryptedClientData
                );

                IApiEndpoint iCaller = oManager.create( IApiEndpoint.class );
                Call<ServerResponse> sResponse = iCaller.register( pRequest );
                oManager.sendRequest( sResponse, mResponseCode );
                break;

            case BIOMETRIC:
                this.mFormattedUsrData =
                        this.mUserIdentifier + REQ_SEP +
                        this.mToken;

                pRequest = buildRequest( REG_RT,
                        mRequestST.getValue(),
                        mFormattedUsrData
                );

                iCaller = oManager.create( IApiEndpoint.class );
                sResponse = iCaller.register( pRequest );
                oManager.sendRequest( sResponse, mResponseCode );
                break;

            case GCM:
                sEncryptedClientData = oEncrypter.encrypt( mUserIdentifier );

                this.mFormattedUsrData =
                        sEncryptedClientData + REQ_SEP +
                        this.mToken + REQ_SEP +
                        DEV_TYPE;

                Map<String, String> params = createJSONRequest(
                        this.mFormattedUsrData,
                        this.mRequestST.getValue()
                );

                SystemUtils.iLogger( TAG, Collections.singletonList( params ).toString() );

                iCaller = oManager.create( IApiEndpoint.class );
                Call<ResponseBody> gResponse = iCaller.register(
                        YodoApplication.IP + ":8081/yodo",
                        PROTOCOL_VERSION,
                        REG_RT + REQ_SEP + mRequestST.getValue(),
                        mFormattedUsrData
                );
                oManager.sendJsonRequest( gResponse, mResponseCode );
                break;

            default:
                throw new IllegalArgumentException( "type no supported" );
        }
    }
}
