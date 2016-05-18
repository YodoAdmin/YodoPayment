package co.yodo.mobile.helper;

import android.app.Activity;
import android.support.v7.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Rect;
import android.view.Window;
import android.webkit.WebView;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;

import co.yodo.mobile.R;

/**
 * Created by luis on 24/01/15.
 * Eula for the YodoPayment
 */
public class AppEula {
    /** DEBUG */
    private final static String TAG = AppEula.class.getSimpleName();

    /** File name */
    private static final String ASSET_EULA = "EULA";

    public static interface OnEulaAgreedTo {
        void onEulaAgreedTo();
    }

    public static boolean show( final Activity activity ) {
        if( !AppUtils.isEulaAccepted( activity ) ) {
            final AlertDialog.Builder builder = new AlertDialog.Builder( activity, R.style.AppCompatAlertDialogStyle );
            builder.setTitle( R.string.eula_title );
            builder.setCancelable( false );

            builder.setPositiveButton( R.string.eula_accept, new DialogInterface.OnClickListener() {
                public void onClick( DialogInterface dialog, int which) {
                    AppUtils.saveEulaAccepted( activity, true );

                    if( activity instanceof OnEulaAgreedTo ) {
                        ((OnEulaAgreedTo) activity).onEulaAgreedTo();
                    }
                }
            });

            builder.setNegativeButton(R.string.eula_refuse, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    activity.finish();
                }
            });

            WebView eulaText = new WebView( activity );

            String text = "<html><body>"
                    + "<p align=\"justify\">"
                    + readEula( activity )
                    + "</p> "
                    + "</body></html>";
            eulaText.loadData( text, "text/html", "UTF-8" );

            Rect displayRectangle = new Rect();
            Window window = activity.getWindow();
            window.getDecorView().getWindowVisibleDisplayFrame( displayRectangle );

            builder.setView( eulaText );
            AlertDialog alert = builder.create();
            alert.show();
            alert.getWindow().setLayout( displayRectangle.width(), (int)( displayRectangle.height() * 0.7f ) );

            return true;
        }
        return false;
    }

    private static CharSequence readEula(Activity activity) {
        BufferedReader in = null;
        try {
            in = new BufferedReader( new InputStreamReader( activity.getAssets().open( ASSET_EULA ) ) );
            String line;
            StringBuilder buffer = new StringBuilder();

            while( ( line = in.readLine() ) != null )
                buffer.append( line ).append( '\n' );

            return buffer;
        } catch( IOException e ) {
            AppUtils.Logger( TAG, e.getMessage() );
            return "";
        } finally {
            closeStream( in );
        }
    }

    private static void closeStream(Closeable stream) {
        if( stream != null ) {
            try {
                stream.close();
            } catch (IOException e) {
                AppUtils.Logger( TAG, e.getMessage() );
            }
        }
    }
}
