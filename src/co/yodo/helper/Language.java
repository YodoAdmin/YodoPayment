package co.yodo.helper;

import java.util.Arrays;
import java.util.Locale;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;

public class Language {
	 /**
	 * Change the language of the application
	 */
	public static void changeLanguage(Context context) {
		SharedPreferences settings = context.getSharedPreferences(YodoGlobals.PREFERENCES, Context.MODE_PRIVATE);
		String code = context.getResources().getConfiguration().locale.getLanguage();
		int languagePosition = settings.getInt(YodoGlobals.ID_LANGUAGE, YodoGlobals.DEFAULT_LANGUAGE);
		Locale appLoc = null;
		
		if(languagePosition == -1 && (Arrays.asList(YodoGlobals.lang_code).contains(code))) {
			appLoc = context.getResources().getConfiguration().locale;		
		}
		else if(YodoGlobals.languages[languagePosition].equals("Spanish")) {
			appLoc = new Locale("es");
	    } 
		else if(YodoGlobals.languages[languagePosition].equals("Chinese")) {
	    	appLoc = new Locale("zh");
	    } 
		else if(YodoGlobals.languages[languagePosition].equals("Japanese")) {
	    	appLoc = new Locale("ja");
	    }
		else if(YodoGlobals.languages[languagePosition].equals("French")) {
	    	appLoc = new Locale("fr");
	    }
	    else {
	    	appLoc = new Locale("en");
	    }
		
		Resources standardResources = context.getResources();
		Locale.setDefault(appLoc);
		Configuration appConfig = new Configuration(standardResources.getConfiguration());
		appConfig.locale = appLoc;
		standardResources.updateConfiguration(appConfig, standardResources.getDisplayMetrics());
	}
}
