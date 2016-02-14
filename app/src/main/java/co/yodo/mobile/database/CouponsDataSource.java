package co.yodo.mobile.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import co.yodo.mobile.data.Coupon;
import co.yodo.mobile.helper.AppUtils;

public class CouponsDataSource {
	/** DEBUG */
	private final static String TAG = CouponsDataSource.class.getSimpleName();

	/** Date format */
	private final static String date_format = "yyyy-MM-dd HH:mm:ss";

	/** Database fields */
	private SQLiteDatabase database;
	private CouponsSQLiteHelper dbHelper;
	private String[] allColumns = {
            CouponsSQLiteHelper.COLUMN_ID,
            CouponsSQLiteHelper.COLUMN_URL,
			CouponsSQLiteHelper.COLUMN_DESCRIPTION,
            CouponsSQLiteHelper.COLUMN_CREATED
    };

	public CouponsDataSource(Context context) {
		dbHelper = new CouponsSQLiteHelper( context );
	}

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		if( dbHelper != null )
			dbHelper.close();
	}

	public Coupon createCoupon(String url, String description) {
		SimpleDateFormat date = new SimpleDateFormat( date_format, java.util.Locale.getDefault() );

		ContentValues values = new ContentValues();
	    values.put( CouponsSQLiteHelper.COLUMN_URL, url );
	    values.put( CouponsSQLiteHelper.COLUMN_DESCRIPTION, description );
	    values.put( CouponsSQLiteHelper.COLUMN_CREATED, date.format( new Date() ) );

	    long insertId = database.insert( CouponsSQLiteHelper.TABLE_COUPONS, null, values );
	    Cursor cursor = database.query( CouponsSQLiteHelper.TABLE_COUPONS,
	        allColumns, CouponsSQLiteHelper.COLUMN_ID + " = " + insertId, null,
	        null, null, null );
	    cursor.moveToFirst();
	    Coupon newCoupon = cursorToCoupon( cursor );
	    cursor.close();

	    return newCoupon;
	}

	public void deleteCoupon(Coupon coupon) {
	    long id = coupon.getId();
	    AppUtils.Logger( TAG, "Coupon deleted with id: " + id );
	    database.delete( CouponsSQLiteHelper.TABLE_COUPONS, CouponsSQLiteHelper.COLUMN_ID
				+ " = " + id, null );
	}

	public void delete() {
        AppUtils.Logger( TAG, "Coupons database deleted" );
		database.delete( CouponsSQLiteHelper.TABLE_COUPONS, null, null );
		database.close();
	}

	public List<Coupon> getAllCoupons() {
	    List<Coupon> coupons = new ArrayList<>();

	    Cursor cursor = database.query( CouponsSQLiteHelper.TABLE_COUPONS,
	        allColumns, null, null, null, null, null );

	    cursor.moveToFirst();
	    while( !cursor.isAfterLast() ) {
	    	Coupon coupon = cursorToCoupon( cursor );
	    	File file     = new File( coupon.getUrl() );

	    	if( file.exists() )
	    		coupons.add( coupon );
	    	else
	    		deleteCoupon( coupon );
	    	cursor.moveToNext();
	    }
	    // make sure to close the cursor
	    cursor.close();
	    return coupons;
	}

	public long getAmount() {
		SQLiteStatement statement = database.compileStatement( "SELECT COUNT(*) FROM " + CouponsSQLiteHelper.TABLE_COUPONS );
		return statement.simpleQueryForLong();
	}

	private Coupon cursorToCoupon(Cursor cursor) {
		Coupon coupon = new Coupon();
		coupon.setId(          cursor.getLong( 0 ) );
		coupon.setUrl(         cursor.getString( 1 ) );
		coupon.setDescription( cursor.getString( 2 ) );
		coupon.setCreated(     cursor.getString( 3 ) );

	    return coupon;
	}
}
