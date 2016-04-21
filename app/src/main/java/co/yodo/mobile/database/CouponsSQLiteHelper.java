package co.yodo.mobile.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by luis on 5/07/13.
 * Database helper for the sqlite
 */
public class CouponsSQLiteHelper extends SQLiteOpenHelper {
    public static final String TABLE_COUPONS      = "Coupons";
    public static final String COLUMN_ID          = "id";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_URL         = "url";
    public static final String COLUMN_CREATED     = "created";
    
    private static final String DATABASE_NAME = "coupons.db";
    private static final int DATABASE_VERSION = 1;

    //Sentencia SQL para crear la tabla de Usuarios
    String sqlCreate = "CREATE TABLE " + TABLE_COUPONS + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
    		COLUMN_DESCRIPTION + " TEXT, " + COLUMN_URL + " TEXT, " +
            COLUMN_CREATED     + " DATETIME)";

    public CouponsSQLiteHelper(Context context) {
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
        //db.execSQL( "DROP TABLE IF EXISTS Coupons" );

        //Se crea la nueva versión de la tabla
        db.execSQL( sqlCreate );
    }
}
