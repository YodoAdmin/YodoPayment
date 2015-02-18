package co.yodo.mobile.data;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;

public class ServerResponse implements Serializable {
    /** ID for authorized responses */
    public static final String AUTHORIZED		       = "AU00";
    public static final String AUTHORIZED_REGISTRATION = "AU01";
    public static final String AUTHORIZED_BALANCE      = "AU55";

    /** ID for error responses */
    public static final String ERROR_FAILED        = "ER00";
    public static final String ERROR_INCORRECT_PIP = "ER22";

    /** Param keys */
    public static final String LOGO        = "logo";
    public static final String BALANCE     = "balance";
    public static final String BIOMETRIC   = "biometric";
    public static final String ADVERTISING = "advertising";

    /** Receipt elements */
    public static final String DESCRIPTION = "description";
    public static final String CREATED     = "created";
    public static final String AMOUNT      = "amount";
    public static final String TAMOUNT     = "tamount";
    public static final String CASHBACK    = "cashback";
    public static final String AUTHNUMBER  = "transauthnumber";
    public static final String CURRENCY    = "currency";

	private String code;
	private String authNumber;
	private String message;
	private long rtime;
    private HashMap<String, String> params;

    public ServerResponse() {
        params = new HashMap<>();
    }

	public void setCode(String code) {
		this.code = code;
	}

	public String getCode() {
		return this.code;
	}

	public void setAuthNumber(String authNumber) {
		this.authNumber = authNumber;
	}

	public String getAuthNumber() {
		return this.authNumber;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getMessage() {
		return this.message;
	}

	public void setRTime(long rtime) {
		this.rtime = rtime;
	}

	public long getRTime() {
		return this.rtime;
	}

    public void addParam(String key, String value) {
        params.put( key, value );
    }

    public String getParam(String key) {
        return params.get(key);
    }

    public HashMap<String, String> getParams() { return params; }
	
	@Override
	public String toString() {
		return "\nCode : " + this.code + "\n" +
               " AuthNumber : " + this.authNumber + "\n" +
               " Message : " + this.message + "\n" +
               " Time : " + this.rtime + "\n" +
               " Params : " + Arrays.asList(this.params);
	}
}