package co.yodo.mobile.net;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.text.format.Time;

import co.yodo.mobile.R;
import co.yodo.mobile.component.Encrypter;
import co.yodo.mobile.data.ServerResponse;
import co.yodo.mobile.helper.AppConfig;
import co.yodo.mobile.helper.AppUtils;
import co.yodo.mobile.service.RESTService;

/**
 * Created by luis on 15/12/14.
 * Request to the Yodo Server
 */
public class YodoRequest extends ResultReceiver {
    /** DEBUG */
    private static final String TAG = YodoRequest.class.getSimpleName();

    public interface RESTListener {
        /**
         * Listener for the server responses
         * @param type Type of the request
         * @param response POJO for the response
         */
        public void onResponse(RequestType type, ServerResponse response);
    }

    /** ID for each request */
    public enum RequestType {
        ERROR_NO_INTERNET     ( "-1" ), // ERROR NO INTERNET
        ERROR_GENERAL         ( "00" ), // ERROR GENERAL
        AUTH_REQUEST          ( "01" ), // RT=0, ST=1
        AUTH_PIP_REQUEST      ( "02" ), // RT=0, ST=2
        RESET_PIP_REQUEST     ( "03" ), // RT=3, ST=1
        RESET_BIO_PIP_REQUEST ( "04" ), // RT=3, ST=2
        QUERY_BAL_REQUEST     ( "05" ), // RT=4, ST=1
        QUERY_BIO_REQUEST     ( "06" ), // RT=4, ST=3
        QUERY_ADV_REQUEST     ( "07" ), // RT=4, ST=3
        QUERY_RCV_REQUEST     ( "08" ), // RT=4, ST=3
        QUERY_LNK_REQUEST     ( "09" ), // RT=4, ST=3
        QUERY_LNK_ACC_REQUEST ( "10" ), // RT=4, ST=3
        CLOSE_ACC_REQUEST     ( "11" ), // RT=8, ST=1
        REG_CLIENT_REQUEST    ( "12 "), // RT=9, ST=0
        REG_BIO_REQUEST       ( "13 "), // RT=9, ST=3
        LINK_ACC_REQUEST      ( "14 "), // RT=10, ST=0
        DELINK_ACC_REQUEST    ( "15 "); // RT=11

        private final String name;

        private RequestType(String s) {
            name = s;
        }

        public String toString() {
            return name;
        }
    }

    /** ID for the types of progress dialog */
    public enum ProgressDialogType {
        NORMAL,
        TRANSPARENT
    }

    /** Object used to encrypt information */
    private Encrypter oEncrypter;

    /** Progress dialog */
    private ProgressDialog progressDialog;

    /** Singleton instance */
    private static YodoRequest instance = null;

    /** the external listener to the service */
    private RESTListener externalListener;

    /** User's data separator */
    private static final String	USR_SEP     = "**";
    private static final String	REQ_SEP     = ",";
    private static final String	PCLIENT_SEP = "/";

    /**
     * Create a new ResultReceive to receive results.  Your
     * {@link #onReceiveResult} method will be called from the thread running
     * <var>handler</var> if given, or from an arbitrary thread if null.
     *
     * @param handler Default
     */
    private YodoRequest(Handler handler) {
        super( handler );
        oEncrypter = new Encrypter();
    }

    /**
     * Gets the instance of the service
     * @return instance
     */
    public static YodoRequest getInstance() {
        if( instance == null )
            instance = new YodoRequest( new Handler() );
        return instance;
    }

    /**
     * Add a listener to the service
     * @param listener Listener for the requests to the server
     */
    public void setListener(RESTListener listener) {
        externalListener = listener ;
    }

    public void createProgressDialog(Context context, ProgressDialogType type) {
        switch( type ) {
            case NORMAL:
                progressDialog = new ProgressDialog( context );
                progressDialog.setCancelable( false );
                progressDialog.show();
                progressDialog.setContentView( R.layout.custom_progressdialog );
                break;
        }
    }

    public boolean progressDialogShowing() {
        return progressDialog != null && progressDialog.isShowing();
    }

