package co.yodo.mobile.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import co.yodo.mobile.data.Receipt;
import co.yodo.mobile.helper.AppUtils;

/**
 * Created by luis on 1/02/15.
 * Helps to handle the data base information
 */
public class ReceiptsDataSource {
    /** DEBUG */
    private final static String TAG = ReceiptsDataSource.class.getSimpleName();

    /** Database fields */
    private SQLiteDatabase database;
    private ReceiptsSQLiteHelper dbHelper;
    private String[] allColumns = {
            ReceiptsSQLiteHelper.COLUMN_ID,
            ReceiptsSQLiteHelper.COLUMN_DESCRIPTION,
            ReceiptsSQLiteHelper.COLUMN_AUTHNUMBER,
            ReceiptsSQLiteHelper.COLUMN_AMOUNT,
            ReceiptsSQLiteHelper.COLUMN_TAMOUNT,
            ReceiptsSQLiteHelper.COLUMN_CASHBACK,
            ReceiptsSQLiteHelper.COLUMN_BALANCE,
            ReceiptsSQLiteHelper.COLUMN_CREATED
    };

    public ReceiptsDataSource(Context context) {
        dbHelper = new ReceiptsSQLiteHelper( context );
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        if( dbHelper != null )
            dbHelper.close();
    }

    public Receipt createReceipt(String description, String authNumber, String amount,
                                 String tAmount, String cashBack, String balance, String created) {

        ContentValues values = new ContentValues();
        values.put( ReceiptsSQLiteHelper.COLUMN_DESCRIPTION, description );
        values.put( ReceiptsSQLiteHelper.COLUMN_AUTHNUMBER, authNumber );
        values.put( ReceiptsSQLiteHelper.COLUMN_AMOUNT, amount );
        values.put( ReceiptsSQLiteHelper.COLUMN_TAMOUNT, tAmount );
        values.put( ReceiptsSQLiteHelper.COLUMN_CASHBACK, cashBack );
        values.put( ReceiptsSQLiteHelper.COLUMN_BALANCE, balance );
        values.put( ReceiptsSQLiteHelper.COLUMN_CREATED, created );

        long insertId = database.insert( ReceiptsSQLiteHelper.TABLE_RECEIPTS, null, values );
        Cursor cursor = database.query(
                ReceiptsSQLiteHelper.TABLE_RECEIPTS,
                allColumns,
                ReceiptsSQLiteHelper.COLUMN_ID + " = " + insertId,
                null, null, null, null );
        cursor.moveToFirst();
        Receipt newReceipt = cursorToReceipt(cursor);
        cursor.close();

        return newReceipt;
    }

    public void deleteReceipt(Receipt receipt) {
        long id = receipt.getId();
        AppUtils.Logger(TAG, "Receipt deleted with id: " + id);
        database.delete(
                ReceiptsSQLiteHelper.TABLE_RECEIPTS,
                ReceiptsSQLiteHelper.COLUMN_ID + " = " + id, null );
    }

    public void delete() {
        AppUtils.Logger( TAG, "Receipts database deleted" );
        database.delete( ReceiptsSQLiteHelper.TABLE_RECEIPTS, null, null );
        database.close();
    }

    public List<Receipt> getAllReceipts() {
        List<Receipt> receipts = new ArrayList<>();

        Cursor cursor = database.query(
                ReceiptsSQLiteHelper.TABLE_RECEIPTS,
                allColumns, null, null, null, null, null );

        cursor.moveToFirst();
        while( !cursor.isAfterLast() ) {
            Receipt receipt = cursorToReceipt( cursor );
            receipts.add( receipt );
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return receipts;
    }

    private Receipt cursorToReceipt(Cursor cursor) {
        Receipt receipt = new Receipt();
        receipt.setId(             cursor.getLong( 0 ) );
        receipt.setDescription(    cursor.getString( 1 ) );
        receipt.setAuthNumber(     cursor.getString( 2 ) );
        receipt.setTotalAmount(    cursor.getString( 3 ) );
        receipt.setTenderAmount(   cursor.getString( 4 ) );
        receipt.setCashBackAmount( cursor.getString( 5 ) );
        receipt.setBalanceAmount(  cursor.getString( 6 ) );
        receipt.setCreated(        cursor.getString( 7 ) );

        return receipt;
    }
}
