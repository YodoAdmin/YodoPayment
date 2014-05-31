package co.yodo.helper;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.view.Window;
import android.webkit.WebView;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Closeable;

import co.yodo.R;
import co.yodo.main.YodoRegistration;

public class Eula {
	private static final String ASSET_EULA = "EULA";

	static interface OnEulaAgreedTo {
			void onEulaAgreedTo();
	}

	public static boolean show(final Activity activity) {
		final SharedPreferences preferences = activity.getSharedPreferences(YodoGlobals.PREFERENCES_EULA, Activity.MODE_PRIVATE);
		if(!preferences.getBoolean(YodoGlobals.PREFERENCE_EULA_ACCEPTED, false)) {
			final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
			builder.setTitle(R.string.eula_title);
			builder.setCancelable(true);
			builder.setPositiveButton(R.string.eula_accept, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					accept(preferences);
					((YodoRegistration) activity).loadInstructions();
					
					if(activity instanceof OnEulaAgreedTo) {
						((OnEulaAgreedTo) activity).onEulaAgreedTo();
					}
				}
			});
			
			builder.setNegativeButton(R.string.eula_refuse, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					refuse(activity);
				}
			});
			
			builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					refuse(activity);
				}
			});
			
			WebView eulaText = new WebView(activity);

			String text = "<html><body>"
						+ "<p align=\"justify\">" 
						+ readEula(activity)
						+ "</p> "
						+ "</body></html>";
			eulaText.loadData(text, "text/html", "UTF-8");
			
			Rect displayRectangle = new Rect();
			Window window = activity.getWindow();
			window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);
			
			builder.setView(eulaText);
			AlertDialog alert = builder.create();
			alert.show();
			alert.getWindow().setLayout(displayRectangle.width(), (int)(displayRectangle.height() * 0.7f));
			
			return false;
		}
		return true;
	}
	
	private static void accept(SharedPreferences preferences) {
		preferences.edit().putBoolean(YodoGlobals.PREFERENCE_EULA_ACCEPTED, true).commit();
	}

	private static void refuse(Activity activity) {
		activity.finish();
	}

	private static CharSequence readEula(Activity activity) {
		BufferedReader in = null;
		try {	
			in = new BufferedReader(new InputStreamReader(activity.getAssets().open(ASSET_EULA)));
			String line;
			StringBuilder buffer = new StringBuilder();
			
			while((line = in.readLine()) != null) 
				buffer.append(line).append('\n');
			return buffer;
		} catch (IOException e) {
			return "";
		} finally {
			closeStream(in);
		}
	}

	private static void closeStream(Closeable stream) {
		if(stream != null) {
			try {
				stream.close();
			} catch (IOException e) {
				/// Ignore
			}
		}
	}
}

