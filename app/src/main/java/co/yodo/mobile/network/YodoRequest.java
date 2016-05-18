package co.yodo.mobile.network;

import android.annotation.SuppressLint;
import android.content.Context;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import co.yodo.mobile.R;
import co.yodo.mobile.component.Encrypter;
import co.yodo.mobile.network.model.ServerResponse;
import co.yodo.mobile.helper.AppConfig;
import co.yodo.mobile.helper.AppUtils;
import co.yodo.mobile.network.builder.ServerRequest;
import co.yodo.mobile.network.handler.XMLHandler;

/**
 * Created by luis on 15/12/14.
 * Request to the Yodo Server
 */
@SuppressLint( "ParcelCreator" )
public class YodoRequest {
    /** DEBUG */
    private static final String TAG = YodoRequest.class.getSimpleName();

    /** Switch server IP address */
    private static final String PROD_IP      = "http://50.56.180.133";  // Production
    private static final String DEV_IP   	 = "http://198.101.209.120";  // Development
    private static final String IP           = PROD_IP;

    /** Two paths used for the requests */
    private static final String YODO_ADDRESS = "/yodo/yodoswitchrequest/getRequest/";

    /** Timeout for the requests */
    private final static int TIMEOUT = 1000 * 10; // 10 seconds

    private RetryPolicy retryPolicy = new DefaultRetryPolicy(
            TIMEOUT,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
    );

    /** Object used to encrypt information */
    private Encrypter oEncrypter;

    /** Global request queue for Volley */
    private RequestQueue mRequestQueue = null;

    /** Singleton instance */
    private static YodoRequest instance = null;

    /** The external listener to the service */
    private RESTListener listener;

    /** Context of the application */
    private static Context mCtx;

    /** User's data separator */
    private static final String	USR_SEP     = "**";
    private static final String	REQ_SEP     = ",";
    private static final String	PCLIENT_SEP = "/";

    public interface RESTListener {
        /**
         * Listener for the server responses
         * @param type Type of the request
         * @param response POJO for the response
         */
        void onResponse( RequestType type, ServerResponse response );
    }

    /** ID for each request */
    public enum RequestType {
        ERROR_NO_INTERNET     ( "-1" ), // ERROR NO INTERNET
        ERROR_GENERAL         ( "00" ), // ERROR GENERAL
        AUTH_REQUEST          ( "01" ), // RT=0, ST=1
        AUTH_PIP_REQUEST      ( "02" ), // RT=0, ST=2
        RESET_PIP_REQUEST     ( "03" ), // RT=3, ST=1
        RESET_BIO_PIP_REQUEST ( "04" ), // RT=3, ST=2
        QUERY_BAL_REQUEST     ( "05" ), // RT=4, ST=1
        QUERY_BIO_REQUEST     ( "06" ), // RT=4, ST=3
        QUERY_ADV_REQUEST     ( "07" ), // RT=4, ST=3
        QUERY_RCV_REQUEST     ( "08" ), // RT=4, ST=3
        QUERY_LNK_REQUEST     ( "09" ), // RT=4, ST=3
        QUERY_LNK_ACC_REQUEST ( "10" ), // RT=4, ST=3
        CLOSE_ACC_REQUEST     ( "11" ), // RT=8, ST=1
        REG_CLIENT_REQUEST    ( "12 "), // RT=9, ST=0
        REG_BIO_REQUEST       ( "13 "), // RT=9, ST=3
        REG_GCM_REQUEST       ( "14 "), // RT=9, ST=4
        LINK_ACC_REQUEST      ( "15 "), // RT=10, ST=0
        DELINK_ACC_REQUEST    ( "16 "); // RT=11

        private final String name;

        RequestType( String s ) {
            name = s;
        }

        public String toString() {
            return name;
        }
    }

    /**
     * Private constructor for the singleton
     * @param context The application context
     */
    private YodoRequest( Context context )  {
        mCtx = context.getApplicationContext();
        mRequestQueue = getRequestQueue();
        oEncrypter = new Encrypter();
    }

