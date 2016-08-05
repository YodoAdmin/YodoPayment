package co.yodo.mobile.database.provider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.util.SparseArray;

import javax.inject.Inject;

import co.yodo.mobile.YodoApplication;
import co.yodo.mobile.database.contract.YodoContract;

/**
 * Created by hei on 05/08/16.
 * Provides access to Yodo data.
 */
public class YodoProvider extends ContentProvider {
    /** DEBUG */
    @SuppressWarnings( "unused" )
    private static final String TAG = YodoProvider.class.getSimpleName();

    /** URI codes */
    private static final int CODE_ALL_RECEIPTS = 100;
    private static final int CODE_RECEIPT_ID    = 101;

    private static final int CODE_ALL_COUPONS = 200;
    private static final int CODE_COUPON_ID   = 201;

    private static final SparseArray<String> URI_CODE_TABLE_MAP =
            new SparseArray<>();

    private static final UriMatcher URI_MATCHER =
            new UriMatcher( UriMatcher.NO_MATCH);

    static {
        URI_CODE_TABLE_MAP.put( CODE_ALL_RECEIPTS, YodoSQLiteHelper.Tables.RECEIPTS );
        URI_CODE_TABLE_MAP.put( CODE_RECEIPT_ID, YodoSQLiteHelper.Tables.RECEIPTS );

        URI_CODE_TABLE_MAP.put( CODE_ALL_COUPONS, YodoSQLiteHelper.Tables.COUPONS );
        URI_CODE_TABLE_MAP.put( CODE_COUPON_ID, YodoSQLiteHelper.Tables.COUPONS );

        URI_MATCHER.addURI( YodoContract.AUTHORITY,
                YodoContract.Receipt.PATH,
                CODE_ALL_RECEIPTS );

        URI_MATCHER.addURI( YodoContract.AUTHORITY,
                YodoContract.Receipt.PATH + "/#",
                CODE_RECEIPT_ID );

        URI_MATCHER.addURI( YodoContract.AUTHORITY,
                YodoContract.Coupon.PATH,
                CODE_ALL_COUPONS );

        URI_MATCHER.addURI( YodoContract.AUTHORITY,
                YodoContract.Coupon.PATH + "/#",
                CODE_COUPON_ID );
    }

    @Inject
    YodoSQLiteHelper mHelper;

    @Override
    public boolean onCreate() {
        // Injection
        return true;
    }

    @Override
    public Cursor query( @NonNull Uri uri, String[] strings, String s, String[] strings1, String s1 ) {
        return null;
    }

    @Override
    public String getType( @NonNull Uri uri ) {
        final int code = URI_MATCHER.match( uri );

        switch( code ) {
            case CODE_ALL_RECEIPTS:
                return String.format( "%s/vnd.%s.%s",
                        ContentResolver.CURSOR_DIR_BASE_TYPE,
                        YodoContract.AUTHORITY,
                        YodoContract.Receipt.PATH
                );

            case CODE_ALL_COUPONS:
                return String.format( "%s/vnd.%s.%s",
                        ContentResolver.CURSOR_DIR_BASE_TYPE,
                        YodoContract.AUTHORITY,
                        YodoContract.Receipt.PATH
                );

            case CODE_RECEIPT_ID:
                return String.format( "%s/vnd.%s.%s",
                        ContentResolver.CURSOR_ITEM_BASE_TYPE,
                        YodoContract.AUTHORITY,
                        YodoContract.Coupon.PATH
                );

            case CODE_COUPON_ID:
                return String.format( "%s/vnd.%s.%s",
                        ContentResolver.CURSOR_ITEM_BASE_TYPE,
                        YodoContract.AUTHORITY,
                        YodoContract.Coupon.PATH
                );

            default:
                return null;
        }
    }

    @Override
    public Uri insert( @NonNull Uri uri, ContentValues values ) {
        long id;
        final int code = URI_MATCHER.match( uri );

        switch( code ) {
            case CODE_ALL_RECEIPTS:
            case CODE_ALL_COUPONS:
                id = mHelper.getWritableDatabase().insertOrThrow(
                        URI_CODE_TABLE_MAP.get( code ),
                        null,
                        values
                );
                break;
            default:
                throw new IllegalArgumentException( "Invalid Uri: " + uri );
        }

        return ContentUris.withAppendedId( uri, id );
    }

    @Override
    public int delete( @NonNull Uri uri, String selection, String[] selectionArgs ) {
        int rowCount;
        final int code = URI_MATCHER.match( uri );

        switch( code ) {
            case CODE_ALL_RECEIPTS:
            case CODE_ALL_COUPONS:
                rowCount = mHelper.getWritableDatabase().delete(
                        URI_CODE_TABLE_MAP.get( code ),
                        selection,
                        selectionArgs
                );
                break;

            case CODE_RECEIPT_ID:
            case CODE_COUPON_ID:
                if( selection == null && selectionArgs == null ) {
                    selection = BaseColumns._ID + " = ?";
                    selectionArgs = new String[] {
                            uri.getLastPathSegment()
                    };

                    rowCount = mHelper.getWritableDatabase().delete(
                            URI_CODE_TABLE_MAP.get( code ),
                            selection,
                            selectionArgs
                    );
                } else {
                    throw new IllegalArgumentException(
                            "Selection must be null when specifying ID as part of uri"
                    );
                }
                break;
            default:
                throw new IllegalArgumentException( "Invalid Uri: " + uri );
        }
        return rowCount;
    }

    @Override
    public int update( @NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs ) {
        int rowCount;
        final int code = URI_MATCHER.match( uri );

        switch( code ) {
            case CODE_ALL_RECEIPTS:
            case CODE_ALL_COUPONS:
                rowCount = mHelper.getWritableDatabase().update(
                        URI_CODE_TABLE_MAP.get( code ),
                        values,
                        selection,
                        selectionArgs
                );
                break;

            case CODE_RECEIPT_ID:
            case CODE_COUPON_ID:
                if( selection == null && selectionArgs == null ) {
                    selection = BaseColumns._ID + " = ?";
                    selectionArgs = new String[] {
                            uri.getLastPathSegment()
                    };
                } else {
                    throw new IllegalArgumentException(
                            "Selection must be null when specifying ID as part of uri"
                    );
                }

                rowCount = mHelper.getWritableDatabase().update(
                        URI_CODE_TABLE_MAP.get( code ),
                        values,
                        selection,
                        selectionArgs
                );
                break;
            default:
                throw new IllegalArgumentException( "Invalid Uri: " + uri );
        }

        return rowCount;
    }
}
