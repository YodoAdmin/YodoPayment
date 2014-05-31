package co.yodo.helper;

import android.text.format.Time;

/**
 * Created by luis on 26/07/13.
 */
public class FormatHelper {
    private static final String	PCLIENT_SEP = "/";

    /**
     * Formats user's data into a well formed string in order to encrypt it
     * @param pUsrAccount IMEI number
     * @param pUsrPip User's pip
     * @return String formated Data
     */
    public static String formatUsrData(String pUsrAccount, String pUsrPip){
        Time now = new Time();
        now.setToNow();

        return pUsrAccount + PCLIENT_SEP + pUsrPip + PCLIENT_SEP + now.toMillis(true) / 1000L;
    }
}
