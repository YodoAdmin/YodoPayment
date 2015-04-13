package co.yodo.mobile.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by luis on 1/02/15.
 * Helper for the receipts
 */
public class ReceiptsSQLiteHelper extends SQLiteOpenHelper {
    public static final String TABLE_RECEIPTS     = "Receipts";
    public static final String COLUMN_ID          = "id";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_AUTHNUMBER  = "authNumber";
    public static final String COLUMN_CURRENCY    = "dcurrency";
    public static final String COLUMN_AMOUNT      = "amount";
    public static final String COLUMN_TAMOUNT     = "tAmount";
    public static final String COLUMN_CASHBACK    = "cashBack";
    public static final String COLUMN_BALANCE     = "balance";
    public static final String COLUMN_CREATED     = "created";

    private static final String DATABASE_NAME = "receipts.db";
    private static final int DATABASE_VERSION = 2;

    //Sentencia SQL para crear la tabla de Usuarios
    String sqlCreate = "CREATE TABLE " + TABLE_RECEIPTS + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_DESCRIPTION + " TEXT, " + COLUMN_AUTHNUMBER  + " INTEGER, " +
            COLUMN_CURRENCY    + " TEXT, " +
            COLUMN_AMOUNT      + " REAL, " + COLUMN_TAMOUNT     + " REAL, " +
            COLUMN_CASHBACK    + " REAL, " + COLUMN_BALANCE     + " REAL, " +
            COLUMN_CREATED     + " TEXT)";

    public ReceiptsSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //Se ejecuta la sentencia SQL de creación de la tabla
        db.execSQL( sqlCreate );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //Se elimina la versión anterior de la tabla
        //db.execSQL( "DROP TABLE IF EXISTS Receipts" );
        if( newVersion > oldVersion ) {
            db.execSQL( "ALTER TABLE " + TABLE_RECEIPTS + " ADD COLUMN dcurrency TEXT" );
        }

        //Se crea la nueva versión de la tabla
        //db.execSQL( sqlCreate );
    }
}
