package co.yodo.mobile.database.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by hei on 05/08/16.
 * Database helper for the SQLite
 */
public class YodoSQLiteHelper extends SQLiteOpenHelper {
    /** DEBUG */
    @SuppressWarnings( "unused" )
    private static final String TAG = YodoSQLiteHelper.class.getSimpleName();

    /**
     * Current allowed tables
     */
    public interface Tables {
        String RECEIPTS = "Receipts";
        String COUPONS = "Coupons";
    }

    /** Common attributes names */
    public static final String COLUMN_ID          = "id";
    public static final String COLUMN_DESCRIPTION = "description"; // Merchant description
    public static final String COLUMN_CREATED     = "created";

    /** Receipts attributes names */
    public static final String COLUMN_AUTHNUMBER = "authNumber";  // Receipt identifier
    public static final String COLUMN_TCURRENCY  = "tcurrency";   // Merchant currency
    public static final String COLUMN_EXCH_RATE  = "exchRate";
    public static final String COLUMN_DCURRENCY  = "dcurrency";   // Tender currency
    public static final String COLUMN_AMOUNT     = "amount";      // Total amount
    public static final String COLUMN_TAMOUNT    = "tAmount";     // Tender amount
    public static final String COLUMN_CASHBACK   = "cashBack";    // Cashback amount
    public static final String COLUMN_BALANCE    = "balance";     // Account balance
    public static final String COLUMN_CURRENCY   = "currency";    // Account currency
    public static final String COLUMN_DONOR      = "donor";
    public static final String COLUMN_RECEIVER   = "receiver";
    public static final String COLUMN_OPENED     = "opened";

    /** Coupons attributes names */
    public static final String COLUMN_URL = "url";

    /** Sentence to create the receipts table */
    private static final String sqlCreateReceipts = "CREATE TABLE " + Tables.RECEIPTS + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_AUTHNUMBER  + " INTEGER, " + COLUMN_DESCRIPTION + " TEXT, " +
            COLUMN_TCURRENCY   + " TEXT, "    + COLUMN_EXCH_RATE   + " REAL,"  +
            COLUMN_DCURRENCY   + " TEXT, "    + COLUMN_AMOUNT      + " REAL, " +
            COLUMN_TAMOUNT     + " REAL, "    + COLUMN_CASHBACK    + " REAL, " +
            COLUMN_BALANCE     + " REAL, "    + COLUMN_CURRENCY    + " TEXT, " +
            COLUMN_DONOR       + " TEXT, "    + COLUMN_RECEIVER    + " TEXT, " +
            COLUMN_CREATED     + " TEXT, "    + COLUMN_OPENED      + " INTEGER DEFAULT 0)";

    /** Sentence to create the coupons table */
    private static final String sqlCreateCoupons = "CREATE TABLE " + Tables.COUPONS + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_DESCRIPTION + " TEXT, " + COLUMN_URL + " TEXT, " +
            COLUMN_CREATED     + " DATETIME)";

    /** Database basics */
    private static final String DATABASE_NAME = "yodo_client.db";
    private static final int DATABASE_VERSION = 1;

    public YodoSQLiteHelper( Context context ) {
        super( context, DATABASE_NAME, null, DATABASE_VERSION );
    }

    @Override
    public void onCreate( SQLiteDatabase db ) {
        // Creates the database tables
        db.execSQL( sqlCreateReceipts );
        db.execSQL( sqlCreateCoupons );
    }

    @Override
    public void onUpgrade( SQLiteDatabase db, int oldVersion, int newVersion ) {
        if( newVersion > oldVersion ) {
            // Deletes the previous tables
            db.execSQL( "DROP TABLE IF EXISTS " + Tables.RECEIPTS );
            db.execSQL( "DROP TABLE IF EXISTS " + Tables.COUPONS );

            // Creates a new table
            onCreate( db );
        }
    }
}
