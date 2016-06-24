package co.yodo.mobile.network;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

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
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.IOException;
import java.io.StringReader;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import co.yodo.mobile.R;
import co.yodo.mobile.component.Encrypter;
import co.yodo.mobile.helper.SystemUtils;
import co.yodo.mobile.network.model.ServerResponse;
import co.yodo.mobile.network.handler.XMLHandler;
import co.yodo.mobile.network.request.contract.IRequest;

/**
 * Created by luis on 15/12/14.
 * Generates a request to the Yodo Server
 */
@SuppressLint( "ParcelCreator" )
public class YodoRequest {
    /** DEBUG */
    private static final String TAG = YodoRequest.class.getSimpleName();

    /** Switch server IP address */
    private static final String PROD_IP  = "http://50.56.180.133";   // Production
    private static final String DEMO_IP  = "http://198.101.209.120"; // Demo
    private static final String DEV_IP   = "http://162.244.228.78";  // Development
    private static final String LOCAL_IP = "http://192.168.1.33";    // Local
    private static final String IP      = DEMO_IP;

    /** Two paths used for the requests */
    private static final String YODO_ADDRESS = "/yodo/yodoswitchrequest/getRequest/";

    /** Timeout for the requests */
    private final static int TIMEOUT = 1000 * 10; // 10 seconds
    private final static int RETRIES = 0;

    private RetryPolicy retryPolicy = new DefaultRetryPolicy(
            TIMEOUT,
            RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
    );

    /** Context of the application */
    private Context mCtx;

    /** Global request queue for Volley and Image Loader */
    private RequestQueue mRequestQueue = null;
    private ImageLoader mImageLoader = null;

    /** Object used to encrypt information */
    private Encrypter oEncrypter;

    /** The external listener to the service */
    private RESTListener listener;

    /** Singleton instance */
    private static YodoRequest instance = null;

    public interface RESTListener {
        /**
         * Listener for the server responses
         * @param responseCode Code of the request
         * @param response POJO for the response
         */
        void onResponse( int responseCode, ServerResponse response );
    }

    /**
     * Private constructor for the singleton
     * @param context The application context
     */
    private YodoRequest( Context context )  {
        // getApplicationContext() is key, it keeps you from leaking the
        // Activity or BroadcastReceiver if someone passes one in.
        mCtx = context.getApplicationContext();
        mRequestQueue = getRequestQueue();
        oEncrypter = Encrypter.getInstance( mCtx );

        mImageLoader = new ImageLoader( mRequestQueue,
                new ImageLoader.ImageCache() {
                    private final LruCache<String, Bitmap> cache = new LruCache<>( 10 );

                    @Override
                    public Bitmap getBitmap( String url) {
                        return cache.get( url );
                    }

                    @Override
                    public void putBitmap( String url, Bitmap bitmap ) {
                        cache.put( url, bitmap );
                    }
                }
        );
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
            mRequestQueue = Volley.newRequestQueue( mCtx );
        }
        return mRequestQueue;
    }

    /**
     * Gets the image loader object
     * @return The image loader
     */
    public ImageLoader getImageLoader() {
        return mImageLoader;
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
     * @return P  - production
     *         De - demo
     *         D  - development
     *         L  - local
     */
    public static String getSwitch() {
        return ( IP.equals( PROD_IP ) ) ? "P" :
               ( IP.equals( DEMO_IP ) ) ? "De" :
               ( IP.equals( LOCAL_IP ) ) ? "L" : "D";
    }

    /**
     * Sends a XML request to the server
     * @param request The request body
     * @param responseCode The response code
     */
    public void sendXMLRequest( final String request, final int responseCode ) {
        if( listener == null )
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
                            response.setMessage( mCtx.getString( R.string.message_error_server ) );
                        }
                        listener.onResponse( responseCode, response );
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse( VolleyError error ) {
                        handleVolleyException( error, responseCode );
                    }
                }
        );

        httpRequest.setTag( "GET" );
        httpRequest.setRetryPolicy( retryPolicy );
        getRequestQueue().add( httpRequest );
    }

    /**
     * Sends a JSON request to the server
     * @param responseCode The response code
     */
    public void sendJSONRequest( final Map<String, String> params, final int responseCode ) {
        if( listener == null )
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

                            // Sends the response to the listener

                            SystemUtils.Logger( TAG, response.toString() );
                        } catch( JSONException e ) {
                            e.printStackTrace();
                            response.setCode( ServerResponse.ERROR_SERVER );
                            response.setMessage( mCtx.getString( R.string.message_error_server ) );
                        }
                        listener.onResponse( responseCode, response );
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse( VolleyError error ) {
                        handleVolleyException( error, responseCode );
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                return params;
            }
        };

        httpRequest.setTag( "POST" );
        httpRequest.setRetryPolicy( retryPolicy );
        getRequestQueue().add( httpRequest );
    }

    /**
     * Handles the error response from volley
     * @param error The Volley error (e.g. timeout, network, server)
     * @param responseCode The response code for the activity
     */
    private void handleVolleyException( VolleyError error, int responseCode ) {
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

    /**
     * Executes a request (extends IRequest class)
     * @param request The request to be executed
     */
    public void invoke( IRequest request) {
        request.execute( oEncrypter, this );
    }
}
