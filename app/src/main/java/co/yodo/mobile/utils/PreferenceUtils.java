package co.yodo.mobile.utils;

import android.os.Environment;

import java.io.File;

import co.yodo.mobile.helper.AppConfig;
import co.yodo.mobile.helper.PrefUtils;
import co.yodo.mobile.model.db.Coupon;
import co.yodo.mobile.model.db.Receipt;

/**
 * Created by hei on 08/03/17.
 * Helper for the preferences
 */
public class PreferenceUtils {
    /**
     * Clears all the stored information
     */
    public static void clearUserData() {
        PrefUtils.clearPrefConfig();
        Receipt.deleteAll( Receipt.class );
        //Coupon.deleteAll( Coupon.class );
        SystemUtils.deleteDir(
                new File( Environment.getExternalStorageDirectory(), AppConfig.COUPONS_FOLDER )
        );
    }
}
