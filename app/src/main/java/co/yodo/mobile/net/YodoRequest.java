package co.yodo.mobile.net;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.text.format.Time;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
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
import co.yodo.mobile.data.ServerResponse;
import co.yodo.mobile.helper.AppConfig;
import co.yodo.mobile.helper.AppUtils;
import co.yodo.mobile.service.RESTService;

/**
 * Created by luis on 15/12/14.
 * Request to the Yodo Server
 */
@SuppressLint( "ParcelCreator" )
public class YodoRequest extends ResultReceiver {
    /** DEBUG */
    private static final String TAG = YodoRequest.class.getSimpleName();

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

        RequestType(String s) {
            name = s;
        }

        public String toString() {
            return name;
        }
    }

    /** ID for the types of progress dialog */
    public enum ProgressDialogType {
        NORMAL,
        TRANSPARENT
    }

    /** Object used to encrypt information */
    private Encrypter oEncrypter;

    /** Progress dialog */
    private ProgressDialog progressDialog;

    /** Singleton instance */
    private static YodoRequest instance = null;

    /** the external listener to the service */
    private RESTListener externalListener;

    /** User's data separator */
    private static final String	USR_SEP     = "**";
    private static final String	REQ_SEP     = ",";
    private static final String	PCLIENT_SEP = "/";

    /**
     * Create a new ResultReceive to receive results.  Your
     * {@link #onReceiveResult} method will be called from the thread running
     * <var>handler</var> if given, or from an arbitrary thread if null.
     *
     * @param handler Default
     */
    private YodoRequest( Handler handler )  {
        super( handler );
        oEncrypter = new Encrypter();
    }

    /**
     * Gets the instance of the service
     * @return instance
     */
    public static YodoRequest getInstance() {
        if( instance == null )
            instance = new YodoRequest( new Handler() );
        return instance;
    }

    /**
     * Add a listener to the service
     * @param listener Listener for the requests to the server
     */
    public void setListener( RESTListener listener ) {
        externalListener = listener ;
    }

    public void createProgressDialog(Context context, ProgressDialogType type) {
        switch( type ) {
            case NORMAL:
                progressDialog = new ProgressDialog( context, R.style.TransparentProgressDialog );
                progressDialog.setCancelable( false );
                progressDialog.show();
                progressDialog.setContentView( R.layout.custom_progressdialog );
                break;
        }
    }

    public boolean progressDialogShowing() {
        return progressDialog != null && progressDialog.isShowing();
    }

    public void destroyProgressDialog() {
        if( progressDialog != null && progressDialog.isShowing() ) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    public void requestAuthentication( Context context, String hardwareToken ) {
        String sEncryptedClientData, pRequest;

        // Encrypting to create request
        oEncrypter.setsUnEncryptedString( hardwareToken );
        oEncrypter.rsaEncrypt( context );
        sEncryptedClientData = oEncrypter.bytesToHex();

        pRequest = ServerRequest.createAuthenticationRequest(
                sEncryptedClientData,
                Integer.parseInt( ServerRequest.AUTH_HW_SUBREQ )
        );

        sendRequest( context, pRequest, RequestType.AUTH_REQUEST );
    }

    public void requestPIPAuthentication( Activity activity, String hardwareToken, String pip ) {
        String sEncryptedClientData, pRequest;

        // Encrypting to create request
        oEncrypter.setsUnEncryptedString(
                hardwareToken + PCLIENT_SEP +
                pip + PCLIENT_SEP +
                System.currentTimeMillis() / 1000L
        );
        oEncrypter.rsaEncrypt( activity );
        sEncryptedClientData = oEncrypter.bytesToHex();

        pRequest = ServerRequest.createAuthenticationRequest(
                sEncryptedClientData,
                Integer.parseInt( ServerRequest.AUTH_HW_PIP_SUBREQ )
        );

        sendRequest( activity, pRequest, RequestType.AUTH_PIP_REQUEST );

        /*Intent intent = new Intent( activity, RESTService.class );
        intent.putExtra( RESTService.ACTION_RESULT, RequestType.AUTH_PIP_REQUEST );
        intent.putExtra( RESTService.EXTRA_PARAMS, pRequest );
        intent.putExtra( RESTService.EXTRA_RESULT_RECEIVER, instance );
        activity.startService( intent );*/
    }

    public void requestRegistration( Activity activity, String hardwareToken, String pip ) {
        String sEncryptedClientData, pRequest;
        StringBuilder sClientData = new StringBuilder();

        String timeStamp = String.valueOf( System.currentTimeMillis() );

        sClientData.append( AppConfig.YODO_BIOMETRIC ).append( USR_SEP );
        sClientData.append( pip ).append( USR_SEP );
        sClientData.append( hardwareToken ).append( USR_SEP );
        sClientData.append( timeStamp );

        // Encrypting to create request
        oEncrypter.setsUnEncryptedString( sClientData.toString() );
        oEncrypter.rsaEncrypt( activity );
        sEncryptedClientData = oEncrypter.bytesToHex();

        pRequest = ServerRequest.createRegistrationRequest(
                sEncryptedClientData,
                Integer.parseInt( ServerRequest.REG_CLIENT_SUBREQ )
        );

        Intent intent = new Intent( activity, RESTService.class );
        intent.putExtra( RESTService.ACTION_RESULT, RequestType.REG_CLIENT_REQUEST );
        intent.putExtra( RESTService.EXTRA_PARAMS, pRequest );
        intent.putExtra( RESTService.EXTRA_RESULT_RECEIVER, instance );
        activity.startService( intent );
    }

    public void requestBiometricRegistration( Activity activity, String authNumber, String token ) {
        String pRequest = ServerRequest.createRegistrationRequest(
                authNumber + REQ_SEP + token,
                Integer.parseInt( ServerRequest.REG_BIOMETRIC_SUBREQ )
        );

        Intent intent = new Intent( activity, RESTService.class );
        intent.putExtra( RESTService.ACTION_RESULT, RequestType.REG_BIO_REQUEST );
        intent.putExtra( RESTService.EXTRA_PARAMS, pRequest );
        intent.putExtra( RESTService.EXTRA_RESULT_RECEIVER, instance );
        activity.startService( intent );
    }

    public void requestGCMRegistration( Context activity, final String hardwareToken, final String token ) throws IOException {
        // Encrypting to create request
        oEncrypter.setsUnEncryptedString( hardwareToken );
        oEncrypter.rsaEncrypt( activity );
        final String sEncryptedClientData = oEncrypter.bytesToHex();

        StringRequest postRequest = new StringRequest( Request.Method.POST, "http://198.101.209.120:8081/yodo",
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

                            externalListener.onResponse( RequestType.REG_GCM_REQUEST, response );
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
                        externalListener.onResponse( RequestType.ERROR_GENERAL, null );
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                // the POST parameters:
                params.put( "prt", "1.1.2" );
                params.put( "req", "9,4" );
                params.put( "par", sEncryptedClientData + REQ_SEP + token );
                return params;
            }
        };
        postRequest.setTag( "POST" );
        Volley.newRequestQueue( activity ).add( postRequest );

        /*pRequest = ServerRequest.createRegistrationRequest(
                sEncryptedClientData + REQ_SEP + token, //AppUtils.compressString( token ),
                Integer.parseInt( ServerRequest.REG_GCM_SUBREQ )
        );

        Intent intent = new Intent( activity, RESTService.class );
        intent.putExtra( RESTService.ACTION_RESULT, RequestType.REG_GCM_REQUEST );
        intent.putExtra( RESTService.EXTRA_PARAMS, pRequest );
        intent.putExtra( RESTService.EXTRA_RESULT_RECEIVER, instance );
        activity.startService( intent );*/
    }

    public void requestPIPReset( Activity activity, String hardwareToken, String pip, String newPip ) {
        String sEncryptedClientData, sEncryptedPIP, sEncryptedNewPIP, pRequest;
        StringBuilder sClientData = new StringBuilder();

        // Encrypting to create request
        oEncrypter.setsUnEncryptedString( hardwareToken );
        oEncrypter.rsaEncrypt( activity );
        sEncryptedClientData = oEncrypter.bytesToHex();

        oEncrypter.setsUnEncryptedString( pip );
        oEncrypter.rsaEncrypt( activity );
        sEncryptedPIP = oEncrypter.bytesToHex();

        oEncrypter.setsUnEncryptedString( newPip );
        oEncrypter.rsaEncrypt( activity );
        sEncryptedNewPIP = oEncrypter.bytesToHex();

        sClientData.append( sEncryptedClientData ).append( REQ_SEP );
        sClientData.append( sEncryptedPIP ).append( REQ_SEP );
        sClientData.append( sEncryptedNewPIP );

        pRequest = ServerRequest.createResetRequest(
                sClientData.toString(),
                Integer.parseInt( ServerRequest.RESET_PIP_SUBREQ )
        );

        Intent intent = new Intent( activity, RESTService.class );
        intent.putExtra( RESTService.ACTION_RESULT, RequestType.RESET_PIP_REQUEST );
        intent.putExtra( RESTService.EXTRA_PARAMS, pRequest );
        intent.putExtra( RESTService.EXTRA_RESULT_RECEIVER, instance );
        activity.startService( intent );
    }

    public void requestBiometricPIPReset( Activity activity, String authNumber, String hardwareToken, String newPip ) {
        String sEncryptedClientData, pRequest;

        // Encrypting to create request
        oEncrypter.setsUnEncryptedString(
                authNumber + REQ_SEP +
                        hardwareToken + REQ_SEP +
                        newPip
        );
        oEncrypter.rsaEncrypt( activity );
        sEncryptedClientData = oEncrypter.bytesToHex();

        pRequest = ServerRequest.createResetRequest(
                sEncryptedClientData,
                Integer.parseInt( ServerRequest.RESET_BIO_PIP_SUBREQ )
        );

        Intent intent = new Intent( activity, RESTService.class );
        intent.putExtra( RESTService.ACTION_RESULT, RequestType.RESET_BIO_PIP_REQUEST );
        intent.putExtra( RESTService.EXTRA_PARAMS, pRequest );
        intent.putExtra( RESTService.EXTRA_RESULT_RECEIVER, instance );
        activity.startService( intent );
    }

    public void requestBalance( Activity activity, String hardwareToken, String pip ) {
        String sEncryptedClientData, pRequest;

        String sClientData =
                hardwareToken + PCLIENT_SEP +
                pip + PCLIENT_SEP +
                System.currentTimeMillis() / 1000L;

        // Encrypting to create request
        oEncrypter.setsUnEncryptedString( sClientData );
        oEncrypter.rsaEncrypt( activity );
        sEncryptedClientData = oEncrypter.bytesToHex();

        pRequest = ServerRequest.createQueryRequest(
                sEncryptedClientData,
                Integer.parseInt( ServerRequest.QUERY_BAL_SUBREQ )
        );

        Intent intent = new Intent( activity, RESTService.class );
        intent.putExtra( RESTService.ACTION_RESULT, RequestType.QUERY_BAL_REQUEST );
        intent.putExtra( RESTService.EXTRA_PARAMS, pRequest );
        intent.putExtra( RESTService.EXTRA_RESULT_RECEIVER, instance );
        activity.startService( intent );
    }

    public void requestBiometricToken( Activity activity, String hardwareToken ) {
        String sEncryptedClientData, pRequest;

        // Encrypting to create request
        oEncrypter.setsUnEncryptedString(
                hardwareToken + REQ_SEP +
                ServerRequest.QUERY_BIOMETRIC
        );
        oEncrypter.rsaEncrypt( activity );
        sEncryptedClientData = oEncrypter.bytesToHex();

        pRequest = ServerRequest.createQueryRequest(
                sEncryptedClientData,
                Integer.parseInt( ServerRequest.QUERY_ACC_SUBREQ )
        );

        Intent intent = new Intent( activity, RESTService.class );
        intent.putExtra( RESTService.ACTION_RESULT, RequestType.QUERY_BIO_REQUEST );
        intent.putExtra( RESTService.EXTRA_PARAMS, pRequest );
        intent.putExtra( RESTService.EXTRA_RESULT_RECEIVER, instance );
        activity.startService( intent );
    }

    public void requestReceipt( Activity activity, String hardwareToken, String pip ) {
        String sEncryptedClientData, pRequest;

        // Encrypting to create request
        oEncrypter.setsUnEncryptedString(
                hardwareToken + REQ_SEP +
                pip + REQ_SEP +
                ServerRequest.QUERY_RECEIPT
        );
        oEncrypter.rsaEncrypt( activity );
        sEncryptedClientData = oEncrypter.bytesToHex();

        pRequest = ServerRequest.createQueryRequest(
                sEncryptedClientData,
                Integer.parseInt( ServerRequest.QUERY_ACC_SUBREQ )
        );

        Intent intent = new Intent( activity, RESTService.class );
        intent.putExtra( RESTService.ACTION_RESULT, RequestType.QUERY_RCV_REQUEST );
        intent.putExtra( RESTService.EXTRA_PARAMS, pRequest );
        intent.putExtra( RESTService.EXTRA_RESULT_RECEIVER, instance );
        activity.startService( intent );
    }

    public void requestAdvertising( Activity activity, String hardwareToken, String merchant ) {
        String sEncryptedClientData, pRequest;

        // Encrypting to create request
        oEncrypter.setsUnEncryptedString(
                hardwareToken + REQ_SEP +
                merchant + REQ_SEP +
                ServerRequest.QUERY_ADVERTISING
        );
        oEncrypter.rsaEncrypt( activity );
        sEncryptedClientData = oEncrypter.bytesToHex();

        pRequest = ServerRequest.createQueryRequest(
                sEncryptedClientData,
                Integer.parseInt( ServerRequest.QUERY_ACC_SUBREQ )
        );

        Intent intent = new Intent( activity, RESTService.class );
        intent.putExtra( RESTService.ACTION_RESULT, RequestType.QUERY_ADV_REQUEST );
        intent.putExtra( RESTService.EXTRA_PARAMS, pRequest );
        intent.putExtra( RESTService.EXTRA_RESULT_RECEIVER, instance );
        activity.startService( intent );
    }

    public void requestLinkingCode( Activity activity, String hardwareToken, String pip ) {
        String sEncryptedClientData, pRequest;

        // Encrypting to create request
        oEncrypter.setsUnEncryptedString(
                hardwareToken + REQ_SEP +
                pip + REQ_SEP +
                ServerRequest.QUERY_LINKING_CODE
        );
        oEncrypter.rsaEncrypt( activity );
        sEncryptedClientData = oEncrypter.bytesToHex();

        pRequest = ServerRequest.createQueryRequest(
                sEncryptedClientData,
                Integer.parseInt( ServerRequest.QUERY_ACC_SUBREQ )
        );

        Intent intent = new Intent( activity, RESTService.class );
        intent.putExtra( RESTService.ACTION_RESULT, RequestType.QUERY_LNK_REQUEST );
        intent.putExtra( RESTService.EXTRA_PARAMS, pRequest );
        intent.putExtra( RESTService.EXTRA_RESULT_RECEIVER, instance );
        activity.startService( intent );
    }

    public void requestLinkedAccounts( Activity activity, String hardwareToken, String pip ) {
        String sEncryptedClientData, pRequest;

        // Encrypting to create request
        oEncrypter.setsUnEncryptedString(
                hardwareToken + REQ_SEP +
                pip + REQ_SEP +
                ServerRequest.QUERY_LINKED_ACCOUNTS
        );
        oEncrypter.rsaEncrypt( activity );
        sEncryptedClientData = oEncrypter.bytesToHex();

        pRequest = ServerRequest.createQueryRequest(
                sEncryptedClientData,
                Integer.parseInt( ServerRequest.QUERY_ACC_SUBREQ )
        );

        Intent intent = new Intent( activity, RESTService.class );
        intent.putExtra( RESTService.ACTION_RESULT, RequestType.QUERY_LNK_ACC_REQUEST );
        intent.putExtra( RESTService.EXTRA_PARAMS, pRequest );
        intent.putExtra( RESTService.EXTRA_RESULT_RECEIVER, instance );
        activity.startService( intent );
    }

    public void requestCloseAccount( Activity activity, String hardwareToken, String pip ) {
        String sEncryptedClientData, pRequest;
        StringBuilder sClientData = new StringBuilder();

        String timeStamp = String.valueOf( System.currentTimeMillis() );

        sClientData.append( pip ).append( USR_SEP );
        sClientData.append( hardwareToken ).append( USR_SEP );
        sClientData.append( timeStamp ).append( REQ_SEP );
        sClientData.append( "0" ).append( REQ_SEP );
        sClientData.append( "0" );

        // Encrypting to create request
        oEncrypter.setsUnEncryptedString( sClientData.toString() );
        oEncrypter.rsaEncrypt( activity );
        sEncryptedClientData = oEncrypter.bytesToHex();

        pRequest = ServerRequest.createCloseRequest(
                sEncryptedClientData
        );

        Intent intent = new Intent( activity, RESTService.class );
        intent.putExtra( RESTService.ACTION_RESULT, RequestType.CLOSE_ACC_REQUEST );
        intent.putExtra( RESTService.EXTRA_PARAMS, pRequest );
        intent.putExtra( RESTService.EXTRA_RESULT_RECEIVER, instance );
        activity.startService( intent );
    }

    public void requestLinkAccount( Activity activity, String hardwareToken, String linkCode ) {
        String sEncryptedClientData, pRequest;
        String timeStamp = String.valueOf( System.currentTimeMillis() );

        // Encrypting to create request
        oEncrypter.setsUnEncryptedString(
                hardwareToken + REQ_SEP +
                        linkCode + REQ_SEP +
                        timeStamp
        );
        oEncrypter.rsaEncrypt( activity );
        sEncryptedClientData = oEncrypter.bytesToHex();

        pRequest = ServerRequest.createLinkingRequest(
                sEncryptedClientData,
                Integer.parseInt( ServerRequest.LINK_ACC_SUBREQ )
        );

        Intent intent = new Intent( activity, RESTService.class );
        intent.putExtra( RESTService.ACTION_RESULT, RequestType.LINK_ACC_REQUEST );
        intent.putExtra( RESTService.EXTRA_PARAMS, pRequest );
        intent.putExtra( RESTService.EXTRA_RESULT_RECEIVER, instance );
        activity.startService( intent );
    }

    public void requestDeLinkAccount( Activity activity, String hardwareToken, String pip, String linkedAccount, String accountType ) {
        String sEncryptedClientData, pRequest;

        // Encrypting to create request
        oEncrypter.setsUnEncryptedString(
                hardwareToken + REQ_SEP +
                pip + REQ_SEP +
                linkedAccount
        );
        oEncrypter.rsaEncrypt( activity );
        sEncryptedClientData = oEncrypter.bytesToHex();

        pRequest = ServerRequest.createDeLinkRequest(
                sEncryptedClientData,
                Integer.parseInt( accountType )
        );

        Intent intent = new Intent( activity, RESTService.class );
        intent.putExtra( RESTService.ACTION_RESULT, RequestType.DELINK_ACC_REQUEST );
        intent.putExtra( RESTService.EXTRA_PARAMS, pRequest );
        intent.putExtra( RESTService.EXTRA_RESULT_RECEIVER, instance );
        activity.startService( intent );
    }

    @Override
    protected void onReceiveResult( int resultCode, Bundle resultData ) {
        if( resultCode == RESTService.STATUS_FAILED ) {
            externalListener.onResponse( RequestType.ERROR_GENERAL, null );
        }
        else if( resultCode == RESTService.STATUS_NO_INTERNET ) {
            externalListener.onResponse( RequestType.ERROR_NO_INTERNET, null );
        }
        else {
            RequestType action      = (RequestType) resultData.getSerializable( RESTService.ACTION_RESULT );
            ServerResponse response = (ServerResponse) resultData.getSerializable( RESTService.EXTRA_RESULT );
            externalListener.onResponse( action , response );

            if( response != null )
                AppUtils.Logger( TAG, response.toString() );
        }
    }

    ///////////////////////////////////////////////////////
    // NEW WAY TO REQUEST THE SERVER (REST) USING VOLLEY //
    ///////////////////////////////////////////////////////

    /** Switch server IP address */
    //private static final String IP 	         = "http://50.56.180.133";  // Production
    private static final String IP 			 = "http://198.101.209.120";  // Development
    private static final String YODO_ADDRESS = "/yodo/yodoswitchrequest/getRequest/";

    /** Timeout */
    private final static int TIMEOUT = 10000;

    private RetryPolicy retryPolicy = new DefaultRetryPolicy(
            TIMEOUT,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
    );

    /**
     * Global request queue for Volley
     */
    private RequestQueue mRequestQueue;

    /**
     * @return The Volley Request queue, the queue will be created if it is null
     */
    public RequestQueue getRequestQueue( Context context ) {
        // lazy initialize the request queue, the queue instance will be
        // created when it is accessed for the first time
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue( context );
        }

        return mRequestQueue;
    }

    private void sendRequest( final Context activity, final String pRequest, final RequestType type ) {
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

                            AppUtils.Logger( TAG, XMLHandler.response.toString() );
                            externalListener.onResponse( type, XMLHandler.response );
                        } catch( ParserConfigurationException | SAXException | IOException e ) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse( VolleyError error ) {
                        error.printStackTrace();

                        if( error instanceof TimeoutError || error instanceof NoConnectionError )
                            externalListener.onResponse( RequestType.ERROR_NO_INTERNET, null );
                        else
                            externalListener.onResponse( RequestType.ERROR_GENERAL, null );
                    }
                }
        );
        httpRequest.setTag( "GET" );
        httpRequest.setRetryPolicy( retryPolicy );
        getRequestQueue( activity ).add( httpRequest );
    }

    public static String getSwitch() {
        if( IP.equals( "http://50.56.180.133" ) )
            return "P";
        return "D";
    }
}
