package co.yodo.mobile.database;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import co.yodo.mobile.helper.AppUtils;

/**
 * Created by luis on 1/02/15.
 * Helper for the receipts
 */
public class ReceiptsSQLiteHelper extends SQLiteOpenHelper {
    /** DEBUG */
    private static final String TAG = ReceiptsSQLiteHelper.class.getSimpleName();

    public static final String TABLE_RECEIPTS     = "Receipts";
    public static final String COLUMN_ID          = "id";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_AUTHNUMBER  = "authNumber";
    public static final String COLUMN_CURRENCY    = "dcurrency";
    public static final String COLUMN_EXCH_RATE   = "exchRate";
    public static final String COLUMN_AMOUNT      = "amount";
    public static final String COLUMN_TAMOUNT     = "tAmount";
    public static final String COLUMN_CASHBACK    = "cashBack";
    public static final String COLUMN_BALANCE     = "balance";
    public static final String COLUMN_DONOR       = "donor";
    public static final String COLUMN_RECEIVER    = "receiver";
    public static final String COLUMN_CREATED     = "created";
    public static final String COLUMN_OPENED      = "opened";

    private static final String DATABASE_NAME = "receipts.db";
    private static final int DATABASE_VERSION = 5;

    // Sentence to create the receipts table
    String sqlCreate = "CREATE TABLE " + TABLE_RECEIPTS + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_DESCRIPTION + " TEXT, " + COLUMN_AUTHNUMBER  + " INTEGER, " +
            COLUMN_CURRENCY    + " TEXT, " + COLUMN_EXCH_RATE   + " REAL," +
            COLUMN_AMOUNT      + " REAL, " + COLUMN_TAMOUNT     + " REAL, " +
            COLUMN_CASHBACK    + " REAL, " + COLUMN_BALANCE     + " REAL, " +
            COLUMN_DONOR       + " TEXT, " + COLUMN_RECEIVER    + " TEXT, " +
            COLUMN_CREATED     + " TEXT, " + COLUMN_OPENED      + " INTEGER DEFAULT 0)";

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
    public void onCreate(SQLiteDatabase db) {
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
                AppUtils.Logger( TAG,
                        "Failed to create " + COLUMN_OPENED + " column. Most likely it already exists, which is fine." );
            }

            try {
                db.execSQL( "ALTER TABLE " + TABLE_RECEIPTS + " ADD COLUMN " + COLUMN_DONOR + " TEXT" );
                db.execSQL( "ALTER TABLE " + TABLE_RECEIPTS + " ADD COLUMN " + COLUMN_RECEIVER + " TEXT" );
            } catch( SQLException e ) {
                AppUtils.Logger( TAG,
                        "Failed to create columns " + COLUMN_DONOR + ", " + COLUMN_RECEIVER + ". Most likely it already exists, which is fine." );
            }

            try {
                db.execSQL( "ALTER TABLE " + TABLE_RECEIPTS + " ADD COLUMN " + COLUMN_EXCH_RATE + " REAL" );
            } catch( SQLException e ) {
                AppUtils.Logger( TAG,
                        "Failed to create " + COLUMN_OPENED + " column. Most likely it already exists, which is fine." );
            }
        }

        //Se crea la nueva versión de la tabla
        //db.execSQL( sqlCreate );
    }
}
