package co.yodo.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by luis on 5/07/13.
 */
public class ReceiptsSQLiteHelper extends SQLiteOpenHelper {
    public static final String TABLE_RECEIPTS = "Receipts";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_CREATED = "created";
    public static final String COLUMN_AMOUNT = "amount";
    public static final String COLUMN_TENDER = "tender";
    public static final String COLUMN_CASHBACK = "cashback";
    public static final String COLUMN_AUTHNUMBER = "transauthnumber";
    public static final String COLUMN_BALANCE = "yodobalance";

    //Sentencia SQL para crear la tabla de Usuarios
    String sqlCreate = "CREATE TABLE " + TABLE_RECEIPTS + " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + 
    		COLUMN_DESCRIPTION + " TEXT, " + COLUMN_CREATED + " TEXT, " + COLUMN_AMOUNT + " REAL, " + 
    		COLUMN_TENDER + " REAL, " + COLUMN_CASHBACK + " REAL, " + COLUMN_AUTHNUMBER + " INTEGER, " + 
    		COLUMN_BALANCE + " REAL)";

    public ReceiptsSQLiteHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //Se ejecuta la sentencia SQL de creación de la tabla
        db.execSQL(sqlCreate);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //Se elimina la versión anterior de la tabla
        //db.execSQL("DROP TABLE IF EXISTS Informations");

        //Se crea la nueva versión de la tabla
        db.execSQL(sqlCreate);
    }
}
