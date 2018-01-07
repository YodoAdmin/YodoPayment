package co.yodo.mobile.business.network.model;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root( strict = false )
public class Params {
    @Element(name = "uuid", required = false)
    private String uuid;

    @Element(name = "balance", required = false)
    private String balance;

    @Element(name = "currency", required = false)
    private String currency;

    @Element(name = "linking_code", required = false)
    private String linking_code;

    @Element(name = "linked_accounts", required = false)
    private String linked_accounts;

    @Element(name = "to", required = false)
    private String linked_to;

    @Element(name = "from", required = false)
    private String linked_from;

    @Element(name = "url", required = false)
    private String url;

    @Element(name = "BiometricToken", required = false)
    private String biometric;

    /** Getters */
    public String getUuid() {
        return uuid;
    }

    public String getBalance() {
        return balance;
    }

    public String getCurrency() {
        return currency;
    }

    public String getLinkingCode() {
        return linking_code;
    }

    public String getLinkedTo() {
        return linked_to;
    }

    public String getLinkedFrom() {
        return linked_from;
    }

    public String getAdvertisingImage() {
        return url;
    }

    public String getBiometricToken() {
        return biometric;
    }
    /************/

    @Override
    public String toString() {
        return "[" +
                "Balance = "      + balance      + ", " +
                "Currency = "     + currency     + ", " +
                "Linking Code = " + linking_code + ", " +
                "Linked To = "    + linked_to    + ", " +
                "Linked From = "  + linked_from  + ", " +
                "Advertising = "  + url          + ", " +
                "Biometric = "    + biometric    + ", " +
                "]";
    }
}