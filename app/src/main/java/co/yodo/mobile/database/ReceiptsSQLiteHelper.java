package co.yodo.mobile.database;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import co.yodo.mobile.helper.SystemUtils;

/**
 * Created by luis on 1/02/15.
 * Helper for the receipts
 */
public class ReceiptsSQLiteHelper extends SQLiteOpenHelper {
    /** DEBUG */
    private static final String TAG = ReceiptsSQLiteHelper.class.getSimpleName();

    public static final String TABLE_RECEIPTS     = "Receipts";
    public static final String COLUMN_ID          = "id";
    public static final String COLUMN_AUTHNUMBER  = "authNumber";  // Receipt identifier
    public static final String COLUMN_DESCRIPTION = "description"; // Merchant description
    public static final String COLUMN_TCURRENCY   = "tcurrency";   // Merchant currency
    public static final String COLUMN_EXCH_RATE   = "exchRate";
    public static final String COLUMN_DCURRENCY   = "dcurrency";   // Tender currency
    public static final String COLUMN_AMOUNT      = "amount";      // Total amount
    public static final String COLUMN_TAMOUNT     = "tAmount";     // Tender amount
    public static final String COLUMN_CASHBACK    = "cashBack";    // Cashback amount
    public static final String COLUMN_BALANCE     = "balance";     // Account balance
    public static final String COLUMN_CURRENCY    = "currency";    // Account currency
    public static final String COLUMN_DONOR       = "donor";
    public static final String COLUMN_RECEIVER    = "receiver";
    public static final String COLUMN_CREATED     = "created";
    public static final String COLUMN_OPENED      = "opened";

    private static final String DATABASE_NAME = "receipts.db";
    private static final int DATABASE_VERSION = 6;

    // Sentence to create the receipts table
    String sqlCreate = "CREATE TABLE " + TABLE_RECEIPTS + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_AUTHNUMBER  + " INTEGER, " + COLUMN_DESCRIPTION + " TEXT, " +
            COLUMN_TCURRENCY   + " TEXT, "    + COLUMN_EXCH_RATE   + " REAL,"  +
            COLUMN_DCURRENCY   + " TEXT, "    + COLUMN_AMOUNT      + " REAL, " +
            COLUMN_TAMOUNT     + " REAL, "    + COLUMN_CASHBACK    + " REAL, " +
            COLUMN_BALANCE     + " REAL, "    + COLUMN_CURRENCY    + " TEXT, " +
            COLUMN_DONOR       + " TEXT, "    + COLUMN_RECEIVER    + " TEXT, " +
            COLUMN_CREATED     + " TEXT, "    + COLUMN_OPENED      + " INTEGER DEFAULT 0)";

    private static ReceiptsSQLiteHelper sInstance;

    private ReceiptsSQLiteHelper( Context context ) {
        super( context, DATABASE_NAME, null, DATABASE_VERSION );
    }

    public static synchronized ReceiptsSQLiteHelper getInstance( Context context ) {
        if( sInstance == null ) {
            sInstance = new ReceiptsSQLiteHelper( context.getApplicationContext() );
        }
        return sInstance;
    }

    @Override
    public void onCreate( SQLiteDatabase db ) {
        // Creates the table
        db.execSQL( sqlCreate );
    }

    @Override
    public void onUpgrade( SQLiteDatabase db, int oldVersion, int newVersion ) {
        // Deletes previous table
        //db.execSQL( "DROP TABLE IF EXISTS Receipts" );
        if( newVersion > oldVersion ) {
            try {
                db.execSQL( "ALTER TABLE " + TABLE_RECEIPTS + " ADD COLUMN " + COLUMN_OPENED + " INTEGER DEFAULT 0" );
            } catch( SQLException e ) {
                SystemUtils.eLogger( TAG, textToError( COLUMN_OPENED ) );
            }

            try {
                db.execSQL( "ALTER TABLE " + TABLE_RECEIPTS + " ADD COLUMN " + COLUMN_DONOR + " TEXT" );
                db.execSQL( "ALTER TABLE " + TABLE_RECEIPTS + " ADD COLUMN " + COLUMN_RECEIVER + " TEXT" );
            } catch( SQLException e ) {
                SystemUtils.eLogger( TAG,
                        "Failed to create columns " + COLUMN_DONOR + ", " + COLUMN_RECEIVER + ". Most likely it already exists, which is fine." );
            }

            try {
                db.execSQL( "ALTER TABLE " + TABLE_RECEIPTS + " ADD COLUMN " + COLUMN_EXCH_RATE + " REAL" );
            } catch( SQLException e ) {
                SystemUtils.eLogger( TAG,
                        "Failed to create " + COLUMN_EXCH_RATE + " column. Most likely it already exists, which is fine." );
            }

            try {
                db.execSQL( "ALTER TABLE " + TABLE_RECEIPTS + " ADD COLUMN " + COLUMN_TCURRENCY + " TEXT" );
            } catch( SQLException e ) {
                SystemUtils.eLogger( TAG,
                        "Failed to create " + COLUMN_TCURRENCY + " column. Most likely it already exists, which is fine." );
            }

            try {
                db.execSQL( "ALTER TABLE " + TABLE_RECEIPTS + " ADD COLUMN " + COLUMN_CURRENCY + " TEXT" );
            } catch( SQLException e ) {
                SystemUtils.eLogger( TAG, textToError( COLUMN_CURRENCY ) );
            }
        }

        //Se crea la nueva versión de la tabla
        //db.execSQL( sqlCreate );
    }

    private String textToError( String... values ) {
        String text = "Failed to create ";
        final int size = values.length;

        if( size > 1 ) {
            text += "columns ";
            for( int i = 0; i < size - 1; i++ )
                text += values[i] + ", ";
            text += values[size - 1] + ". ";
        } else
            text += values[0] + " column.";
        text += "Most likely it already exists, which is fine.";

        return text;
    }
}
