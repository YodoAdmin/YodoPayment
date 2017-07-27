package co.yodo.mobile.model.dtos;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by yodop on 2017-07-24.
 * POJO for transfered data
 */
public class Transfer {
    /** Main attributes */
    private final String from;
    private final String amount;
    private final String currency;
    private final String accountBalance;
    private final String accountCurrency;

    /** JSON keys */
    private static final String JK_FROM = "from";
    private static final String JK_AMOUNT = "amount";
    private static final String JK_CURRENCY = "currency";
    private static final String JK_ACCOUNT = "account";
    private static final String JK_BALANCE = "balance";

    private Transfer( Builder builder ) {
        this.from = builder.from;
        this.amount = builder.amount;
        this.currency = builder.currency;
        this.accountBalance = builder.accountBalance;
        this.accountCurrency = builder.accountCurrency;
    }

    /**
     * Generates a Transfer from a JSON text
     * @param message The json message
     * @return The parsed transfer
     */
    public static Transfer fromJSON(String message) throws JSONException {
        // Get parent tag
        JSONObject root = new JSONObject(message);
        JSONObject account = root.getJSONObject(JK_ACCOUNT);

        return new Builder()
                .from( root.getString(JK_FROM) )
                .amount( root.getString(JK_AMOUNT) )
                .currency( root.getString(JK_CURRENCY) )
                .accountBalance( account.getString(JK_BALANCE), account.getString(JK_CURRENCY))
                .build();
    }

    public String getFrom() {
        return from;
    }

    public String getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getAccountBalance() {
        return accountBalance;
    }

    public String getAccountCurrency() {
        return accountCurrency;
    }

    /**
     * Transfer Builder
     */
    public static class Builder {
        /** Main Attributes */
        private String from;
        private String amount;
        private String currency;
        private String accountBalance;
        private String accountCurrency;

        Builder from( String from ) {
            this.from = from;
            return this;
        }

        Builder amount( String amount ) {
            this.amount = amount;
            return this;
        }

        Builder currency( String currency) {
            this.currency = currency;
            return this;
        }

        Builder accountBalance( String balance, String currency ) {
            this.accountBalance = balance;
            this.accountCurrency = currency;
            return this;
        }

        public Transfer build() {
            return new Transfer( this );
        }
    }
}
