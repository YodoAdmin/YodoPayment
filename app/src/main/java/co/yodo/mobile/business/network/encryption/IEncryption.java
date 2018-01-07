package co.yodo.mobile.business.network.encryption;

/**
 * Created by hei on 04/09/17.
 * Interface for the different encryption algorithms
 */
public interface IEncryption {
    /**
     * Applies the algorithm to a plain text String
     * @param strings The input String
     * @return The output after the process
     */
    String apply(String... strings);
}
