package co.yodo.mobile.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import java.util.ArrayList;
import java.util.List;

import co.yodo.mobile.database.model.Receipt;
import co.yodo.mobile.helper.PrefUtils;
import co.yodo.mobile.helper.SystemUtils;

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
            ReceiptsSQLiteHelper.COLUMN_AUTHNUMBER,
            ReceiptsSQLiteHelper.COLUMN_DESCRIPTION,
            ReceiptsSQLiteHelper.COLUMN_TCURRENCY,
            ReceiptsSQLiteHelper.COLUMN_EXCH_RATE,
            ReceiptsSQLiteHelper.COLUMN_DCURRENCY,
            ReceiptsSQLiteHelper.COLUMN_AMOUNT,
            ReceiptsSQLiteHelper.COLUMN_TAMOUNT,
            ReceiptsSQLiteHelper.COLUMN_CASHBACK,
            ReceiptsSQLiteHelper.COLUMN_BALANCE,
            ReceiptsSQLiteHelper.COLUMN_CURRENCY,
            ReceiptsSQLiteHelper.COLUMN_DONOR,
            ReceiptsSQLiteHelper.COLUMN_RECEIVER,
            ReceiptsSQLiteHelper.COLUMN_CREATED,
            ReceiptsSQLiteHelper.COLUMN_OPENED
    };

    private boolean open = false;

    /** Receipts instance */
    private static ReceiptsDataSource instance = null;

    public ReceiptsDataSource( Context context ) {
        dbHelper = ReceiptsSQLiteHelper.getInstance( context );
    }

    public static ReceiptsDataSource getInstance( Context context ) {
        if( instance == null )
            instance = new ReceiptsDataSource( context );
        return instance;
    }

    public synchronized void open() throws SQLException {
        if( !open ) {
            database = dbHelper.getWritableDatabase();
            open = true;
        }
    }

    public synchronized void close() {
        if( dbHelper != null && open ) {
            dbHelper.close();
            open = false;
        }
    }

    public boolean isOpen() {
        return open;
    }

    public Receipt createReceipt( String authNumber, String description, String tCurrency, String exchRate,
                                  String dCurrency, String amount, String tAmount, String cashBack,
                                  String balance, String currency, String donor, String receiver, String created ) {

        ContentValues values = new ContentValues();
        values.put( ReceiptsSQLiteHelper.COLUMN_AUTHNUMBER,  authNumber );
        values.put( ReceiptsSQLiteHelper.COLUMN_DESCRIPTION, description );
        values.put( ReceiptsSQLiteHelper.COLUMN_TCURRENCY,   tCurrency );
        values.put( ReceiptsSQLiteHelper.COLUMN_EXCH_RATE,   exchRate );
        values.put( ReceiptsSQLiteHelper.COLUMN_DCURRENCY,   dCurrency );
        values.put( ReceiptsSQLiteHelper.COLUMN_AMOUNT,      amount );
        values.put( ReceiptsSQLiteHelper.COLUMN_TAMOUNT,     tAmount );
        values.put( ReceiptsSQLiteHelper.COLUMN_CASHBACK,    cashBack );
        values.put( ReceiptsSQLiteHelper.COLUMN_BALANCE,     balance );
        values.put( ReceiptsSQLiteHelper.COLUMN_CURRENCY,    currency );
        values.put( ReceiptsSQLiteHelper.COLUMN_DONOR,       donor );
        values.put( ReceiptsSQLiteHelper.COLUMN_RECEIVER,    receiver );
        values.put( ReceiptsSQLiteHelper.COLUMN_CREATED,     created );
        values.put( ReceiptsSQLiteHelper.COLUMN_OPENED,      false );

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
        values.put( ReceiptsSQLiteHelper.COLUMN_ID,          receipt.getId() );
        values.put( ReceiptsSQLiteHelper.COLUMN_AUTHNUMBER,  receipt.getAuthNumber() );
        values.put( ReceiptsSQLiteHelper.COLUMN_DESCRIPTION, receipt.getDescription() );
        values.put( ReceiptsSQLiteHelper.COLUMN_TCURRENCY,   receipt.getTCurrency() );
        values.put( ReceiptsSQLiteHelper.COLUMN_EXCH_RATE,   receipt.getExchRate() );
        values.put( ReceiptsSQLiteHelper.COLUMN_DCURRENCY,   receipt.getDCurrency() );
        values.put( ReceiptsSQLiteHelper.COLUMN_AMOUNT,      receipt.getTotalAmount() );
        values.put( ReceiptsSQLiteHelper.COLUMN_TAMOUNT,     receipt.getTenderAmount() );
        values.put( ReceiptsSQLiteHelper.COLUMN_CASHBACK,    receipt.getCashBackAmount() );
        values.put( ReceiptsSQLiteHelper.COLUMN_BALANCE,     receipt.getBalanceAmount() );
        values.put( ReceiptsSQLiteHelper.COLUMN_CURRENCY,    receipt.getCurrency() );
        values.put( ReceiptsSQLiteHelper.COLUMN_DONOR,       receipt.getDonorAccount() );
        values.put( ReceiptsSQLiteHelper.COLUMN_RECEIVER,    receipt.getReceiverAccount() );
        values.put( ReceiptsSQLiteHelper.COLUMN_CREATED,     receipt.getCreated() );
        values.put( ReceiptsSQLiteHelper.COLUMN_OPENED,      receipt.isOpened() );

        database.beginTransactionNonExclusive();

        try {
            long id = database.insert( ReceiptsSQLiteHelper.TABLE_RECEIPTS, null, values );
            SystemUtils.Logger( TAG, "Receipt deleted with id: " + id );
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
            SystemUtils.Logger( TAG, "Receipt deleted with id: " + id );
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    public void delete() {
        SystemUtils.Logger( TAG, "Receipts database deleted" );
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

    public long getAmount() {
        SQLiteStatement statement = database.compileStatement( "SELECT COUNT(*) FROM " + ReceiptsSQLiteHelper.TABLE_RECEIPTS );
        return statement.simpleQueryForLong();
    }

    private Receipt cursorToReceipt( Cursor cursor ) {
        Receipt receipt = new Receipt();
        receipt.setId( cursor.getLong( 0 ) );
        receipt.setAuthNumber( cursor.getString( 1 ) );
        receipt.setDescription( cursor.getString( 2 ) );
        receipt.setTCurrency( cursor.getString( 3 ) );
        receipt.setExchRate( cursor.getString( 4 ) );
        receipt.setDCurrency( cursor.getString( 5 ) );
        receipt.setTotalAmount( cursor.getString( 6 ) );
        receipt.setTenderAmount( cursor.getString( 7 ) );
        receipt.setCashBackAmount( cursor.getString( 8 ) );
        receipt.setBalanceAmount( cursor.getString( 9 ) );
        receipt.setCurrency( cursor.getString( 10 ) );

        // It only has donor and receiver for a heart transaction
        if( !cursor.isNull( 11 ) )
            receipt.setDonorAccount( cursor.getString( 11 ) );

        if( !cursor.isNull( 12 ) )
            receipt.setReceiverAccount( cursor.getString( 12 ) );

        receipt.setCreated( cursor.getString( 13 ) );
        receipt.setOpened( ( cursor.getInt( 14 ) != 0 ) );

        return receipt;
    }
}
