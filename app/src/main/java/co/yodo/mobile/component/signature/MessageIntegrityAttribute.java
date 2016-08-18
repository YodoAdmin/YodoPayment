package co.yodo.mobile.component.signature;

import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.spec.SecretKeySpec;

import co.yodo.mobile.component.cipher.AESCrypt;
import co.yodo.mobile.helper.CryptUtils;

/**
 * Created by hei on 16/08/16.
 * Includes the integrity attribute to the message
 */
public class MessageIntegrityAttribute {
    /**
     * Adds the digital signature to the message
     * @param message The formatted message
     * @param key They key to encrypt the signature
     * @return The encoded signature
     */
    public static String encode( String message, SecretKeySpec key ) {
        final String digest = sha256( message );
        return AESCrypt.encrypt( digest, key );
    }

    /**
     * Apply the sha256 hash algorithm to a string
     * @param message The text - usually the entire request
     * @return The hash key for the text
     */
    public static String sha256( String message ) {
        String encodedHash = null;

        try {
            MessageDigest crypt = MessageDigest.getInstance( "SHA-256" );
            crypt.reset();
            crypt.update( message.getBytes( "UTF-8" ) );
            encodedHash = CryptUtils.bytesToHex( crypt.digest() );
        } catch( NoSuchAlgorithmException | UnsupportedEncodingException e ) {
            e.printStackTrace();
        }
        return encodedHash;
    }
}
