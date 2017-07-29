package co.yodo.mobile.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * Created by yodop on 2017-07-28.
 * Utils for json
 */
public final class JsonUtils {
    /**
     * Avoid instances of the class
     */
    private JsonUtils() {}

    /**
     * Verifies if a text is a json array or object
     * @param text The plain text
     * @return boolean
     */
    public static boolean isValidJson(String text) {
        try {
            Object json = new JSONTokener(text).nextValue();
            if (json instanceof JSONObject || json instanceof JSONArray) {
                return true;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }
}
