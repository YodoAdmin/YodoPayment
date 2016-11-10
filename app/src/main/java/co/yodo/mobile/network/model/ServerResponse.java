package co.yodo.mobile.network.model;

import org.simpleframework.xml.Element;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;

public class ServerResponse implements Serializable {
    /** ID for authorized responses */
    public static final String AUTHORIZED		       = "AU00";
    public static final String AUTHORIZED_REGISTRATION = "AU01";
    public static final String AUTHORIZED_BALANCE      = "AU55";

    /** ID for error responses */
    public static final String ERROR_UNKOWN        = "UNKN";
    public static final String ERROR_NETWORK       = "NCON";
    public static final String ERROR_TIMEOUT       = "TOUT";
    public static final String ERROR_SERVER        = "ESRV";
    public static final String ERROR_FAILED        = "ER00";
    public static final String ERROR_NO_BALANCE    = "ER21";
    public static final String ERROR_INCORRECT_PIP = "ER22";

    /** Param keys */
    public static final String LOGO         = "logo";
    public static final String BALANCE      = "balance";
    public static final String CURRENCY     = "currency";
    public static final String BIOMETRIC    = "biometric";
    public static final String ADVERTISING  = "advertising";
    public static final String LINKING_CODE = "linking_code";

    /** Merchant elements */
    public static final String DESCRIPTION = "description";
    public static final String DCURRENCY   = "dcurrency";

    /** Receipt elements */
    public static final String CREATED     = "created";
    public static final String AMOUNT      = "amount";
    public static final String TAMOUNT     = "tamount";
    public static final String CASHBACK    = "cashback";
    public static final String AUTHNUMBER  = "transauthnumber";
    public static final String TCURRENCY   = "tcurrency";
    public static final String EXCH_RATE   = "xch_rate";
    public static final String DONOR       = "donor";
    public static final String RECEIVER    = "receiver";

    /** Linked Account elements */
    public static final String TO   = "to";
    public static final String FROM = "from";

    @Element( name = "code" )
    private String code;

    @Element( name = "authNumber" )
    private String authNumber;

    @Element( name = "message", required = false )
    private String message;

    @Element
    private Params params;

    @Element( name = "rtime" )
    private long rtime;

    public void setCode( String code ) {
        this.code = code;
    }

    public String getCode() {
        return this.code;
    }

    public void setAuthNumber( String authNumber ) {
        this.authNumber = authNumber;
    }

    public String getAuthNumber() {
        return this.authNumber;
    }

    public void setMessage( String message ) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }

    public Params getParams() {
        return this.params;
    }

    public void setRTime( long rtime ) {
        this.rtime = rtime;
    }

    public long getRTime() {
        return this.rtime;
    }

    public void setParams( Params params ) {
        this.params = params;
    }

    public String getParam( String test ) { return ""; }

	@Override
	public String toString() {
		return "\nCode : "      + this.code       + "\n" +
               " AuthNumber : " + this.authNumber + "\n" +
               " Message : "    + this.message    + "\n" +
               " Time : "       + this.rtime      + "\n" +
               " Params : "     + this.params.toString();
	}
}