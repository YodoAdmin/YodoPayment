package co.yodo.mobile.helper;

/**
 * Created by hei on 10/06/16.
 * Common utils for the encryption
 */
public class CryptUtils {
    /** DEBUG */
    @SuppressWarnings( "unused" )
    private static final String TAG = CryptUtils.class.getSimpleName();

    /**
     * Receives an encrypted byte array and returns a string of
     * hexadecimal numbers that represents it
     * @param data Encrypted byte array
     * @return           String of hexadecimal number
     */
    public static String bytesToHex( byte[] data ) {
        if( data == null )
            return null;

        String str = "";
        for( byte aData : data ) {
            if( ( aData & 0xFF ) < 16 )
                str = str + "0" + Integer.toHexString( aData & 0xFF );
            else
                str = str + Integer.toHexString( aData & 0xFF );
        }
        return str;
    }

    /**
     * Transforms a String to bytes
     * @param str The String
     * @return bytes
     */
    public static byte[] hexToBytes( String str ) {
        if( str == null ) {
            return null;
        } else if( str.length() < 2 ) {
            return null;
        } else {
            int len = str.length() / 2;
            byte[] buffer = new byte[ len ];
            for( int i = 0; i < len; i++ ) {
                buffer[i] = (byte) Integer.parseInt( str.substring( i * 2, i * 2 + 2 ), 16 );
            }
            return buffer;
        }
    }
}
