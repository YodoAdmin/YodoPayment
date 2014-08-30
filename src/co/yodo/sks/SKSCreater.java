package co.yodo.sks;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Hashtable;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.util.Log;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import co.yodo.R;

/**
 * This class is used for create Yodo SKS by using 
 * ZXing's qr library. 
 * @author Sirinut Thangthumachit (Zui), zui@yodo.mobi
 */
public class SKSCreater {
	/*!< DEBUG */
	private final static boolean DEBUG = true;
	private final static String TAG = SKSCreater.class.getName();

	// size of qr code (px)
	private final static int QR_WIDTH = 300;
	
	public static final int SKS_CODE = 1;
	public static final int QR_CODE  = 0;

	private static final int WHITE = 0xFFFFFFFF;
	private static final int BLACK = 0xFF000000;

	private static final BarcodeFormat QR = BarcodeFormat.QR_CODE;
	
	/**
	 * Public key generated with: openssl rsa -in 11.private.pem -pubout -outform DER -out 11.public.der
	 * This key is created using the private key generated using openssl in unix environments
	*/
	private static String PUBLIC_KEY = "YodoKey/12.public.der";
	//"YodoKey/public.yodokey"

	public static Bitmap createSKS(String original, Activity parent, int type) throws UnsupportedEncodingException{
		int width, height, pixels [];
		QRCodeWriter writer = new QRCodeWriter();
		BitMatrix qrMatrix = null;
		String encoding, response = "";
		Bitmap bitmap = null;
		//String text2Encrypt = "**renato123**1204122156";
		encoding = (String)parent.getResources().getString(R.string.SC_LETTER_TYPE);
		try {
			//byte encrypted []= rsaEncrypt(text2Encrypt.getBytes(), parent);
			byte encrypted []= rsaEncrypt(original.getBytes("UTF-8"), parent);
			response = bytesToHex(encrypted);

			if(DEBUG)
				Log.e(TAG, response + " - " + response.length());
			
			Hashtable<Object, String> hint = new Hashtable<Object, String>();
			hint.put(EncodeHintType.CHARACTER_SET, encoding);
			
			if(type == 0) { /// qr
				qrMatrix = writer.encode(original, QR, QR_WIDTH, QR_WIDTH, hint);
			} else { /// sks
				qrMatrix = writer.encode((String)parent.getResources().getString(R.string.SKS_HEADER) + response , QR, QR_WIDTH, QR_WIDTH, hint);
			}
			
			width = qrMatrix.getWidth();
			height = qrMatrix.getHeight();
			pixels = new int[width * height];
			// All are 0, or black, by default
			for (int y = 0; y < height; y++) {
				int offset = y * width;
				for (int x = 0; x < width; x++) {
					pixels[offset + x] = qrMatrix.get(x, y) ? BLACK : WHITE;
				}
			}
			
			bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
			bitmap.setPixels(pixels, 0, width, 0, 0, width, height); 
			
		} catch (WriterException e) {
			e.printStackTrace();
		} catch (Exception e){
			e.printStackTrace();
		}
		return bitmap;
	}
	
	/*public static void setWidth(int size) {
		QR_WIDTH = size;
	}*/
	
	/**
	 * Function that opens the public key and returns the java object that contains it
	 * @param parent		Parent activity of SKSCreater
	 * @return				The public key specified in $keyFileName
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 */
	static PublicKey readKeyFromFile(Activity parent) {
		AssetManager as;
		InputStream inFile;
		byte[] encodedKey;
		PublicKey pkPublic = null;
		
		try{
			as = parent.getResources().getAssets();   
			inFile = as.open(PUBLIC_KEY);
			encodedKey = new byte[inFile.available()];
			inFile.read(encodedKey);
			inFile.close();
			
			X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(encodedKey);
			KeyFactory kf = KeyFactory.getInstance("RSA");
			pkPublic = kf.generatePublic(publicKeySpec);
			    
		}
		catch(Exception e){
			if(DEBUG)
				Log.e(parent.getClass().toString() , "Error Reading Public Key - SKSCreater");
		}
		
		return pkPublic;
	}
	
	/**
	 * Encrypts a string and returns a byte array containing the encrypted string
	 * @param data		Text to encrypt
	 * @param parent	Activity that uses the SKSCreater
	 * @return			Byte array containing the encrypted string
	 */
	@SuppressLint("TrulyRandom")
	public static byte[] rsaEncrypt(byte[] data, Activity parent) {
		PublicKey pubKey = readKeyFromFile(parent);
		Cipher cipher;
		byte[] cipherData = null;
		String cipherString = "";
		try {
			cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			cipher.init(Cipher.ENCRYPT_MODE, pubKey);
			cipherData = cipher.doFinal(data);
			cipherString = new String(cipherData);
			  
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			if(DEBUG)
				Log.e(parent.getClass().toString() , "Error Encrypting string - SKSCreater");
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
			Log.e(parent.getClass().toString() , "Error Encrypting string - SKSCreater");
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
			if(DEBUG)
				Log.e(parent.getClass().toString() , "Error Encrypting string - SKSCreater");
		} catch (BadPaddingException e) {
			e.printStackTrace();
			if(DEBUG)
				Log.e(parent.getClass().toString() , "Error Encrypting string - SKSCreater");
		}
		if(DEBUG)
			Log.e(parent.getClass().toString() , cipherString);
		return cipherData;
	}
	
	/**
	 * Receives an encrypted byte array and returns a string of
	 * hexadecimal numbers that represents it
	 * @param cipherData	Encrypted byte array
	 * @return				String of hexadecimal number 
	 */
	public static String bytesToHex(byte cipherData[]) {
		StringBuffer hexCrypt = new StringBuffer();
		for(int i = 0; i < cipherData.length; i++ ) {
			int int_value = (int)cipherData[i];
			
			if(int_value < 0) {
				int_value = int_value + 256;
			}
			String hexNum = Integer.toHexString(int_value);
			
			if(hexNum.length() == 1) {
				hexCrypt.append("0"+ hexNum);
			} else {
				hexCrypt.append(hexNum);
			}
		}
		if(DEBUG)
			Log.e("User Crypt" , hexCrypt.toString());
		return hexCrypt.toString();
	}
}