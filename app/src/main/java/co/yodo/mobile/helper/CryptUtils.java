package co.yodo.mobile.helper;

import co.yodo.mobile.network.YodoRequest;

/**
 * Created by hei on 10/06/16.
 * Common utils for the encryption
 */
public class CryptUtils {
    /** DEBUG */
    @SuppressWarnings( "unused" )
    private static final String TAG = CryptUtils.class.getSimpleName();

    /**
     * Gets the public key for the RSA encryption
     * @return The String path of the public key
     */
    public static String getPublicKey() {
        String PUBLIC_KEY;

        if( YodoRequest.getSwitch().equals( "P" ) )
            PUBLIC_KEY = "YodoKey/Prod/12.public.der";
        else
            PUBLIC_KEY = "YodoKey/Local/12.public.der";
        /*else if( YodoRequest.getSwitch().equals( "L" ) )
            PUBLIC_KEY = "YodoKey/Local/12.public.der";
        else
            PUBLIC_KEY = "YodoKey/Dev/12.public.der";*/

        return PUBLIC_KEY;
    }

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
