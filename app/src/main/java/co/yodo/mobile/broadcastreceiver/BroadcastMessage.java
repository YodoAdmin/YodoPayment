package co.yodo.mobile.broadcastreceiver;

/**
 * All the messages that the broadcast receivers exchange.
 * 
 * @author Davi Rocha
 */
public class BroadcastMessage {
	
	/**
	 * It is the ID of the application (the package) used in all the broadcast
	 * messages, to prevent conflict problems from others broadcasts.
	 */
	private static final String BROADCAST_APPID
		= BroadcastMessage.class.getClass().getPackage().getName() + ".";
	
	/**
	 * It is used to send the merchant device name
	 * 
	 * EXTRA - The name - String object.
	 */
	public static final String ACTION_NEW_MERCHANT
		= BROADCAST_APPID + "ActionNewLocation";
	public static final String EXTRA_NEW_MERCHANT
		= BROADCAST_APPID + "ExtraNewLocation";

	/**
	 * It is used to send the hardware token, to register the gcm_id
	 *
	 * EXTRA - The name - String object.
	 */
	public static final String EXTRA_HARDWARE_TOKEN
			= BROADCAST_APPID + "ExtraHardwareToken";
}
