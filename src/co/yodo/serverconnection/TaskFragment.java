package co.yodo.serverconnection;

import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import co.yodo.R;
import co.yodo.helper.Utils;
import co.yodo.helper.YodoGlobals;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;

public class TaskFragment extends Fragment {
	/*!< DEBUG */
	private final static boolean DEBUG = true;
	private final static String TAG = "TaskFragment";
	
	/**
	 * Callback interface through which the AsyncTask will report the
	 * task's progress and results back to the Activity.
	 */
	public static interface YodoCallback {
		void onPreExecute(String message);
		void onPostExecute();
		void onTaskCompleted(ServerResponse response, int type);
	}
	
	private YodoCallback mCallbacks;
	private SwitchServer mTask;
	private boolean mRunning;
	
	/**
	 * Hold a reference to the parent Activity so we can report the
	 * task's current progress and results. The Android framework 
	 * will pass us a reference to the newly created Activity after 
	 * each configuration change.
	 */
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if(!(activity instanceof YodoCallback)) {
			throw new IllegalStateException("Activity must implement the TaskCallbacks interface.");
	    }
		
		mCallbacks = (YodoCallback) activity;
	}
	
	/**
	 * This method will only be called once when the retained
	 * Fragment is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    setRetainInstance(true);
	}
	
	/**
	 * Set the callback to null so we don't accidentally leak the 
	 * Activity instance.
	 */
	@Override
	public void onDetach() {
		super.onDetach();
	    mCallbacks = null;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	    cancel();
	}
	
	/**
	 * Start the background task.
	 */
	public void start(SwitchServer server, String... requestParams) {
		if(!mRunning) {
			mTask = server;
			mTask.execute(requestParams);
			mRunning = true;
		}
	}
	
	/**
	 * Cancel the background task.
	 */
	public void cancel() {
		if(mRunning) {
			mTask.cancel(false);
		    mTask = null;
		    mRunning = false;
		}
	}
	
	/**
	 * Returns the current state of the background task.
	 */
	public boolean isRunning() {
		return mRunning;
	}
	
	public SwitchServer getSwitchServerInstance() {
		return new SwitchServer();
	}
	
	public class SwitchServer extends AsyncTask<String, Void, ServerResponse>  {
	    /*!< Query type */
	    private int type = 0;

	    /*!< Progress Dialog constants */
	    private boolean flagPorgress = false;
	    private String message;

	    /*!< Connection Detector */
	    private boolean internetFlag = true;

	    /*!< ID for identify requests */
	    public static final String AUTH_HW_REQUEST     = "01";	// RT=0, ST=1
	    public static final String AUTH_HW_PIP_REQUEST = "02";	// RT=0, ST=2
	    public static final String BALANCE_REQUEST     = "03";	// RT=4, ST=1
	    public static final String RECEIPT_REQUEST     = "04";	// RT=4, ST=3
	    public static final String RESET_PIP_REQUEST   = "05";	// RT=3, ST=1
	    public static final String REGISTER_REQUEST    = "06";	// RT=9, ST=0
	    public static final String CLOSE_REQUEST       = "07";	// RT=8, ST=1
	    public static final String BIO_REG_REQUEST     = "08";	// RT=9, ST=3
	    public static final String QUERY_ADS_REQUEST   = "09";	// RT=4, ST=3
	    public static final String BIOMETRIC_REQUEST   = "10";	// RT=4, ST=3

	    /*!< Switch server ip address */ 
	    //private static final String IP 		     = "http://192.168.1.34"; // Localhost
	    //private static final String IP 		     = "http://50.56.180.133"; // Production 
	    private static final String IP 			 = "http://198.101.209.120"; // Development 
	    //private static final String YODO_ADDRESS = "/yodoLuis/yodoswitchrequest/getRequest/"; // Localhost
	    private static final String YODO_ADDRESS = "/yodo/yodoswitchrequest/getRequest/"; // Production & Development

	    /*!< Response Params */
	    HashMap<String, String> responseParams;

	    /**
	     * Switch Server Constructor
	     */
	    public SwitchServer() {
	    	if(mCallbacks != null)
	    		message =  ((Context) mCallbacks).getString(R.string.loading);
	    }

	    @Override
	    protected ServerResponse doInBackground(String... requestParams) {
	        // Check internet connection
	        if(!Utils.isOnline(IP)) {
	        	internetFlag = false;
	        }

	        // Connecting to the server
	        if(requestParams[0].equals(AUTH_HW_REQUEST)) {
	            this.connect(ServerRequest.createAuthenticationRequest(requestParams[1], Integer.parseInt(ServerRequest.AUTH_HW_SUBREQ)));
	        }
	        else if(requestParams[0].equals(AUTH_HW_PIP_REQUEST)) {
	            this.connect(ServerRequest.createAuthenticationRequest(requestParams[1], Integer.parseInt(ServerRequest.AUTH_HW_PIP_SUBREQ)));
	        }
	        else if(requestParams[0].equals(BALANCE_REQUEST)){
	            this.connect(ServerRequest.createQueryRequest(requestParams[1], Integer.parseInt(ServerRequest.QUERY_BAL_SUBREQ)));
	        }
	        else if(requestParams[0].equals(RECEIPT_REQUEST)) {
	            this.connect(ServerRequest.createQueryRequest(requestParams[1], Integer.parseInt(ServerRequest.QUERY_DATA_SUBREQ)));
	        }
	        else if(requestParams[0].equals(RESET_PIP_REQUEST)) {
	            this.connect(ServerRequest.createResetRequest(requestParams[1], Integer.parseInt(ServerRequest.RESET_PIP_SUBREQ)));
	        }
	        else if(requestParams[0].equals(REGISTER_REQUEST)) {
	            this.connect(ServerRequest.createRegistrationRequest(requestParams[1], Integer.parseInt(ServerRequest.CLIENT_SUBREQ)));
	        }
	        else if(requestParams[0].equals(BIO_REG_REQUEST)) {
				this.connect(ServerRequest.createRegistrationRequest(requestParams[1], Integer.parseInt(ServerRequest.BIOMETRIC_SUBREQ)));
			}
	        else if(requestParams[0].equals(QUERY_ADS_REQUEST)) {
				this.connect(ServerRequest.createQueryRequest(requestParams[1], Integer.parseInt(ServerRequest.QUERY_DATA_SUBREQ)));
			}
	        else if(requestParams[0].equals(BIOMETRIC_REQUEST)) {
				this.connect(ServerRequest.createQueryRequest(requestParams[1], Integer.parseInt(ServerRequest.QUERY_DATA_SUBREQ)));
			}
	        else if(requestParams[0].equals(CLOSE_REQUEST)) {
	            this.connect(ServerRequest.createCloseRequest(requestParams[1]));
	        }

	        // Getting response
	        return getServerResponse();
	    }

	    public void connect(String pRequest) {
	    	XMLHandler myXMLHandler;
	        try {
	            // Handling XML
	            SAXParserFactory spf = SAXParserFactory.newInstance();
	            SAXParser sp = spf.newSAXParser();
	            XMLReader xr = sp.getXMLReader();
	            
	            if(DEBUG)
	            	Utils.Logger(DEBUG, TAG, IP + YODO_ADDRESS + pRequest);
	            
	            // Send URL to parse XML Tags
	            URL sourceUrl = new URL(IP + YODO_ADDRESS + pRequest);

	            // Create handler to handle XML Tags ( extends DefaultHandler )
	            myXMLHandler = new XMLHandler();
	            xr.setContentHandler(myXMLHandler);
	            xr.parse(new InputSource(sourceUrl.openStream()));
	        } catch(Exception e) {
	            System.out.println("XML Pasing Exception = " + e);
	        }

	        // Get result from MyXMLHandler SitlesList Object
	        responseParams = XMLHandler.responseValues;
	    }

	    public ServerResponse getServerResponse(){
	        ServerResponse oSrvRes = null;

	        if((responseParams != null) && (!responseParams.isEmpty()) ){
	            oSrvRes = new ServerResponse();

	            Iterator<Map.Entry<String, String>> it = responseParams.entrySet().iterator();
	            while(it.hasNext()) {
	                Map.Entry<String, String> pairs = it.next();

	                // Getting the response code
	                if(pairs.getKey().equals(ServerResponse.CODE_ELEM))
	                    oSrvRes.setCode(pairs.getValue());

	                // Getting the authorization number
	                if(pairs.getKey().equals(ServerResponse.AUTH_NUM_ELEM))
	                    oSrvRes.setAuthNumber(pairs.getValue());

	                // Getting the response message
	                if(pairs.getKey().equals(ServerResponse.MESSAGE_ELEM))
	                    oSrvRes.setMessage(pairs.getValue());

	                // Getting the response parameters
	                if(pairs.getKey().equals(ServerResponse.PARAMS_ELEM))
	                    oSrvRes.setParams(pairs.getValue());

	                it.remove(); // avoids a ConcurrentModificationException
	            }
	        }
	        return oSrvRes;
	    }

	    @Override
	    protected void onPreExecute() {
	        super.onPreExecute();

	        if(mCallbacks != null && flagPorgress) {
				mCallbacks.onPreExecute(message);
			}
	    }

	    @Override
	    protected void onPostExecute(ServerResponse response) {
	        super.onPostExecute(response);

	        if(response == null && !internetFlag) {
	        	response = new ServerResponse();
	        	response.setCode(YodoGlobals.ERROR_INTERNET);
	        }
	        
	    	if(response == null)
	    		Utils.Logger(DEBUG, TAG, ((Context) mCallbacks).getString(R.string.null_response));
	    	else
	    		Utils.Logger(DEBUG, TAG, response.toString());

	        if(mCallbacks != null) {
	        	TaskFragment.this.cancel();
	        	mCallbacks.onPostExecute();
	        	mCallbacks.onTaskCompleted(response, type);
	        }
	    }

	    public void setType(int type) {
	        this.type = type;
	    }

	    public void setDialog(boolean state, String message) {
	        this.flagPorgress = state;

	        if(message != null)
	            this.message = message;
	    }
	}
}
