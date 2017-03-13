package co.yodo.mobile.business.component.totp;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by hei on 27/07/16.
 * Utils for the TOTP
 */
public class TOTPUtils {
    /** DEBUG */
    @SuppressWarnings( "unused" )
    private static final String TAG = TOTPUtils.class.getSimpleName();

    /** Window is used to check codes generated in the near past */
    private static final int WINDOW = 1;

    /** Interval of time for the OTP */
    private static final int INTERVAL = 1000 * 30; // 30 seconds

    /**
     * Verify if a code (OTP) is still valid
     * @param secret The secret between client - server
     * @param code   The code to verify
     * @param length The length of the code
     * @param crypto The algorithm used to generate the OTP
     * @return True if the code is still valid
     */
    public static boolean checkCode( String secret, long code, int length, String crypto ) throws NoSuchAlgorithmException, InvalidKeyException {
        final byte[] decodedKey = secret.getBytes();
        final long currentInterval = getTimeIndex();

        // Tolerance for network delay and others
        for( int i = -WINDOW; i <= WINDOW; ++i ) {
            long hash = TOTP.generateTOTP( decodedKey, currentInterval + i, length, crypto );
            if( hash == code )
                return true;
        }

        // The validation code is invalid.
        return false;
    }

    /**
     * Gets the time interval (life-time of the OTP)
     * @return The time interval
     */
    private static long getTimeIndex() {
        return System.currentTimeMillis() / INTERVAL;
    }

    /**
     * Apply the sha1 hash algorithm to a string
     * @param password The text - usually a password
     * @return The hash key for the text
     */
    public static String sha1( String password ) {
        try {
            MessageDigest crypt = MessageDigest.getInstance( "SHA-1" );
            crypt.reset();
            crypt.update( password.getBytes( "UTF-8" ) );
            return new BigInteger( 1, crypt.digest() ).toString( 16 );
        } catch( NoSuchAlgorithmException | UnsupportedEncodingException e ) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Generates an OTP with default parameters
     * @param pip The user's password
     * @return The one time password
     */
    public static String defaultOTP( String pip ) {
        final String hashPip = TOTPUtils.sha1( pip );
        if( hashPip == null )
            throw new NullPointerException( "Null user pip" );

        final int otp = TOTP.generateTOTP(
                hashPip.getBytes(),
                TOTPUtils.getTimeIndex(),
                TOTP.LENGTH,
                TOTP.HmacSHA1
        );

        return String.valueOf( otp );
    }
}
