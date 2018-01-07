package co.yodo.mobile.business.network.libraries;

import co.yodo.mobile.business.network.contract.IExecuter;
import co.yodo.mobile.business.network.contract.RequestCallback;

/**
 * Created by hei on 20/04/17.
 * Interface for the rest libraries
 */
public abstract class IClient {
    /** Two paths used for the requests */
    protected static final String YODO = "/yodo/";
    protected static final String SWITCH_ADDRESS = YODO + "yodoswitchrequest/getRequest/";

    /**
     * Request something from the server, the response is a XML
     * @param params The data of the request
     * @param callback The response callback
     */
    public abstract void sendXmlRequest(String params, RequestCallback callback);

    /**
     * Request something from the server, the response is a JSON Array
     * @param executer It handle some complex requests (include cache)
     * @param callback The response callback
     */
    public abstract void sendJsonRequest(IExecuter executer, RequestCallback callback);
}
