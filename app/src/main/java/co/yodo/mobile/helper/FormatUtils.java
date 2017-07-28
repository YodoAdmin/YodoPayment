package co.yodo.mobile.helper;

import android.content.Context;
import android.text.format.DateUtils;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.zip.GZIPOutputStream;

/**
 * Created by hei on 10/06/16.
 * Formats different structures (e.g. date, integer), or
 * cast values to other objects
 */
public class FormatUtils {
    /**
     * Gets the actual date
     * @return	String actual date
     */
    public static String getCurrentDate() {
        final Calendar c = Calendar.getInstance();
        int year  = c.get( Calendar.YEAR );
        int month = c.get( Calendar.MONTH ) + 1;
        int day   = c.get( Calendar.DAY_OF_MONTH );
        String sMonth = ( month < 10 ) ? "0" + month : "" + month;
        String sDay   = ( day   < 10 ) ? "0" + day   : "" + day;
        return year + "/"+ ( sMonth ) + "/" + sDay;
    }

    /**
     * Transforms a UTC date to the cellphone date
     * @param date The date in UTC
     * @return the Date in the cellphone time
     */
    public static String UTCtoCurrent( Context ac, String date ) {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd HH:mm", Locale.US );

        try {
            TimeZone z = c.getTimeZone();
            int offset = z.getRawOffset();
            if( z.inDaylightTime( new Date() ) )
                offset = offset + z.getDSTSavings();
            int offsetHrs = offset / 1000 / 60 / 60;

            c.setTime( sdf.parse( date ) );
            c.add( Calendar.HOUR_OF_DAY, offsetHrs );
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return DateUtils. getRelativeTimeSpanString( ac, c.getTimeInMillis(), true ).toString();
    }

    /**
     * Cast a Object to a List of type clazz
     * @param obj The object to be cast
     * @param clazz The class of the item's List
     * @param <T> The type
     * @return the list of the new type
     */
    public static <T> List<T> castList( Object obj, Class<T> clazz ) {
        List<T> result = new ArrayList<>();
        if( obj instanceof List<?> ) {
            for( Object o : (List<?>) obj )
                result.add( clazz.cast( o ) );
            return result;
        }
        return null;
    }

    /**
     * Truncates n number of positions (decimal) from a number
     * @param number The number to be truncated
     * @param positions the n positions
     * @return The truncated number as String
     */
    public static String truncateDecimal( String number, int positions ) {
        BigDecimal value  = new BigDecimal( number );
        BigDecimal factor = BigDecimal.TEN.pow( positions );
        value = value.multiply( factor ).setScale( positions, RoundingMode.DOWN );

        return value.divide( factor, positions, RoundingMode.DOWN ).toString();
    }

    /**
     * Truncates 2 number of positions (decimal) from a number
     * @param number The number to be truncated
     * @return The truncated number as String
     */
    public static String truncateDecimal( String number ) {
        return truncateDecimal( number, 2 );
    }

    /**
     * Compress a string before being send to the cloud
     * @param srcTxt The original String
     * @return The compressed String
     * @throws IOException error for transform
     */
    public static String compressString( String srcTxt ) throws IOException {
        ByteArrayOutputStream rstBao = new ByteArrayOutputStream();
        GZIPOutputStream zos = new GZIPOutputStream( rstBao );
        zos.write( srcTxt.getBytes() );
        zos.close();
        byte[] bytes = rstBao.toByteArray();
        rstBao.close();
        return Base64.encodeToString( bytes, Base64.NO_WRAP );
    }

    /**
     * Replace a null string with an empty one
     * @param input The original String
     * @return The string or "" if null
     */
    public static String replaceNull( String input ) {
        return input == null ? "" : input;
    }
}
