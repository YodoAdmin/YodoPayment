package co.yodo.mobile.utils;

import co.yodo.mobile.YodoApplication;

/**
 * Created by hei on 10/06/16.
 * Common utils for the encryption
 */
public class CryptUtils {
     /**
     * Gets the public key for the RSA encryption
     * @return The String path of the public key
     */
    public static String getPublicKey() {
        String PUBLIC_KEY;

        // Production
        if( YodoApplication.getSwitch().equals( "P" ) ) {
            PUBLIC_KEY = "YodoKey/Prod/2048.public.der";
        }
        // Demo
        else if( YodoApplication.getSwitch().equals( "E" ) ) {
            PUBLIC_KEY = "YodoKey/Dev/2048.public.der";
        }
        // Development
        else if( YodoApplication.getSwitch().equals( "D" ) ) {
            PUBLIC_KEY = "YodoKey/Dev/2048.public.der";
        }
        // Local
        else {
            PUBLIC_KEY = "YodoKey/Local/2048.public.der";
        }

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
