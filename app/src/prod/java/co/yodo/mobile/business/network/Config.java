package co.yodo.mobile.business.network;

/**
 * Created by yodop on 2017-07-28.
 * Production server information
 */
public class Config {
    /** Switch server IP address */
    private static final String PROD_IP  = "http://50.56.180.133";   // Production

    /** IP used for requests */
    public static final String IP = PROD_IP;

    /**
     * Returns an string that represents the server of the IP
     * @return P  - production
     */
    public static String getServerIdentifier() {
        return "P";
    }
}
