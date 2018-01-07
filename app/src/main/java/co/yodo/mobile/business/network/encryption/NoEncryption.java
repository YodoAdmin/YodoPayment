package co.yodo.mobile.business.network.encryption;

import java.util.Arrays;

/**
 * Created by hei on 04/09/17.
 * Implements a no encryption based algorithm
 */
public class NoEncryption implements IEncryption {
    @Override
    public String apply(String... strings) {
        return Arrays.toString(strings);
    }
}
