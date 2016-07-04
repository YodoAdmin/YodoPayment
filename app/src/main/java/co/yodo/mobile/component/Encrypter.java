package co.yodo.mobile.component;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import co.yodo.mobile.helper.CryptUtils;
import co.yodo.mobile.network.YodoRequest;

/**
 * @author renatomarroquin
 * Encyrpter class for the requests
 */
public class Encrypter {
	/** DEBUG */
	@SuppressWarnings( "unused" )
	private final static String TAG = Encrypter.class.getSimpleName();

	/**
	 * Public key generated with: openssl rsa -in 11.private.pem -pubout -outform DER -out 11.public.der
	 * This key is created using the private key generated using openssl in unix environments
	*/
    private static String PUBLIC_KEY;

	/**
	 * If you change this section, also update the
	 * Encrypter.java
	 */
    static {
        if( YodoRequest.getSwitch().equals( "P" ) )
            PUBLIC_KEY = "YodoKey/Prod/12.public.der";
        else
            PUBLIC_KEY = "YodoKey/Dev/12.public.der";
    }

	/** Cipher instance used for encryption */
	private static final String CIPHER_INSTANCE = "RSA/ECB/PKCS1Padding";
	
	/** Public key instance */
	private static final String KEY_INSTANCE = "RSA";
	
	/** Contains string to be encrypted */
	private String sUnEncryptedString;
	
	/** Contains encrypted data */
	private byte cipherData[];

	/** Context of the application */
	private static Context mCtx;

	/** Singleton instance */
	private static Encrypter instance = null;

	/**
	 * Private constructor for the singleton
	 * @param context The Android context for the application
     */
	private Encrypter( Context context ) {
		mCtx = context.getApplicationContext();
	}

	/**
	 * Gets the instance of the service
	 * @return instance
	 */
	public static synchronized Encrypter getInstance( Context context ) {
		if( instance == null )
			instance = new Encrypter( context );
		return instance;
	}

	/**
	 * Function that opens the public key and returns the java object that contains it
	 * @param parent	Parent activity of SKSCreater
	 * @return			The public key specified in $keyFileName
	 */
	@SuppressWarnings( "ResultOfMethodCallIgnored" )
	private static PublicKey readKeyFromFile( Context parent ) {
		AssetManager as;
		InputStream inFile;
		byte[] encodedKey;
		PublicKey pkPublic = null;
		
		try {
			as = parent.getResources().getAssets();   
			inFile = as.open( PUBLIC_KEY );
			encodedKey = new byte[inFile.available()];
			inFile.read( encodedKey );
			inFile.close();
			
			X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec( encodedKey );
			KeyFactory kf = KeyFactory.getInstance( KEY_INSTANCE );
			pkPublic = kf.generatePublic( publicKeySpec );
		} catch( Exception e ) {
			e.printStackTrace();
		}
		
		return pkPublic;
	}

	/**
	 * Encrypts a string and returns a byte array containing the encrypted string
	 */
	public void rsaEncrypt() {
		PublicKey pubKey = readKeyFromFile( mCtx );
		Cipher cipher;

		try {
			cipher = Cipher.getInstance( CIPHER_INSTANCE );
			cipher.init( Cipher.ENCRYPT_MODE, pubKey );
			this.cipherData = cipher.doFinal( this.sUnEncryptedString.getBytes() );
			  
		} catch( NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException | InvalidKeyException e ) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Receives an encrypted byte array and returns a string of
	 * hexadecimal numbers that represents it
	 * @return	String of hexadecimal number
	 */
	public String bytesToHex() {
		return CryptUtils.bytesToHex( this.cipherData );
	}

	/**
	 * @param sUnEncryptedString the sUnEncryptedString to set
	 */
	public void setsUnEncryptedString( String sUnEncryptedString ) {
		this.sUnEncryptedString = sUnEncryptedString;
	}
}
