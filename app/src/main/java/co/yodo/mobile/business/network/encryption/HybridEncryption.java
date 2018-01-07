package co.yodo.mobile.business.network.encryption;

import javax.crypto.spec.SecretKeySpec;
import javax.inject.Inject;

import co.yodo.mobile.YodoApplication;
import co.yodo.mobile.business.component.cipher.AESCrypt;
import co.yodo.mobile.business.component.cipher.RSACrypt;

/**
 * Created by hei on 04/09/17.
 * Implements the Hybrid encryption
 */
public class HybridEncryption implements IEncryption {
    @Inject
    RSACrypt cipher;

    /** Separator */
    private static final String REQ_SEP = ",";

    public HybridEncryption() {
        YodoApplication.getComponent().inject(this);
    }

    @Override
    public String apply(String... strings) {
        // Generates an AES key
        SecretKeySpec key = AESCrypt.generateKey();
        // Encrypts the AES key with RSA
        final String encyptedKey = cipher.encrypt(AESCrypt.encodeKey(key));
        // Encrypts the data with the AES key
        StringBuilder encryptedData = new StringBuilder();
        for (String plain : strings) {
            if (encryptedData.length() > 0) {
                encryptedData.append(REQ_SEP);
            }
            encryptedData.append(AESCrypt.encrypt(plain, key));
        }

        // Returns the encrypted key and data, separated with a comma
        return encyptedKey + REQ_SEP + encryptedData.toString();
    }
}
