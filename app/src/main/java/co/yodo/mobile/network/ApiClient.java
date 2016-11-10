package co.yodo.mobile.network;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import javax.inject.Inject;

import co.yodo.mobile.component.cipher.RSACrypt;
import co.yodo.mobile.helper.SystemUtils;
import co.yodo.mobile.network.model.ServerResponse;
import co.yodo.mobile.network.request.contract.IRequest;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;

/**
 * Created by luis on 15/12/14.
 * Generates a request to the Yodo Server
 */
public class ApiClient {
    /** DEBUG */
    private static final String TAG = ApiClient.class.getSimpleName();

    /** Client to execute requests */
    private Retrofit mRetrofit;

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
    public ApiClient( Retrofit retrofit, RSACrypt encrypter )  {
        mRetrofit = retrofit;
        mEncrypter = encrypter;
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
        return "P";
    }

    /**
     * Creates the interface for the requests
     * @param service The interface
     * @param <T> The type
     * @return An object to call the request
     */
    public <T> T create( Class<T> service ) {
        return mRetrofit.create( service );
    }

    /**
     * Handles the requests errors
     * @param responseCode The response code
     * @param error The error type
     */
    private void handleError( final int responseCode, final Throwable error ) {
        error.printStackTrace();

        // depending on the error, return an alert to the activity
        ServerResponse response = new ServerResponse();
        if( error instanceof IOException )  // Network error
            response.setCode( ServerResponse.ERROR_NETWORK );
        else
            response.setCode( ServerResponse.ERROR_FAILED );
        mListener.onResponse( responseCode, response );
    }

    public void sendRequest( Call<ServerResponse> sResponse, final int responseCode ) {
        sResponse.enqueue( new Callback<ServerResponse>() {
            @Override
            public void onResponse( Call<ServerResponse> call, retrofit2.Response<ServerResponse> response ) {
                ServerResponse temp = response.body();
                SystemUtils.eLogger( TAG, temp.toString() );
                mListener.onResponse( responseCode, temp );
            }

            @Override
            public void onFailure( Call<ServerResponse> call, Throwable error ) {
                handleError( responseCode, error );
            }
        } );
    }

    public void sendJsonRequest( Call<ResponseBody> sResponse, final int responseCode ) {
        sResponse.enqueue( new Callback<ResponseBody>() {
            @Override
            public void onResponse( Call<ResponseBody> call, retrofit2.Response<ResponseBody> response ) {
                ServerResponse serverResponse = new ServerResponse();

                try {
                    final String body = response.body().string();
                    JSONObject jsonResponse = new JSONObject( body );
                    SystemUtils.eLogger( TAG, body );

                    // Parse the attributes of the ServerResponse
                    serverResponse.setCode( jsonResponse.getString( "respCode" ) );
                    serverResponse.setAuthNumber( jsonResponse.getString( "authCode" ) );
                    serverResponse.setMessage( jsonResponse.getString( "msg" ) );
                    serverResponse.setRTime( jsonResponse.getLong( "respTime" ) );
                } catch( JSONException | IOException e ) {
                    e.printStackTrace();
                    serverResponse.setCode( ServerResponse.ERROR_SERVER );
                }

                mListener.onResponse( responseCode, serverResponse );
            }

            @Override
            public void onFailure( Call<ResponseBody> call, Throwable error ) {
                handleError( responseCode, error );
            }
        } );
    }

    /**
     * Executes a request (extends IRequest class)
     * @param request The request to be executed
     */
    public void invoke( IRequest request ) {
        if( mListener == null )
            throw new NullPointerException( "Listener not defined" );

        request.execute( mEncrypter, this );
    }
}
