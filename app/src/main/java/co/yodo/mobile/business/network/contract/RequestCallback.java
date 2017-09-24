package co.yodo.mobile.business.network.contract;

import co.yodo.mobile.business.network.model.ServerResponse;

/**
 * Created by hei on 19/04/17.
 * Handles the response of the requests
 */
public interface RequestCallback {
    /**
     *  Handles some actions before the request is
     *  executed
     */
    void onPrepare();

    /**
     * Listener for the server responses
     * @param response POJO for the response
     */
    void onResponse(ServerResponse response);

    /**
     * Wherever an error appears
     * @param error The error object
     */
    void onError(Throwable error);
}
