package co.yodo.serverconnection;

public class ServerResponse {
	/**
	 * XML root element
	 */
	public static final String ROOT_ELEMENT = "Yodoresponses";
	
	/**
	 * XML sub root element
	 */
	public static final String SUB_ROOT_ELEMENT = "Yodoresponse";
	
	public static final String CODE_ELEM = "code";
	
	public static final String AUTH_NUM_ELEM = "authNumber";
	
	public static final String MESSAGE_ELEM = "message";
	
	public static final String PARAMS_ELEM = "params";
	
	public static final String BALANCE_ELEM = "balance";
	
	public static final String DESCRIPTION_ELEM = "description";
	
	public static final String CREATED_ELEM = "created";
	
	public static final String AMOUNT_ELEM = "amount";
	
	public static final String TENDER_ELEM = "tamount";
	
	public static final String CASHBACK_ELEM = "cashback";
	
	public static final String RECEIVE_ELEM = "transauthnumber";
	
	public static final String TIME_ELEM = "rtime";
	
	public static final String VALUE_SEPARATOR = ">";
	
	public static final String ENTRY_SEPARATOR = "#";
	
	/**
	 * Response code number
	 */
	private String code;
	
	/**
	 * Response authentication number
	 */
    private String authNumber;
	
	/**
	 * Response message
	 */
    private String message;
	
	/**
	 * Response parameters
	 */
    private String params;
	
	public String getRootElement() {
		return ROOT_ELEMENT;
	}
	
	public String getSubRootElement() {
		return SUB_ROOT_ELEMENT;
	}
	
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getAuthNumber() {
		return authNumber;
	}
	public void setAuthNumber(String authNumber) {
		this.authNumber = authNumber;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getParams() {
		return params;
	}
	public void setParams(String params) {
		this.params = params;
	}

	/**
	 * @return the balanceElem
	 */
	public static String getBalanceElem() {
		return BALANCE_ELEM;
	}
	
	public long getTime() {
		Long time = null;
		String aParams[] = params.split(ENTRY_SEPARATOR);
		
		for(String param : aParams) {
            String aValParams[] = param.split(VALUE_SEPARATOR);
            
            if(aValParams[0].equals(TIME_ELEM)) 
                time = Long.valueOf(aValParams[1]);
		}
        
		return time;
	}
	
	@Override
	public String toString() {
		return code + " " + authNumber + " " + message + " " + params;
	}
}
