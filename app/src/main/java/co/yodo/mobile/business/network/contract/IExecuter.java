package co.yodo.mobile.business.network.contract;

import org.json.JSONArray;

import java.io.IOException;

import co.yodo.mobile.business.network.model.ServerResponse;

/**
 * Created by hei on 21/04/17.
 * Handles some requests
 */
public interface IExecuter {
    /**
     * It handles the response of a json array request
     * @param array The response
     * @return The parsed response from the server
     */
    ServerResponse execute(JSONArray array) throws IOException;
}
