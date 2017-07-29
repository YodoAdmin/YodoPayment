package co.yodo.mobile.business.network;

/**
 * Created by yodop on 2017-07-28.
 * Server ip addresses
 */
public class Config {
    /** Switch server IP address */
    private static final String DEMO_IP  = "http://162.244.228.84";  // Demo
    private static final String DEV_IP   = "http://162.244.228.78";  // Development
    private static final String LOCAL_IP = "http://192.168.1.38";    // Local

    /** IP used for requests */
    public static final String IP = DEMO_IP;

    /**
     * Returns an string that represents the server of the IP
     * @return De - demo
     *         D  - development
     *         L  - local
     */
    public static String getServerIdentifier() {
        return ( IP.equals( DEMO_IP ) ) ? "E" : ( IP.equals( DEV_IP ) ) ? "D" : "L";
    }
}