    /**
     * Gets the instance of the service
     * @return instance
     */
    public static synchronized YodoRequest getInstance( Context context ) {
        if( instance == null )
            instance = new YodoRequest( context );
        return instance;
    }

    /**
     * Returns the request queue for other queries
     * @return The static volley request queue
     */
    public RequestQueue getRequestQueue() {
        if( mRequestQueue == null ) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            mRequestQueue = Volley.newRequestQueue( mCtx );
        }
        return mRequestQueue;
    }

    /**
     * Add a listener to the service
     * @param listener Listener for the requests to the server
     */
    public void setListener( RESTListener listener ) {
        this.listener = listener ;
    }

    /**
     * Returns an string that represents the server of the IP
     * @return P - production
     *         D - development
     */
    public static String getSwitch() {
        return ( IP.equals( PROD_IP ) ) ? "P" : "D";
    }

    private void sendXMLRequest( final String pRequest, final RequestType responseCode ) {
        if( listener == null )
            throw new NullPointerException( "Listener not defined" );

        final StringRequest httpRequest = new StringRequest( Request.Method.GET, IP + YODO_ADDRESS + pRequest,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse( String xml ) {
                        try {
                            // Handling XML
                            SAXParserFactory spf = SAXParserFactory.newInstance();
                            SAXParser sp = spf.newSAXParser();
                            XMLReader xr = sp.getXMLReader();

                            // Create handler to handle XML Tags ( extends DefaultHandler )
                            xr.setContentHandler( new XMLHandler() );
                            xr.parse( new InputSource( new StringReader( xml ) ) );

                            // Sends the response to the listener
                            AppUtils.Logger( TAG, XMLHandler.response.toString() );
                            listener.onResponse( responseCode, XMLHandler.response );
                        } catch( ParserConfigurationException | SAXException | IOException e ) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse( VolleyError error ) {
                        error.printStackTrace();
                        // depending on the error, return an alert to the activity
                        ServerResponse response = new ServerResponse();
                        if( error instanceof TimeoutError  ) {
                            response.setCode( ServerResponse.ERROR_TIMEOUT );
                            response.setMessage( mCtx.getString( R.string.message_error_timeout ) );
                        } else if( error instanceof NetworkError ) {
                            response.setCode( ServerResponse.ERROR_NETWORK );
                            response.setMessage( mCtx.getString( R.string.message_error_network ) );
                        } else if( error instanceof ServerError ) {
                            response.setCode( ServerResponse.ERROR_SERVER );
                            response.setMessage( mCtx.getString( R.string.message_error_server ) );
                        } else {
                            response.setCode( ServerResponse.ERROR_UNKOWN );
                            response.setMessage( mCtx.getString( R.string.message_error_unknown ) );
                        }
                        listener.onResponse( responseCode, response );
                    }
                }
        );
        httpRequest.setTag( "GET" );
        httpRequest.setRetryPolicy( retryPolicy );
        getRequestQueue().add( httpRequest );
    }

    /**
     * Queries directed to the server
     * {{ ======================================================================
     */

    /**
     * Authenticates the client using the hardware token
     * @param hardwareToken The hardware token - client identifier
     */
    public void requestClientAuth( String hardwareToken ) {
        String sEncryptedClientData, pRequest;

        // Encrypting to create request
        oEncrypter.setsUnEncryptedString( hardwareToken );
        oEncrypter.rsaEncrypt( mCtx );
        sEncryptedClientData = oEncrypter.bytesToHex();

        pRequest = ServerRequest.createAuthenticationRequest(
                sEncryptedClientData,
                ServerRequest.AUTH_HW_ST
        );

        sendXMLRequest( pRequest, RequestType.AUTH_REQUEST );
    }

    /**
     * Authenticates the client using the hardware token and PIP
     * @param hardwareToken The hardware token - client identifier
     * @param pip The PIP (password) used to authenticate the client
     */
    public void requestClientAuth( String hardwareToken, String pip ) {
        String sEncryptedClientData, pRequest;

        // Encrypting to create request
        oEncrypter.setsUnEncryptedString(
                hardwareToken + PCLIENT_SEP +
                pip + PCLIENT_SEP +
                System.currentTimeMillis() / 1000L
        );
        oEncrypter.rsaEncrypt( mCtx );
        sEncryptedClientData = oEncrypter.bytesToHex();

        pRequest = ServerRequest.createAuthenticationRequest(
                sEncryptedClientData,
                ServerRequest.AUTH_HW_PIP_ST
        );

        sendXMLRequest( pRequest, RequestType.AUTH_PIP_REQUEST );
    }

    /**
     * Changes the user PIP for a new one
     * @param hardwareToken The hardware token - client identifier
     * @param pip The PIP (password) used to authenticate the client
     * @param newPip The new PIP which will be set
     */
    public void requestPIPReset( String hardwareToken, String pip, String newPip ) {
        String sEncryptedClientData, sEncryptedPIP, sEncryptedNewPIP, pRequest;
        StringBuilder sClientData = new StringBuilder();

        // Encrypting to create request
        oEncrypter.setsUnEncryptedString( hardwareToken );
        oEncrypter.rsaEncrypt( mCtx );
        sEncryptedClientData = oEncrypter.bytesToHex();

        oEncrypter.setsUnEncryptedString( pip );
        oEncrypter.rsaEncrypt( mCtx );
        sEncryptedPIP = oEncrypter.bytesToHex();

        oEncrypter.setsUnEncryptedString( newPip );
        oEncrypter.rsaEncrypt( mCtx );
        sEncryptedNewPIP = oEncrypter.bytesToHex();

        sClientData.append( sEncryptedClientData ).append( REQ_SEP );
        sClientData.append( sEncryptedPIP ).append( REQ_SEP );
        sClientData.append( sEncryptedNewPIP );

        pRequest = ServerRequest.createResetRequest(
                sClientData.toString(),
                ServerRequest.RESET_PIP_ST
        );

        sendXMLRequest( pRequest, RequestType.RESET_PIP_REQUEST );
    }

    /**
     * Change the PIP with an authnumber used in the biometric
     * authentication
     * @param authNumber The authentication number of the biometric query
     * @param hardwareToken The hardware token - client identifier
     * @param newPip The new PIP which will be set
     */
    public void requestBiometricPIPReset( String authNumber, String hardwareToken, String newPip ) {
        String sEncryptedClientData, pRequest;

        // Encrypting to create request
        oEncrypter.setsUnEncryptedString(
                authNumber + REQ_SEP +
                hardwareToken + REQ_SEP +
                newPip
        );
        oEncrypter.rsaEncrypt( mCtx );
        sEncryptedClientData = oEncrypter.bytesToHex();

        pRequest = ServerRequest.createResetRequest(
                sEncryptedClientData,
                ServerRequest.RESET_BIO_PIP_ST
        );

        sendXMLRequest( pRequest, RequestType.RESET_BIO_PIP_REQUEST );
    }

    /**
     * Request the user balance
     * @param hardwareToken The hardware token - client identifier
     * @param pip The PIP (password) used to authenticate the client
     */
    public void requestBalance( String hardwareToken, String pip ) {
        String sEncryptedClientData, pRequest;

        // Encrypting to create request
        oEncrypter.setsUnEncryptedString(
                hardwareToken + PCLIENT_SEP +
                pip + PCLIENT_SEP +
                System.currentTimeMillis() / 1000L );
        oEncrypter.rsaEncrypt( mCtx );
        sEncryptedClientData = oEncrypter.bytesToHex();

        pRequest = ServerRequest.createQueryRequest(
                sEncryptedClientData,
                ServerRequest.QUERY_BAL_ST
        );

        sendXMLRequest( pRequest, RequestType.QUERY_BAL_REQUEST );
    }

    /**
     * Requests an advertisement image for a respective merchant
     * @param hardwareToken The hardware token - client identifier
     * @param merchant The name of the merchant (username)
     */
    public void requestAdvertising( String hardwareToken, String merchant ) {
        String sEncryptedClientData, pRequest;

        // Encrypting to create request
        oEncrypter.setsUnEncryptedString(
                hardwareToken + REQ_SEP +
                merchant + REQ_SEP +
                ServerRequest.QueryRecord.ADVERTISING.getValue()
        );
        oEncrypter.rsaEncrypt( mCtx );
        sEncryptedClientData = oEncrypter.bytesToHex();

        pRequest = ServerRequest.createQueryRequest(
                sEncryptedClientData,
                ServerRequest.QUERY_ACC_ST
        );

        sendXMLRequest( pRequest, RequestType.QUERY_ADV_REQUEST );
    }

    /**
     * Request Biometric token from the server
     * @param hardwareToken The hardware token - client identifier
     */
    public void requestBiometricToken( String hardwareToken ) {
        String sEncryptedClientData, pRequest;

        // Encrypting to create request
        oEncrypter.setsUnEncryptedString(
                hardwareToken + REQ_SEP +
                ServerRequest.QueryRecord.BIOMETRIC.getValue()
        );
        oEncrypter.rsaEncrypt( mCtx );
        sEncryptedClientData = oEncrypter.bytesToHex();

        pRequest = ServerRequest.createQueryRequest(
                sEncryptedClientData,
                ServerRequest.QUERY_ACC_ST
        );

        sendXMLRequest( pRequest, RequestType.QUERY_BIO_REQUEST );
    }

    /**
     * Requests a new linking code
     * @param hardwareToken The hardware token - client identifier
     * @param pip The PIP (password) used to authenticate the client
     */
    public void requestLinkingCode( String hardwareToken, String pip ) {
        String sEncryptedClientData, pRequest;

        // Encrypting to create request
        oEncrypter.setsUnEncryptedString(
                hardwareToken + REQ_SEP +
                pip + REQ_SEP +
                ServerRequest.QueryRecord.LINKING_CODE.getValue()
        );
        oEncrypter.rsaEncrypt( mCtx );
        sEncryptedClientData = oEncrypter.bytesToHex();

        pRequest = ServerRequest.createQueryRequest(
                sEncryptedClientData,
                ServerRequest.QUERY_ACC_ST
        );

        sendXMLRequest( pRequest, RequestType.QUERY_LNK_REQUEST );
    }

    /**
     * Requests the current linked accounts
     * @param hardwareToken The hardware token - client identifier
     * @param pip The PIP (password) used to authenticate the client
     */
    public void requestLinkedAccounts( String hardwareToken, String pip ) {
        String sEncryptedClientData, pRequest;

        // Encrypting to create request
        oEncrypter.setsUnEncryptedString(
                hardwareToken + REQ_SEP +
                pip + REQ_SEP +
                ServerRequest.QueryRecord.LINKED_ACCOUNTS.getValue()
        );
        oEncrypter.rsaEncrypt( mCtx );
        sEncryptedClientData = oEncrypter.bytesToHex();

        pRequest = ServerRequest.createQueryRequest(
                sEncryptedClientData,
                ServerRequest.QUERY_ACC_ST
        );

        sendXMLRequest( pRequest, RequestType.QUERY_LNK_ACC_REQUEST );
    }

    /**
     * Closes a secondary client account
     * @param hardwareToken The hardware token - client identifier
     * @param pip The PIP (password) used to authenticate the client
     */
    public void requestCloseAccount( String hardwareToken, String pip ) {
        String sEncryptedClientData, pRequest;

        // Encrypting to create request
        oEncrypter.setsUnEncryptedString(
                pip + USR_SEP +
                hardwareToken + USR_SEP +
                System.currentTimeMillis() / 1000L + REQ_SEP +
                "0" + REQ_SEP +
                "0"
        );
        oEncrypter.rsaEncrypt( mCtx );
        sEncryptedClientData = oEncrypter.bytesToHex();

        pRequest = ServerRequest.createCloseRequest(
                sEncryptedClientData
        );

        sendXMLRequest( pRequest, RequestType.CLOSE_ACC_REQUEST );
    }

    /**
     * Registers a new client
     * @param hardwareToken The hardware token - client identifier
     * @param pip The PIP which will be set
     */
    public void requestRegistration( String hardwareToken, String pip ) {
        String sEncryptedClientData, pRequest;

        // Encrypting to create request
        oEncrypter.setsUnEncryptedString(
                AppConfig.YODO_BIOMETRIC + USR_SEP +
                pip + USR_SEP +
                hardwareToken + USR_SEP +
                System.currentTimeMillis() / 1000L
        );
        oEncrypter.rsaEncrypt( mCtx );
        sEncryptedClientData = oEncrypter.bytesToHex();

        pRequest = ServerRequest.createRegistrationRequest(
                sEncryptedClientData,
                ServerRequest.REG_CLIENT_ST
        );

        sendXMLRequest( pRequest, RequestType.REG_CLIENT_REQUEST );
    }

    /**
     * Registers the biometric token
     * @param authNumber The authnumber of the registration
     * @param token The biometric token
     */
    public void requestBiometricRegistration( String authNumber, String token ) {
        String pRequest = ServerRequest.createRegistrationRequest(
                authNumber + REQ_SEP + token,
                ServerRequest.REG_BIOMETRIC_ST
        );

        sendXMLRequest( pRequest, RequestType.REG_BIO_REQUEST );
    }

    public void requestGCMRegistration( Context activity, final String hardwareToken, final String token ) throws IOException {
        // Encrypting to create request
        oEncrypter.setsUnEncryptedString( hardwareToken );
        oEncrypter.rsaEncrypt( activity );
        final String sEncryptedClientData = oEncrypter.bytesToHex();

        StringRequest postRequest = new StringRequest( Request.Method.POST, IP + ":8081/yodo",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse( String json ) {
                        try {
                            ServerResponse response = new ServerResponse();
                            JSONObject jsonResponse = new JSONObject( json );

                            response.setCode( jsonResponse.getString( "respCode" ) );
                            response.setAuthNumber( jsonResponse.getString( "authCode" ) );
                            response.setMessage( jsonResponse.getString( "msg" ) );
                            response.setRTime( jsonResponse.getLong( "respTime" ) );

                            listener.onResponse( RequestType.REG_GCM_REQUEST, response );
                            AppUtils.Logger( TAG, response.toString() );
                        } catch( JSONException e ) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse( VolleyError error ) {
                        error.printStackTrace();
                        listener.onResponse( RequestType.ERROR_GENERAL, null );
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                // the POST parameters:
                params.put( "prt", "1.1.2" );
                params.put( "req", "9,4" );
                params.put( "par", sEncryptedClientData + REQ_SEP + token + REQ_SEP + AppConfig.DEV_TYPE );
                return params;
            }
        };
        postRequest.setTag( "POST" );
        Volley.newRequestQueue( activity ).add( postRequest );
    }

    /**
     * Requests a linking of accounts using a link code
     * @param hardwareToken The hardware token - client identifier
     * @param linkCode A code used to link two accounts
     */
    public void requestLinkAccount( String hardwareToken, String linkCode ) {
        String sEncryptedClientData, pRequest;

        // Encrypting to create request
        oEncrypter.setsUnEncryptedString(
                hardwareToken + REQ_SEP +
                linkCode + REQ_SEP +
                System.currentTimeMillis() / 1000L
        );
        oEncrypter.rsaEncrypt( mCtx );
        sEncryptedClientData = oEncrypter.bytesToHex();

        pRequest = ServerRequest.createLinkingRequest(
                sEncryptedClientData,
                ServerRequest.LINK_ACC_ST
        );

        sendXMLRequest( pRequest, RequestType.LINK_ACC_REQUEST );
    }

    public void requestDeLinkAccount( String hardwareToken, String pip, String linkedAccount, String accountType ) {
        String sEncryptedClientData, pRequest;

        // Encrypting to create request
        oEncrypter.setsUnEncryptedString(
                hardwareToken + REQ_SEP +
                pip + REQ_SEP +
                linkedAccount
        );
        oEncrypter.rsaEncrypt( mCtx );
        sEncryptedClientData = oEncrypter.bytesToHex();

        pRequest = ServerRequest.createDeLinkRequest(
                sEncryptedClientData,
                accountType
        );

        sendXMLRequest( pRequest, RequestType.DELINK_ACC_REQUEST );
    }
}
