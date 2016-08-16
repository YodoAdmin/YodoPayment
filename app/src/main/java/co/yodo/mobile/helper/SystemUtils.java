package co.yodo.mobile.helper;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.io.File;

import co.yodo.mobile.R;
import co.yodo.mobile.ui.notification.AlertDialogHelper;
import co.yodo.mobile.ui.notification.ToastMaster;

/**
 * Created by hei on 10/06/16.
 * Any system requirement like permissions,
 * google services or logger
 */
public class SystemUtils {
    /**
     * Method to verify google play services on the device
     * @param activity The activity that
     * @param code The code for the activity result
     * */
    public static boolean isGooglePlayServicesAvailable( Activity activity, int code ) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable( activity );
        if( resultCode != ConnectionResult.SUCCESS ) {
            if( apiAvailability.isUserResolvableError( resultCode ) ) {
                apiAvailability.getErrorDialog( activity, resultCode, code ).show();
            } else {
                ToastMaster.makeText( activity, R.string.error_not_supported, Toast.LENGTH_LONG ).show();
                activity.finish();
            }
            return false;
        }
        return true;
    }

    /**
     * Requests a permission for the use of a phone's characteristic (e.g. Camera, Phone info, etc)
     * @param ac The application context
     * @param message A message to request the permission
     * @param permission The permission
     * @param requestCode The request code for the result
     * @return If the permission was already allowed or not
     */
    public static boolean requestPermission( final Activity ac, final int message, final String permission, final int requestCode ) {
        // Assume thisActivity is the current activity
        int permissionCheck = ContextCompat.checkSelfPermission( ac, permission );
        if( permissionCheck != PackageManager.PERMISSION_GRANTED ) {
            if( ActivityCompat.shouldShowRequestPermissionRationale( ac, permission ) ) {
                DialogInterface.OnClickListener onClick = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick( DialogInterface dialog, int which ) {
                        ActivityCompat.requestPermissions(
                                ac,
                                new String[]{permission},
                                requestCode
                        );
                    }
                };

                AlertDialogHelper.create(
                        ac,
                        null,
                        message,
                        onClick
                ).show();
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions( ac, new String[]{permission}, requestCode );
            }
            return false;
        }
        return true;
    }

    /**
     * For to Delete the directory inside list of files and inner Directory
     * @param dir The directory
     * @return True if it was success or false if not
     */
    public static boolean deleteDir( File dir ) {
        if( dir.isDirectory() ) {
            String[] children = dir.list();
            for( String aChildren : children ) {
                boolean success = deleteDir( new File( dir, aChildren ) );
                if( !success )
                    return false;
            }
        }

        // The directory is now empty so delete it
        return dir.delete();
    }

    /**
     * Logger for Android
     * @param TAG The String of the TAG for the log
     * @param text The text to print on the log
     */
    public static void Logger( String TAG, String text ) {
        if( AppConfig.DEBUG ) {
            if( text == null )
                Log.e( TAG, "Null Text" );
            else
                Log.e( TAG, text );
        }
    }
}