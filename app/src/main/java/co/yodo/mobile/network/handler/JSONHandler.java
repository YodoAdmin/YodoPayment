package co.yodo.mobile.network.handler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import co.yodo.mobile.database.model.Receipt;

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
    private static final String YI = "yi";
    private static final String YT = "yt";

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

    public static Receipt parseReceipt( String message ) {
        Receipt receipt = null;
        String donor = null, receiver = null;

        try {
            JSONArray temp  = new JSONArray( message );
            JSONObject jo   = temp.getJSONObject( 0 );

            // Get "yi", data of the merchant
            JSONObject yi   = (JSONObject) jo.get( YI );
            // Get "yt", data of the transaction
            JSONObject yt   = (JSONObject) jo.get( YT );

            // Only for heart transactions
            if( yt.has( EXCH_DON_ELEM ) )
                donor = String.valueOf( yt.get( EXCH_DON_ELEM ) );

            if( yt.has( EXCH_REC_ELEM ) )
                receiver = String.valueOf( yt.get( EXCH_REC_ELEM ) );

            receipt = new Receipt.Builder()
                    .description( String.valueOf( yi.get( DESCRIPTION_ELEM ) ) )
                    .authnumber( String.valueOf( yt.get( AUTHNUM_ELEM ) ) )
                    .created( String.valueOf( yt.get( CREATED_ELEM ) ) )
                    .total( String.valueOf( yt.get( AMOUNT_ELEM ) ),
                            String.valueOf( yi.get( TCURRENCY_ELEM ) ) )
                    .tender( String.valueOf( yt.get( TAMOUNT_ELEM ) ),
                             String.valueOf( yt.get( CURRENCY_ELEM ) ) )
                    .cashback( String.valueOf( yt.get( CASHBACK_ELEM ) ) )
                    .exchRate( String.valueOf( yt.get( EXCH_RATE_ELEM ) ) )
                    .donor( donor )
                    .recipient( receiver )
                    .balance( String.valueOf( yt.get( EXCH_BAL_ELEM ) ),
                              String.valueOf( yt.get( EXCH_CUR_ELEM ) ) )
                    .build();
        } catch( JSONException e ) {
            e.printStackTrace();
        }

        return receipt;
    }
}

