package co.yodo.mobile.database.contract;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

import co.yodo.mobile.BuildConfig;

/**
 * Created by hei on 05/08/16.
 * Basic API
 */
public final class YodoContract {
    /** Identifier for the provider */
    public static final String AUTHORITY = String.format( "%s.provider", BuildConfig.APPLICATION_ID );
    public static final Uri AUTHORITY_URI = new Uri.Builder()
            .scheme( ContentResolver.SCHEME_CONTENT )
            .authority( AUTHORITY )
            .build();

    public interface Receipt extends BaseColumns {
        /** Default Path */
        String PATH = "receipt";

        /** Receipts attributes names */
        String COLUMN_AUTHNUMBER  = "authNumber";  // Receipt identifier
        String COLUMN_DESCRIPTION = "description"; // Merchant description
        String COLUMN_TCURRENCY   = "tcurrency";   // Merchant currency
        String COLUMN_EXCH_RATE   = "exchRate";
        String COLUMN_DCURRENCY   = "dcurrency";   // Tender currency
        String COLUMN_AMOUNT      = "amount";      // Total amount
        String COLUMN_TAMOUNT     = "tAmount";     // Tender amount
        String COLUMN_CASHBACK    = "cashBack";    // Cashback amount
        String COLUMN_BALANCE     = "balance";     // Account balance
        String COLUMN_CURRENCY    = "currency";    // Account currency
        String COLUMN_DONOR       = "donor";
        String COLUMN_RECEIVER    = "receiver";
        String COLUMN_CREATED     = "created";
        String COLUMN_OPENED      = "opened";

        Uri CONTENT_URI = Uri.withAppendedPath( AUTHORITY_URI, PATH );
    }

    public interface Coupon extends BaseColumns {
        /** Default Path */
        String PATH = "coupon";

        /** Coupons attributes names */
        String COLUMN_DESCRIPTION = "description";
        String COLUMN_URL         = "url";
        String COLUMN_CREATED     = "created";

        Uri CONTENT_URI = Uri.withAppendedPath( AUTHORITY_URI, PATH );
    }
}
