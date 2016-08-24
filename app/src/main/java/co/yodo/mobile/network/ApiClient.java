package co.yodo.mobile.network;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.IOException;
import java.io.StringReader;
import java.util.Map;

import javax.inject.Inject;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import co.yodo.mobile.component.cipher.RSACrypt;
import co.yodo.mobile.helper.SystemUtils;
import co.yodo.mobile.network.handler.XMLHandler;
import co.yodo.mobile.network.model.ServerResponse;
import co.yodo.mobile.network.request.contract.IRequest;

/**
 * Created by luis on 15/12/14.
 * Generates a request to the Yodo Server
 */
public class ApiClient {
    /** DEBUG */
    private static final String TAG = ApiClient.class.getSimpleName();

    /** Switch server IP address */
    private static final String PROD_IP  = "http://50.56.180.133";   // Production
    private static final String DEMO_IP  = "http://198.101.209.120"; // Demo
    private static final String DEV_IP   = "http://162.244.228.78";  // Development
    private static final String LOCAL_IP = "http://192.168.1.37";    // Local
    private static final String IP = DEV_IP;

    /** Two paths used for the requests */
    private static final String YODO_ADDRESS = "/yodo/yodoswitchrequest/getRequest/";

    /** Timeout for the requests */
    private final static int TIMEOUT = 1000 * 20; // 20 seconds
    private final static int RETRIES = -1; // To avoid retries

    private RetryPolicy retryPolicy = new DefaultRetryPolicy(
            TIMEOUT,
            RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
    );

    /** Global request queue for Volley */
    private RequestQueue mRequestQueue;

    /** Loads images from URLs */
    private ImageLoader mImageLoader;

    /** Object used to encrypt information */
    private RSACrypt mEncrypter;

    /** The external mListener to the service */
    private RequestsListener mListener;

    public interface RequestsListener {
        /**
         * Listener for the preparation of the request
         */
        void onPrepare();

        /**
         * Listener for the server responses
         * @param responseCode Code of the request
         * @param response POJO for the response
         */
        void onResponse( int responseCode, ServerResponse response );
    }

    @Inject
    public ApiClient( RequestQueue requestQueue, ImageLoader imageLoader, RSACrypt encrypter )  {
        mRequestQueue = requestQueue;
        mImageLoader = imageLoader;
        mEncrypter = encrypter;
    }

    /**
     * Gets the image loader object
     * @return The image loader
     */
    public ImageLoader getImageLoader() {
        return mImageLoader;
    }

    /**
     * Add a mListener to the service
     * @param listener Listener for the requests to the server
     */
    public void setListener( RequestsListener listener ) {
        this.mListener = listener ;
    }

    /**
     * Returns an string that represents the server of the IP
     * @return P  - production
     *         De - demo
     *         D  - development
     *         L  - local
     */
    public static String getSwitch() {
        return ( IP.equals( PROD_IP ) ) ? "P" :
               ( IP.equals( DEMO_IP ) ) ? "E" :
               ( IP.equals( LOCAL_IP ) ) ? "L" : "D";
    }

    /**
     * Sends a XML request to the server
     * @param request The request body
     * @param responseCode The response code
     */
    public void sendXMLRequest( final String request, final int responseCode ) {
        if( mListener == null )
            throw new NullPointerException( "Listener not defined" );

        final StringRequest httpRequest = new StringRequest( Request.Method.GET, IP + YODO_ADDRESS + request,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse( String xml ) {
                        ServerResponse response = new ServerResponse();
                        try {
                            // Handling XML
                            SAXParserFactory spf = SAXParserFactory.newInstance();
                            SAXParser sp = spf.newSAXParser();
                            XMLReader xr = sp.getXMLReader();

                            // Create handler to handle XML Tags ( extends DefaultHandler )
                            xr.setContentHandler( new XMLHandler() );
                            xr.parse( new InputSource( new StringReader( xml ) ) );

                            // Get the response from the handler
                            response = XMLHandler.response;
                            SystemUtils.Logger( TAG, response.toString() );
                        } catch( ParserConfigurationException | SAXException | IOException e ) {
                            e.printStackTrace();
                            response.setCode( ServerResponse.ERROR_SERVER );
                        }
                        mListener.onResponse( responseCode, response );
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse( VolleyError error ) {
                        handleVolleyException( responseCode, error );
                    }
                }
        );

        addToRequestQueue( httpRequest );
    }

    /**
     * Sends a JSON request to the server
     * @param responseCode The response code
     */
    public void sendJSONRequest( final Map<String, String> params, final int responseCode ) {
        if( mListener == null )
            throw new NullPointerException( "Listener not defined" );

        final StringRequest httpRequest = new StringRequest( Request.Method.POST, IP + ":8081/yodo",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse( String json ) {
                        ServerResponse response = new ServerResponse();
                        try {
                            JSONObject jsonResponse = new JSONObject( json );

                            // Parse the attributes of the ServerResponse
                            response.setCode( jsonResponse.getString( "respCode" ) );
                            response.setAuthNumber( jsonResponse.getString( "authCode" ) );
                            response.setMessage( jsonResponse.getString( "msg" ) );
                            response.setRTime( jsonResponse.getLong( "respTime" ) );

                            // Sends the response to the mListener
                            SystemUtils.Logger( TAG, response.toString() );
                        } catch( JSONException e ) {
                            e.printStackTrace();
                            response.setCode( ServerResponse.ERROR_SERVER );
                        }
                        mListener.onResponse( responseCode, response );
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse( VolleyError error ) {
                        handleVolleyException( responseCode, error );
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                return params;
            }
        };

        addToRequestQueue( httpRequest );
    }

    public <T> void addToRequestQueue( Request<T> httpRequest ) {
        // Setups any configuration before the request is added to the queue
        mListener.onPrepare();

        httpRequest.setTag( TAG );
        httpRequest.setRetryPolicy( retryPolicy );
        mRequestQueue.add( httpRequest );
    }

    /**
     * Handles the error response from volley
     * * @param responseCode The response code for the activity
     * @param error The Volley error (e.g. timeout, network, server)
     */
    private void handleVolleyException( int responseCode, VolleyError error ) {
        error.printStackTrace();
        // depending on the error, return an alert to the activity
        ServerResponse response = new ServerResponse();
        if( error instanceof TimeoutError  )
            response.setCode( ServerResponse.ERROR_TIMEOUT );
        else if( error instanceof NetworkError )
            response.setCode( ServerResponse.ERROR_NETWORK );
        else if( error instanceof ServerError )
            response.setCode( ServerResponse.ERROR_SERVER );
        else
            response.setCode( ServerResponse.ERROR_UNKOWN );
        mListener.onResponse( responseCode, response );
    }

    /**
     * Cancels all the pending requests with an identifier
     * @param tag The identifier
     */
    @SuppressWarnings( "unused" )
    public void cancelPendingRequests( Object tag ) {
        mRequestQueue.cancelAll( tag );
    }

    /**
     * Executes a request (extends IRequest class)
     * @param request The request to be executed
     */
    public void invoke( IRequest request ) {
        request.execute( mEncrypter, this );
    }
}
