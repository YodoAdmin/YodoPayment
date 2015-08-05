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
            ReceiptsSQLiteHelper.COLUMN_CURRENCY,
            ReceiptsSQLiteHelper.COLUMN_AMOUNT,
            ReceiptsSQLiteHelper.COLUMN_TAMOUNT,
            ReceiptsSQLiteHelper.COLUMN_CASHBACK,
            ReceiptsSQLiteHelper.COLUMN_BALANCE,
            ReceiptsSQLiteHelper.COLUMN_CREATED,
            ReceiptsSQLiteHelper.COLUMN_OPENED
    };

    public ReceiptsDataSource(Context context) {
        dbHelper = ReceiptsSQLiteHelper.getInstance( context );
    }

    public synchronized void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public synchronized void close() {
        if( dbHelper != null )
            dbHelper.close();
    }

    public Receipt createReceipt(String description, String authNumber, String currency, String amount,
                                 String tAmount, String cashBack, String balance, String created) {

        ContentValues values = new ContentValues();
        values.put( ReceiptsSQLiteHelper.COLUMN_DESCRIPTION, description );
        values.put( ReceiptsSQLiteHelper.COLUMN_AUTHNUMBER, authNumber );
        values.put( ReceiptsSQLiteHelper.COLUMN_CURRENCY, currency );
        values.put( ReceiptsSQLiteHelper.COLUMN_AMOUNT, amount );
        values.put( ReceiptsSQLiteHelper.COLUMN_TAMOUNT, tAmount );
        values.put( ReceiptsSQLiteHelper.COLUMN_CASHBACK, cashBack );
        values.put( ReceiptsSQLiteHelper.COLUMN_BALANCE, balance );
        values.put( ReceiptsSQLiteHelper.COLUMN_CREATED, created );
        values.put( ReceiptsSQLiteHelper.COLUMN_OPENED, false );

        database.beginTransactionNonExclusive();
        try {
            long insertId = database.insert( ReceiptsSQLiteHelper.TABLE_RECEIPTS, null, values );
            database.setTransactionSuccessful();

            Cursor cursor = database.query(
                    ReceiptsSQLiteHelper.TABLE_RECEIPTS,
                    allColumns,
                    ReceiptsSQLiteHelper.COLUMN_ID + " = " + insertId,
                    null, null, null, null );
            cursor.moveToFirst();
            Receipt newReceipt = cursorToReceipt( cursor );
            cursor.close();

            return newReceipt;
        } finally {
            database.endTransaction();
        }
    }

    /**
     * Adds a receipt to the database
     * @param receipt The Receipt
     */
    public void addReceipt( Receipt receipt ) {
        ContentValues values = new ContentValues();
        values.put( ReceiptsSQLiteHelper.COLUMN_ID, receipt.getId() );
        values.put( ReceiptsSQLiteHelper.COLUMN_DESCRIPTION, receipt.getDescription() );
        values.put( ReceiptsSQLiteHelper.COLUMN_AUTHNUMBER, receipt.getAuthNumber() );
        values.put( ReceiptsSQLiteHelper.COLUMN_CURRENCY, receipt.getCurrency() );
        values.put( ReceiptsSQLiteHelper.COLUMN_AMOUNT, receipt.getTotalAmount() );
        values.put( ReceiptsSQLiteHelper.COLUMN_TAMOUNT, receipt.getTenderAmount() );
        values.put( ReceiptsSQLiteHelper.COLUMN_CASHBACK, receipt.getCashBackAmount() );
        values.put( ReceiptsSQLiteHelper.COLUMN_BALANCE, receipt.getBalanceAmount() );
        values.put( ReceiptsSQLiteHelper.COLUMN_CREATED, receipt.getCreated() );
        values.put( ReceiptsSQLiteHelper.COLUMN_OPENED, receipt.isOpened() );

        database.beginTransactionNonExclusive();

        try {
            long id = database.insert( ReceiptsSQLiteHelper.TABLE_RECEIPTS, null, values );
            AppUtils.Logger( TAG, "Receipt deleted with id: " + id );
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    /**
     * Updates a Receipt (only if it is opened)
     * @param item The Receipt
     */
    public void updateReceipt( Receipt item ) {
        ContentValues values = new ContentValues();
        values.put( ReceiptsSQLiteHelper.COLUMN_OPENED, item.isOpened() );
        database.beginTransaction();

        try {
            database.update( ReceiptsSQLiteHelper.TABLE_RECEIPTS,
                    values,
                    ReceiptsSQLiteHelper.COLUMN_ID + "=" + item.getId(),
                    null );
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    /**
     * Deletes a receipt from the database
     * @param receipt The Receipt
     */
    public void deleteReceipt( Receipt receipt ) {
        long id = receipt.getId();
        database.beginTransaction();

        try {
            database.delete(
                ReceiptsSQLiteHelper.TABLE_RECEIPTS,
                ReceiptsSQLiteHelper.COLUMN_ID + " = " + id, null );
            AppUtils.Logger( TAG, "Receipt deleted with id: " + id );
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
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
                allColumns, null, null, null, null, ReceiptsSQLiteHelper.COLUMN_CREATED + " DESC" );

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
        receipt.setCurrency(       cursor.getString( 3 ) );
        receipt.setTotalAmount(    cursor.getString( 4 ) );
        receipt.setTenderAmount(   cursor.getString( 5 ) );
        receipt.setCashBackAmount( cursor.getString( 6 ) );
        receipt.setBalanceAmount(  cursor.getString( 7 ) );
        receipt.setCreated(        cursor.getString( 8 ) );
        receipt.setOpened(       ( cursor.getInt( 9 ) != 0) );

        return receipt;
    }
}