    public void destroyProgressDialog() {
        if( progressDialog != null && progressDialog.isShowing() ) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    public void requestAuthentication(Activity activity, String hardwareToken) {
        String sEncryptedClientData, pRequest;

        // Encrypting to create request
        oEncrypter.setsUnEncryptedString( hardwareToken );
        oEncrypter.rsaEncrypt( activity );
        sEncryptedClientData = oEncrypter.bytesToHex();

        pRequest = ServerRequest.createAuthenticationRequest(
                sEncryptedClientData,
                Integer.parseInt( ServerRequest.AUTH_HW_SUBREQ )
        );

        Intent intent = new Intent( activity, RESTService.class );
        intent.putExtra( RESTService.ACTION_RESULT, RequestType.AUTH_REQUEST );
        intent.putExtra( RESTService.EXTRA_PARAMS, pRequest );
        intent.putExtra( RESTService.EXTRA_RESULT_RECEIVER, instance );
        activity.startService( intent );
    }

    public void requestPIPAuthentication(Activity activity, String hardwareToken, String pip) {
        String sEncryptedClientData, pRequest;
        StringBuilder sClientData = new StringBuilder();

        Time now = new Time();
        now.setToNow();

        sClientData.append( hardwareToken ).append( PCLIENT_SEP );
        sClientData.append( pip ).append( PCLIENT_SEP );
        sClientData.append( now.toMillis( true ) / 1000L );

        // Encrypting to create request
        oEncrypter.setsUnEncryptedString( sClientData.toString() );
        oEncrypter.rsaEncrypt( activity );
        sEncryptedClientData = oEncrypter.bytesToHex();

        pRequest = ServerRequest.createAuthenticationRequest(
                sEncryptedClientData,
                Integer.parseInt( ServerRequest.AUTH_HW_PIP_SUBREQ )
        );

        Intent intent = new Intent( activity, RESTService.class );
        intent.putExtra( RESTService.ACTION_RESULT, RequestType.AUTH_PIP_REQUEST );
        intent.putExtra( RESTService.EXTRA_PARAMS, pRequest );
        intent.putExtra( RESTService.EXTRA_RESULT_RECEIVER, instance );
        activity.startService( intent );
    }

    public void requestRegistration(Activity activity, String hardwareToken, String pip) {
        String sEncryptedClientData, pRequest;
        StringBuilder sClientData = new StringBuilder();

        String timeStamp = String.valueOf( System.currentTimeMillis() );

        sClientData.append( AppConfig.YODO_BIOMETRIC ).append( USR_SEP );
        sClientData.append( pip ).append( USR_SEP );
        sClientData.append( hardwareToken ).append( USR_SEP );
        sClientData.append( timeStamp );

        // Encrypting to create request
        oEncrypter.setsUnEncryptedString( sClientData.toString() );
        oEncrypter.rsaEncrypt( activity );
        sEncryptedClientData = oEncrypter.bytesToHex();

        pRequest = ServerRequest.createRegistrationRequest(
                sEncryptedClientData,
                Integer.parseInt( ServerRequest.REG_CLIENT_SUBREQ )
        );

        Intent intent = new Intent( activity, RESTService.class );
        intent.putExtra( RESTService.ACTION_RESULT, RequestType.REG_CLIENT_REQUEST );
        intent.putExtra( RESTService.EXTRA_PARAMS, pRequest );
        intent.putExtra( RESTService.EXTRA_RESULT_RECEIVER, instance );
        activity.startService( intent );
    }

    public void requestBiometricRegistration(Activity activity, String authNumber, String token) {
        String pRequest = ServerRequest.createRegistrationRequest(
                authNumber + REQ_SEP + token,
                Integer.parseInt( ServerRequest.REG_BIOMETRIC_SUBREQ )
        );

        Intent intent = new Intent( activity, RESTService.class );
        intent.putExtra( RESTService.ACTION_RESULT, RequestType.REG_BIO_REQUEST );
        intent.putExtra( RESTService.EXTRA_PARAMS, pRequest );
        intent.putExtra( RESTService.EXTRA_RESULT_RECEIVER, instance );
        activity.startService( intent );
    }

    public void requestPIPReset(Activity activity, String hardwareToken, String pip, String newPip) {
        String sEncryptedClientData, sEncryptedPIP, sEncryptedNewPIP, pRequest;
        StringBuilder sClientData = new StringBuilder();

        // Encrypting to create request
        oEncrypter.setsUnEncryptedString( hardwareToken );
        oEncrypter.rsaEncrypt( activity );
        sEncryptedClientData = oEncrypter.bytesToHex();

        oEncrypter.setsUnEncryptedString( pip );
        oEncrypter.rsaEncrypt( activity );
        sEncryptedPIP = oEncrypter.bytesToHex();

        oEncrypter.setsUnEncryptedString( newPip );
        oEncrypter.rsaEncrypt( activity );
        sEncryptedNewPIP = oEncrypter.bytesToHex();

        sClientData.append( sEncryptedClientData ).append( REQ_SEP );
        sClientData.append( sEncryptedPIP ).append( REQ_SEP );
        sClientData.append( sEncryptedNewPIP );

        pRequest = ServerRequest.createResetRequest(
                sClientData.toString(),
                Integer.parseInt( ServerRequest.RESET_PIP_SUBREQ )
        );

        Intent intent = new Intent( activity, RESTService.class );
        intent.putExtra( RESTService.ACTION_RESULT, RequestType.RESET_PIP_REQUEST );
        intent.putExtra( RESTService.EXTRA_PARAMS, pRequest );
        intent.putExtra( RESTService.EXTRA_RESULT_RECEIVER, instance );
        activity.startService( intent );
    }

    public void requestBiometricPIPReset(Activity activity, String authNumber, String hardwareToken, String newPip) {
        String sEncryptedClientData, pRequest;

        // Encrypting to create request
        oEncrypter.setsUnEncryptedString(
                authNumber    + REQ_SEP +
                hardwareToken + REQ_SEP +
                newPip
        );
        oEncrypter.rsaEncrypt( activity );
        sEncryptedClientData = oEncrypter.bytesToHex();

        pRequest = ServerRequest.createResetRequest(
                sEncryptedClientData,
                Integer.parseInt( ServerRequest.RESET_BIO_PIP_SUBREQ )
        );

        Intent intent = new Intent( activity, RESTService.class );
        intent.putExtra( RESTService.ACTION_RESULT, RequestType.RESET_BIO_PIP_REQUEST );
        intent.putExtra( RESTService.EXTRA_PARAMS, pRequest );
        intent.putExtra( RESTService.EXTRA_RESULT_RECEIVER, instance );
        activity.startService( intent );
    }

    public void requestBalance(Activity activity, String hardwareToken, String pip) {
        String sEncryptedClientData, pRequest;
        StringBuilder sClientData = new StringBuilder();

        Time now = new Time();
        now.setToNow();

        sClientData.append( hardwareToken ).append( PCLIENT_SEP );
        sClientData.append( pip ).append( PCLIENT_SEP );
        sClientData.append( now.toMillis( true ) / 1000L );

        // Encrypting to create request
        oEncrypter.setsUnEncryptedString( sClientData.toString() );
        oEncrypter.rsaEncrypt( activity );
        sEncryptedClientData = oEncrypter.bytesToHex();

        pRequest = ServerRequest.createQueryRequest(
                sEncryptedClientData,
                Integer.parseInt( ServerRequest.QUERY_BAL_SUBREQ )
        );

        Intent intent = new Intent( activity, RESTService.class );
        intent.putExtra( RESTService.ACTION_RESULT, RequestType.QUERY_BAL_REQUEST );
        intent.putExtra( RESTService.EXTRA_PARAMS, pRequest );
        intent.putExtra( RESTService.EXTRA_RESULT_RECEIVER, instance );
        activity.startService( intent );
    }

    public void requestBiometricToken(Activity activity, String hardwareToken) {
        String sEncryptedClientData, pRequest;

        // Encrypting to create request
        oEncrypter.setsUnEncryptedString(
                hardwareToken + REQ_SEP +
                ServerRequest.QUERY_BIOMETRIC
        );
        oEncrypter.rsaEncrypt( activity );
        sEncryptedClientData = oEncrypter.bytesToHex();

        pRequest = ServerRequest.createQueryRequest(
                sEncryptedClientData,
                Integer.parseInt( ServerRequest.QUERY_ACC_SUBREQ )
        );

        Intent intent = new Intent( activity, RESTService.class );
        intent.putExtra( RESTService.ACTION_RESULT, RequestType.QUERY_BIO_REQUEST );
        intent.putExtra( RESTService.EXTRA_PARAMS, pRequest );
        intent.putExtra( RESTService.EXTRA_RESULT_RECEIVER, instance );
        activity.startService( intent );
    }

    public void requestReceipt(Activity activity, String hardwareToken, String pip) {
        String sEncryptedClientData, pRequest;

        // Encrypting to create request
        oEncrypter.setsUnEncryptedString(
                hardwareToken + REQ_SEP +
                pip + REQ_SEP +
                ServerRequest.QUERY_RECEIPT
        );
        oEncrypter.rsaEncrypt( activity );
        sEncryptedClientData = oEncrypter.bytesToHex();

        pRequest = ServerRequest.createQueryRequest(
                sEncryptedClientData,
                Integer.parseInt( ServerRequest.QUERY_ACC_SUBREQ )
        );

        Intent intent = new Intent( activity, RESTService.class );
        intent.putExtra( RESTService.ACTION_RESULT, RequestType.QUERY_RCV_REQUEST );
        intent.putExtra( RESTService.EXTRA_PARAMS, pRequest );
        intent.putExtra( RESTService.EXTRA_RESULT_RECEIVER, instance );
        activity.startService( intent );
    }

    public void requestAdvertising(Activity activity, String hardwareToken, String merchant) {
        String sEncryptedClientData, pRequest;

        // Encrypting to create request
        oEncrypter.setsUnEncryptedString(
                hardwareToken + REQ_SEP +
                merchant + REQ_SEP +
                ServerRequest.QUERY_ADVERTISING
        );
        oEncrypter.rsaEncrypt( activity );
        sEncryptedClientData = oEncrypter.bytesToHex();

        pRequest = ServerRequest.createQueryRequest(
                sEncryptedClientData,
                Integer.parseInt( ServerRequest.QUERY_ACC_SUBREQ )
        );

        Intent intent = new Intent( activity, RESTService.class );
        intent.putExtra( RESTService.ACTION_RESULT, RequestType.QUERY_ADV_REQUEST );
        intent.putExtra( RESTService.EXTRA_PARAMS, pRequest );
        intent.putExtra( RESTService.EXTRA_RESULT_RECEIVER, instance );
        activity.startService( intent );
    }

    public void requestLinkingCode(Activity activity, String hardwareToken, String pip) {
        String sEncryptedClientData, pRequest;

        // Encrypting to create request
        oEncrypter.setsUnEncryptedString(
                hardwareToken + REQ_SEP +
                pip + REQ_SEP +
                ServerRequest.QUERY_LINKING_CODE
        );
        oEncrypter.rsaEncrypt( activity );
        sEncryptedClientData = oEncrypter.bytesToHex();

        pRequest = ServerRequest.createQueryRequest(
                sEncryptedClientData,
                Integer.parseInt( ServerRequest.QUERY_ACC_SUBREQ )
        );

        Intent intent = new Intent( activity, RESTService.class );
        intent.putExtra( RESTService.ACTION_RESULT, RequestType.QUERY_LNK_REQUEST );
        intent.putExtra( RESTService.EXTRA_PARAMS, pRequest );
        intent.putExtra( RESTService.EXTRA_RESULT_RECEIVER, instance );
        activity.startService( intent );
    }

    public void requestLinkedAccounts(Activity activity, String hardwareToken, String pip) {
        String sEncryptedClientData, pRequest;

        // Encrypting to create request
        oEncrypter.setsUnEncryptedString(
                hardwareToken + REQ_SEP +
                pip + REQ_SEP +
                ServerRequest.QUERY_LINKED_ACCOUNTS
        );
        oEncrypter.rsaEncrypt( activity );
        sEncryptedClientData = oEncrypter.bytesToHex();

        pRequest = ServerRequest.createQueryRequest(
                sEncryptedClientData,
                Integer.parseInt( ServerRequest.QUERY_ACC_SUBREQ )
        );

        Intent intent = new Intent( activity, RESTService.class );
        intent.putExtra( RESTService.ACTION_RESULT, RequestType.QUERY_LNK_ACC_REQUEST );
        intent.putExtra( RESTService.EXTRA_PARAMS, pRequest );
        intent.putExtra( RESTService.EXTRA_RESULT_RECEIVER, instance );
        activity.startService( intent );
    }

    public void requestCloseAccount(Activity activity, String hardwareToken, String pip) {
        String sEncryptedClientData, pRequest;
        StringBuilder sClientData = new StringBuilder();

        String timeStamp = String.valueOf( System.currentTimeMillis() );

        sClientData.append( pip ).append( USR_SEP );
        sClientData.append( hardwareToken ).append( USR_SEP );
        sClientData.append( timeStamp ).append( REQ_SEP );
        sClientData.append( "0" ).append( REQ_SEP );
        sClientData.append( "0" );

        // Encrypting to create request
        oEncrypter.setsUnEncryptedString( sClientData.toString() );
        oEncrypter.rsaEncrypt( activity );
        sEncryptedClientData = oEncrypter.bytesToHex();

        pRequest = ServerRequest.createCloseRequest(
                sEncryptedClientData
        );

        Intent intent = new Intent( activity, RESTService.class );
        intent.putExtra( RESTService.ACTION_RESULT, RequestType.CLOSE_ACC_REQUEST );
        intent.putExtra( RESTService.EXTRA_PARAMS, pRequest );
        intent.putExtra( RESTService.EXTRA_RESULT_RECEIVER, instance );
        activity.startService( intent );
    }

    public void requestLinkAccount(Activity activity, String hardwareToken, String linkCode) {
        String sEncryptedClientData, pRequest;
        String timeStamp = String.valueOf( System.currentTimeMillis() );

        // Encrypting to create request
        oEncrypter.setsUnEncryptedString(
                hardwareToken + REQ_SEP +
                        linkCode + REQ_SEP +
                        timeStamp
        );
        oEncrypter.rsaEncrypt( activity );
        sEncryptedClientData = oEncrypter.bytesToHex();

        pRequest = ServerRequest.createLinkingRequest(
                sEncryptedClientData,
                Integer.parseInt( ServerRequest.LINK_ACC_SUBREQ )
        );

        Intent intent = new Intent( activity, RESTService.class );
        intent.putExtra( RESTService.ACTION_RESULT, RequestType.LINK_ACC_REQUEST );
        intent.putExtra( RESTService.EXTRA_PARAMS, pRequest );
        intent.putExtra( RESTService.EXTRA_RESULT_RECEIVER, instance );
        activity.startService( intent );
    }

    public void requestDeLinkAccount(Activity activity, String hardwareToken, String pip, String linkedAccount, String accountType) {
        String sEncryptedClientData, pRequest;

        // Encrypting to create request
        oEncrypter.setsUnEncryptedString(
                hardwareToken + REQ_SEP +
                pip + REQ_SEP +
                linkedAccount
        );
        oEncrypter.rsaEncrypt( activity );
        sEncryptedClientData = oEncrypter.bytesToHex();

        pRequest = ServerRequest.createDeLinkRequest(
                sEncryptedClientData,
                Integer.parseInt( accountType )
        );

        Intent intent = new Intent( activity, RESTService.class );
        intent.putExtra( RESTService.ACTION_RESULT, RequestType.DELINK_ACC_REQUEST );
        intent.putExtra( RESTService.EXTRA_PARAMS, pRequest );
        intent.putExtra( RESTService.EXTRA_RESULT_RECEIVER, instance );
        activity.startService( intent );
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        if( resultCode == RESTService.STATUS_FAILED ) {
            externalListener.onResponse( RequestType.ERROR_GENERAL, null );
        }
        else if( resultCode == RESTService.STATUS_NO_INTERNET ) {
            externalListener.onResponse( RequestType.ERROR_NO_INTERNET, null );
        }
        else {
            RequestType action      = (RequestType) resultData.getSerializable( RESTService.ACTION_RESULT );
            ServerResponse response = (ServerResponse) resultData.getSerializable( RESTService.EXTRA_RESULT );
            externalListener.onResponse( action , response );

            AppUtils.Logger( TAG, response.toString() );
        }
    }
}
