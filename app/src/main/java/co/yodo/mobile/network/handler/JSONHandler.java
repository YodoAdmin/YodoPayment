package co.yodo.mobile.network.handler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import co.yodo.mobile.network.model.ServerResponse;

/**
 * Created by luis on 19/01/16.
 * Handler for the JSON responses
 */
public class JSONHandler {
    /** DEBUG */
    @SuppressWarnings( "unused" )
    private static final String TAG = JSONHandler.class.getSimpleName();

    /** Param elements */
    private static final String CURRENCY_ELEM = "currency"; // It also belongs to the receipt

    /** Transaction Elements */
    public static final String YI = "yi";
    public static final String YT = "yt";

    /** Merchant elements */
    private static final String DESCRIPTION_ELEM = "description";
    private static final String TCURRENCY_ELEM   = "dcurrency";

    /** Receipt elements */
    private static final String CREATED_ELEM   = "created";
    private static final String AMOUNT_ELEM    = "amount";
    private static final String TAMOUNT_ELEM   = "tamount";
    private static final String CASHBACK_ELEM  = "cashback";
    private static final String AUTHNUM_ELEM   = "transauthnumber";
    private static final String EXCH_RATE_ELEM = "xch_rate";
    private static final String EXCH_REC_ELEM  = "trx_account"; // receiver
    private static final String EXCH_DON_ELEM  = "account"; // donor
    private static final String EXCH_BAL_ELEM  = "account_bal";
    private static final String EXCH_CUR_ELEM  = "account_cur";

    public static ServerResponse parseReceipt( String message ) {
        ServerResponse response = new ServerResponse();

        try {
            JSONArray temp  = new JSONArray( message );
            JSONObject jo   = temp.getJSONObject( 0 );

            // Get "yi", data of the merchant
            JSONObject yi   = (JSONObject) jo.get( YI );
            response.addParam( ServerResponse.DESCRIPTION, String.valueOf( yi.get( DESCRIPTION_ELEM ) ) );
            response.addParam( ServerResponse.TCURRENCY,   String.valueOf( yi.get( TCURRENCY_ELEM ) ) );

            // Get "yt", data of the transaction
            JSONObject yt   = (JSONObject) jo.get( YT );
            response.addParam( ServerResponse.CREATED,    String.valueOf( yt.get( CREATED_ELEM ) ) );
            response.addParam( ServerResponse.AMOUNT,     String.valueOf( yt.get( AMOUNT_ELEM ) ) );
            response.addParam( ServerResponse.TAMOUNT,    String.valueOf( yt.get( TAMOUNT_ELEM ) ) );
            response.addParam( ServerResponse.CASHBACK,   String.valueOf( yt.get( CASHBACK_ELEM ) ) );
            response.addParam( ServerResponse.AUTHNUMBER, String.valueOf( yt.get( AUTHNUM_ELEM ) ) );
            response.addParam( ServerResponse.DCURRENCY,  String.valueOf( yt.get( CURRENCY_ELEM ) ) );
            response.addParam( ServerResponse.EXCH_RATE,  String.valueOf( yt.get( EXCH_RATE_ELEM ) ) );

            // Only for heart transactions
            if( yt.has( EXCH_DON_ELEM ) )
            response.addParam( ServerResponse.DONOR,      String.valueOf( yt.get( EXCH_DON_ELEM ) ) );

            if( yt.has( EXCH_REC_ELEM ) )
            response.addParam( ServerResponse.RECEIVER,   String.valueOf( yt.get( EXCH_REC_ELEM ) ) );

            response.addParam( ServerResponse.BALANCE,    String.valueOf( yt.get( EXCH_BAL_ELEM ) ) );
            response.addParam( ServerResponse.CURRENCY,   String.valueOf( yt.get( EXCH_CUR_ELEM ) ) );
        } catch( JSONException e ) {
            e.printStackTrace();
        }

        return response;
    }
}

