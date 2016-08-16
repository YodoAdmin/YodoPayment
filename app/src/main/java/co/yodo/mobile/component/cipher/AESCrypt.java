package co.yodo.mobile.component.cipher;

import android.util.Base64;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by hei on 15/07/16.
 * AES implementation
 */
public class AESCrypt {
    /** DEBUG */
    @SuppressWarnings( "unused" )
    private final static String TAG = AESCrypt.class.getSimpleName();

    /** Key instance */
    private static final String KEY_INSTANCE = "AES";
    private static final int KEY_SIZE = 128;

    /** Cipher instance used for encryption */
    private static final String CIPHER_INSTANCE = "AES/CBC/PKCS5Padding";

    /** BASE_64_FLAGS  */
    private static final int BASE64_FLAGS = Base64.NO_WRAP;

    /**
     * Function that creates a key and returns the java object that contains it
     * @return PublicKey The public key specified in PUBLIC_KEY
     */
    public static SecretKeySpec generateKey() {
        SecretKeySpec key = null;

        try {
            final KeyGenerator keyGen = KeyGenerator.getInstance( KEY_INSTANCE );
            keyGen.init( KEY_SIZE, new SecureRandom() );
            final SecretKey secretKey = keyGen.generateKey();
            key = new SecretKeySpec( secretKey.getEncoded(), KEY_INSTANCE );
        } catch( NoSuchAlgorithmException e ) {
            e.printStackTrace();
        }

        return key;
    }

    /**
     * Encodes the key to String (Base64)
     * @param key     SecretKeySpec AES key
     * @return String Text version of the key
     */
    public static String encodeKey( SecretKeySpec key ) {
        return Base64.encodeToString( key.getEncoded(), BASE64_FLAGS );
    }

    /**
     * Decodes the key from a String (Base64)
     * @param key            Text version of the key
     * @return SecretKeySpec The AES Key
     */
    public static SecretKeySpec decodeKey( String key ) {
        final byte[] decodedKey = Base64.decode( key, BASE64_FLAGS );
        return new SecretKeySpec( decodedKey, 0, decodedKey.length, KEY_INSTANCE );
    }

    /**
     * Generates the IV (salt) from the key
     * @param key              The key used to encrypt
     * @return IvParameterSpec The IV (salt)
     */
    private static IvParameterSpec generateIV( SecretKeySpec key ) {
        return new IvParameterSpec( key.getEncoded() );
    }

    /**
     * Encrypts a string and transforms the byte array containing
     * the encrypted string to Base64 format
     * @param plainText The unencrypted string
     * @param key       The key used to encrypt the data
     * @return String   The encrypted string in Base64
     */
    public static String encrypt( String plainText, SecretKeySpec key ) {
        String encryptedData = null;

        try {
            Cipher cipher = Cipher.getInstance( CIPHER_INSTANCE );
            cipher.init( Cipher.ENCRYPT_MODE, key, generateIV( key ) );
            final byte[] cipherData = cipher.doFinal( plainText.getBytes() );
            encryptedData = Base64.encodeToString( cipherData, BASE64_FLAGS );
        } catch( Exception e ) {
            e.printStackTrace();
        }
        return encryptedData;
    }

    /**
     * Decrypts a string (Base64 format) using AES
     * @param encryptedText The encrypted text
     * @param key           The key used to encrypt the data
     * @return String       The unencrypted data
     */
    public static String decrypt( String encryptedText, SecretKeySpec key ) {
        final byte[] cipherData = Base64.decode( encryptedText, BASE64_FLAGS );
        String unencryptedData = null;

        try {
            final Cipher cipher = Cipher.getInstance( CIPHER_INSTANCE );
            cipher.init( Cipher.DECRYPT_MODE, key, generateIV( key ) );
            unencryptedData = new String( cipher.doFinal( cipherData ) );
        } catch( Exception e ) {
            e.printStackTrace();
        }

        return unencryptedData;
    }
}
