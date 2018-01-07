package co.yodo.mobile.business.network;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import javax.inject.Inject;

import co.yodo.mobile.business.network.encryption.IEncryption;
import co.yodo.mobile.business.network.model.ServerResponse;
import co.yodo.mobile.business.network.request.IRequest;
import co.yodo.mobile.utils.ErrorUtils;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;

/**
 * Created by luis on 15/12/14.
 * Generates a request to the backend
 */
public class ApiClient {
    /** Application context */
    private Context context;

    /** Client to execute requests */
    private Retrofit retrofit;

    /** Object used to encrypt information */
    private IEncryption encryption;

    @Inject
    public ApiClient(Context context, Retrofit retrofit, IEncryption encryption)  {
        this.context = context;
        this.retrofit = retrofit;
        this.encryption = encryption;
    }

    /**
     * Creates the interface for the requests
     * @param service The interface
     * @param <T> The type
     * @return An object to call the request
     */
    public <T> T create(Class<T> service) {
        return retrofit.create(service);
    }

    /**
     * Executes a request (extends IRequest class)
     * @param request The request to be executed
     */
    public void invoke(IRequest request, RequestCallback callback) {
        request.execute(encryption, this, callback );
    }

    /**
     * Sends a XML request to the server
     * @param request The request to the server
     * @param callback The callback for the observer
     */
    public void sendXMLRequest(final Call<ServerResponse> request, final RequestCallback callback) {
        request.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, retrofit2.Response<ServerResponse> response) {
                try {
                    callback.onResponse(response.body());
                } catch(NullPointerException error) {
                    ErrorUtils.handleApiError(context, error, callback);
                }
            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable error) {
                ErrorUtils.handleApiError(context, error, callback);
            }
        } );
    }

    /**
     * Sends a JSON request to the server
     * @param request The request to the server
     * @param callback The callback for the observer
     */
    public void sendJSONRequest(final Call<ResponseBody> request, final RequestCallback callback) {
        request.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                try {
                    ServerResponse serverResponse = new ServerResponse();
                    final String body = response.body().string();
                    JSONObject jsonResponse = new JSONObject(body);

                    // Parse the attributes of the ServerResponse
                    serverResponse.setCode(jsonResponse.getString("respCode"));
                    serverResponse.setAuthNumber(jsonResponse.getString("authCode"));
                    serverResponse.setMessage(jsonResponse.getString("msg"));
                    serverResponse.setRTime(jsonResponse.getLong("respTime"));
                    callback.onResponse(serverResponse );
                } catch (JSONException | IOException error) {
                    ErrorUtils.handleApiError(context, error, callback);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable error) {
                ErrorUtils.handleApiError(context, error, callback);
            }
        } );
    }

    public interface RequestCallback {
        /**
         * Listener for the server responses
         * @param response POJO for the response
         */
        void onResponse(ServerResponse response);

        /**
         * Whenever an error occurs
         * @param message The message to be displayed
         */
        void onError(String message);
    }
}
