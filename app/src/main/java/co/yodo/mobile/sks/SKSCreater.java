package co.yodo.mobile.sks;

import android.app.Activity;
import android.content.res.AssetManager;
import android.graphics.Bitmap;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Hashtable;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import co.yodo.mobile.R;
import co.yodo.mobile.helper.AppUtils;
import co.yodo.mobile.service.RESTService;

/**
 * This class is used for create Yodo SKS by using 
 * ZXing's qr library. 
 * @author Sirinut Thangthumachit (Zui), zui@yodo.mobi
 */
public class SKSCreater {
	/** DEBUG */
	private final static String TAG = SKSCreater.class.getSimpleName();

    public static final int SKS_CODE = 1;
    //public static final int QR_CODE  = 0;

	private static final int WHITE = 0xFFFFFFFF;
	private static final int BLACK = 0xFF000000;

	private static final BarcodeFormat QR = BarcodeFormat.QR_CODE;
	
	/**
	 * Public key generated with: openssl rsa -in 11.private.pem -pubout -outform DER -out 11.public.der
	 * This key is created using the private key generated using openssl in unix environments
	*/
	//private static String PUBLIC_KEY = "YodoKey/12.public.der";
    private static String PUBLIC_KEY;

    static {
        if( RESTService.getSwitch().equals( "D" ) )
            PUBLIC_KEY = "YodoKey/Dev/12.public.der";
        else
            PUBLIC_KEY = "YodoKey/Prod/12.public.der";
    }

	public static Bitmap createSKS(String original, Activity parent, int type, Integer account_type) throws UnsupportedEncodingException{
		int width, height, pixels [];
		QRCodeWriter writer = new QRCodeWriter();
		BitMatrix qrMatrix;
		String encoding, response;
		Bitmap bitmap = null;

        Integer QR_SIZE = AppUtils.getSKSSize( parent );
		
		//String text2Encrypt = "**renato123**1204122156";
		encoding = parent.getResources().getString( R.string.SC_LETTER_TYPE );
		try {
			byte encrypted []= rsaEncrypt( original.getBytes( "UTF-8" ), parent );
			response = bytesToHex(encrypted);
			
			if( account_type != null )
				response += account_type;
			
			AppUtils.Logger(TAG, response + " - " + response.length());
			
			Hashtable<Object, String> hint = new Hashtable<Object, String>();
			hint.put(EncodeHintType.CHARACTER_SET, encoding);
			
			if(type == 0) { /// qr
				qrMatrix = writer.encode(original, QR, QR_SIZE, QR_SIZE, hint);
			} else { /// sks
				qrMatrix = writer.encode( parent.getResources().getString( R.string.SKS_HEADER ) + response , QR, QR_SIZE, QR_SIZE, hint);
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
			
		} catch (Exception e){
			e.printStackTrace();
		}
		return bitmap;
	}
	
	/**
	 * Function that opens the public key and returns the java object that contains it
	 * @param parent		Parent activity of SKSCreater
	 * @return				The public key specified in $keyFileName
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
			inFile.read( encodedKey );
			inFile.close();
			
			X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(encodedKey);
			KeyFactory kf = KeyFactory.getInstance("RSA");
			pkPublic = kf.generatePublic(publicKeySpec);
		}
		catch(Exception e) {
			AppUtils.Logger( TAG, "Error Reading Public Key - SKSCreater" );
		}
		return pkPublic;
	}
	
	/**
	 * Encrypts a string and returns a byte array containing the encrypted string
	 * @param data		Text to encrypt
	 * @param parent	Activity that uses the SKSCreater
	 * @return			Byte array containing the encrypted string
	 */
	public static byte[] rsaEncrypt(byte[] data, Activity parent) {
		PublicKey pubKey = readKeyFromFile(parent);
		Cipher cipher;
		byte[] cipherData = null;
		String cipherString = "";
		try {
			cipher = Cipher.getInstance( "RSA/ECB/PKCS1Padding" );
			cipher.init(Cipher.ENCRYPT_MODE, pubKey);
			cipherData = cipher.doFinal(data);
			cipherString = new String(cipherData);
			  
		} catch (NoSuchAlgorithmException | IllegalBlockSizeException | BadPaddingException | NoSuchPaddingException | InvalidKeyException e) {
			e.printStackTrace();
		}
        AppUtils.Logger(TAG, cipherString);
		return cipherData;
	}
	
	/**
	 * Receives an encrypted byte array and returns a string of
	 * hexadecimal numbers that represents it
	 * @param cipherData	Encrypted byte array
	 * @return				String of hexadecimal number 
	 */
	public static String bytesToHex(byte cipherData[]) {
		StringBuilder hexCrypt = new StringBuilder();
        for (byte aCipherData : cipherData) {
            int int_value = (int) aCipherData;

            if (int_value < 0) {
                int_value = int_value + 256;
            }
            String hexNum = Integer.toHexString(int_value);

            if (hexNum.length() == 1) {
                hexCrypt.append( "0" ).append(hexNum);
            } else {
                hexCrypt.append(hexNum);
            }
        }
		AppUtils.Logger( TAG, hexCrypt.toString() );
		return hexCrypt.toString();
	}
}