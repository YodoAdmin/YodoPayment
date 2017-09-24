package co.yodo.mobile.helper;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Rect;
import android.support.v7.app.AlertDialog;
import android.view.Window;
import android.webkit.WebView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import co.yodo.mobile.R;

/**
 * Created by luis on 24/01/15.
 * Eula for the YodoPayment
 */
public class EulaHelper {
    /** File name */
    private static final String ASSET_EULA = "EULA";

    public interface EulaCallback {
        /**
         * When the user accepts the
         * EULA agreement
         */
        void onEulaAgreedTo();
    }

    public static void show(final Activity activity, final EulaCallback callback) {
        if (!PreferencesHelper.isEulaAccepted()) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle(R.string.text_eula_title);
            builder.setCancelable(false);

            builder.setPositiveButton(R.string.text_eula_accept, new DialogInterface.OnClickListener() {
                public void onClick( DialogInterface dialog, int which ) {
                    PreferencesHelper.saveEulaAccepted(true);
                    if (callback != null) {
                        callback.onEulaAgreedTo();
                    }
                }
            } );

            builder.setNegativeButton(R.string.text_eula_refuse, new DialogInterface.OnClickListener() {
                public void onClick( DialogInterface dialog, int which ) {
                    activity.finish();
                }
            } );

            // Setup GUI
            WebView eulaText = new WebView(activity);
            String text = "<html><body>"
                    + "<p align=\"justify\">"
                    + readEula(activity)
                    + "</p> "
                    + "</body></html>";
            eulaText.loadData(text, "text/html", "UTF-8");
            builder.setView(eulaText);

            Rect displayRectangle = new Rect();
            Window window = activity.getWindow();
            window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);

            AlertDialog alertDialog = builder.show();
            window = alertDialog.getWindow();
            if (window != null) {
                window.setLayout(displayRectangle.width(), (int) (displayRectangle.height() * 0.7f));
            }
        } else {
            if (callback != null) {
                callback.onEulaAgreedTo();
            }
        }
    }

    /**
     * Reads the EULA from the assets
     * @param context, The application context
     * @return The text from the file
     */
    private static CharSequence readEula(Context context) {
        StringBuilder buffer = new StringBuilder();
        BufferedReader in = null;

        try {
            in = new BufferedReader(new InputStreamReader(context.getAssets().open(ASSET_EULA)));
            String line;
            while((line = in.readLine()) != null) {
                buffer.append(line).append('\n');
            }
        } catch(IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return buffer;
    }
}
