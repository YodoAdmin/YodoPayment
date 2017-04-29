package co.yodo.mobile.model.db;

import com.orm.SugarRecord;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import co.yodo.mobile.helper.FormatUtils;

/**
 * Created by luis on 31/01/15.
 * POJO for the receipt
 */
public class Receipt extends SugarRecord {
    /** Main attributes */
    private String authNumber;
    private String description;
    private String tCurrency;
    private String exchRate;
    private String dCurrency;
    private String totalAmount;
    private String tenderAmount;
    private String cashBackAmount;
    private String balanceAmount;
    private String currency;
    private String donorAccount;
    private String recipientAccount;
    private String created;
    private boolean opened;

    /** JSON keys */
    private static final String YI = "yi";
    private static final String YT = "yt";

    /** Merchant elements */
    private static final String DESCRIPTION = "description";
    private static final String TCURRENCY   = "dcurrency";

    /** Transaction elements */
    private static final String CREATED      = "created";
    private static final String AMOUNT       = "amount";
    private static final String TAMOUNT      = "tamount";
    private static final String CASHBACK     = "cashback";
    private static final String AUTHNUM      = "transauthnumber";
    private static final String CURRENCY     = "currency";
    private static final String EXCH_RATE    = "xch_rate";
    private static final String ACC          = "account";     // donor
    private static final String ACC_RECEIVER = "trx_account"; // receiver
    private static final String ACC_BALANCE  = "account_bal";
    private static final String ACC_CURRENCY = "account_cur";

    @SuppressWarnings("unused")
    public Receipt() {}

    private Receipt( Builder builder ) {
        this.authNumber = builder.authNumber;
        this.description = builder.description;
        this.tCurrency = builder.tCurrency;
        this.exchRate = builder.exchRate;
        this.dCurrency = builder.dCurrency;
        this.totalAmount = builder.totalAmount;
        this.tenderAmount = builder.tenderAmount;
        this.cashBackAmount = builder.cashbackAmount;
        this.balanceAmount = builder.balanceAmount;
        this.currency = builder.currency;
        this.donorAccount = builder.donorAccount;
        this.recipientAccount = builder.recipientAccount;
        this.created = builder.created;
        this.opened = builder.opened;
    }

    /**
     * Generates a Receipt from a JSON text
     * @param message The json message
     * @return The parsed receipt
     */
    public static Receipt fromJSON( String message ) throws JSONException {
        String donor = null, receiver = null;

        // Get info
        JSONArray temp = new JSONArray( message );
        JSONObject info = temp.getJSONObject( 0 );

        // Get parent tags
        JSONObject merchant = (JSONObject) info.get( YI );
        JSONObject transaction = (JSONObject) info.get( YT );

        // Only for heart transactions
        if( transaction.has( ACC ) )
            donor = String.valueOf( transaction.get( ACC ) );

        if( transaction.has( ACC_RECEIVER ) )
            receiver = String.valueOf( transaction.get( ACC_RECEIVER ) );

        return new Receipt.Builder()
                .description( String.valueOf( merchant.get( DESCRIPTION ) ) )
                .authnumber( String.valueOf( transaction.get( AUTHNUM ) ) )
                .created( String.valueOf( transaction.get( CREATED ) ) )
                .total( String.valueOf( transaction.get( AMOUNT ) ),
                        String.valueOf( merchant.get( TCURRENCY ) ) )
                .tender( String.valueOf( transaction.get( TAMOUNT ) ),
                        String.valueOf( transaction.get( CURRENCY ) ) )
                .cashback( String.valueOf( transaction.get( CASHBACK ) ) )
                .exchRate( String.valueOf( transaction.get( EXCH_RATE ) ) )
                .donor( donor )
                .recipient( receiver )
                .balance( String.valueOf( transaction.get( ACC_BALANCE ) ),
                        String.valueOf( transaction.get( ACC_CURRENCY ) ) )
                .build();
    }

    /** Handles the animation in the ListView */
    public boolean isChecked;

    public String getAuthNumber() {
        return authNumber;
    }

    public String getDescription() {
        return description;
    }

    public String getTCurrency() {
        return tCurrency;
    }

    public String getExchRate() {
        return exchRate;
    }

    public String getDCurrency() {
        return dCurrency;
    }

    public String getTotalAmount() {
        return totalAmount.replaceAll( ",", "." );
    }

    public String getTenderAmount() {
        return tenderAmount.replaceAll( ",", "." );
    }

    public String getCashBackAmount() {
        return cashBackAmount.replaceAll( ",", "." );
    }

    public String getBalanceAmount() {
        return balanceAmount.replaceAll( ",", "." );
    }

    public String getCurrency() {
        return currency;
    }

    public String getDonorAccount() {
        return donorAccount;
    }

    public String getRecipientAccount() {
        return recipientAccount;
    }

    public String getCreated() {
        return created;
    }

    public boolean isOpened() {
        return opened;
    }

    /**
     * Setters
     * {{ ==============================================
     */
    public void setOpened( boolean opened ) {
        this.opened = opened;
    }

    public void setChecked( boolean checked ) {
        this.isChecked = checked;
    }

    @Override
    public String toString() {
        return String.format( "%s\nAU# %s\t\t%s\nTotal:\t\t%s %s\nCashTender:\t%s %s\nCashBack:\t%s %s\n\nDonor: %s\nRecipient: %s",
                getDescription(),
                getAuthNumber() ,
                getCreated(),
                FormatUtils.truncateDecimal( getTotalAmount() ), getTCurrency(),
                FormatUtils.truncateDecimal( getTenderAmount() ), getDCurrency(),
                FormatUtils.truncateDecimal( getCashBackAmount() ), getTCurrency(),
                FormatUtils.replaceNull( getDonorAccount() ),
                getRecipientAccount()
        );
    }

    /**
     * Receipt Builder
     */
    public static class Builder {
        /** Main Attributes */
        private String authNumber;
        private String description;
        private String tCurrency;
        private String exchRate; // Exchange rate
        private String dCurrency;
        private String totalAmount;
        private String tenderAmount;
        private String cashbackAmount;
        private String balanceAmount;
        private String currency;
        private String donorAccount;
        private String recipientAccount;
        private String created;
        private boolean opened = false;

        public Builder authnumber( String authnumber ) {
            this.authNumber = authnumber;
            return this;
        }

        public Builder description( String description ) {
            this.description = description;
            return this;
        }

        public Builder total( String total, String currency ) {
            this.totalAmount = total;
            this.tCurrency = currency;
            return this;
        }

        public Builder tender( String tender, String currency ) {
            this.tenderAmount = tender;
            this.dCurrency = currency;
            return this;
        }

        public Builder cashback( String cashback ) {
            this.cashbackAmount = cashback;
            return this;
        }

        public Builder exchRate( String exchRate ) {
            this.exchRate = exchRate;
            return this;
        }

        public Builder donor( String donor ) {
            this.donorAccount = donor;
            return this;
        }

        public Builder recipient( String recipient ) {
            this.recipientAccount = recipient;
            return this;
        }

        public Builder created( String created ) {
            this.created = created;
            return this;
        }

        public Builder opened( boolean opened ) {
            this.opened = opened;
            return this;
        }

        public Builder balance( String balance, String currency ) {
            this.balanceAmount = balance;
            this.currency = currency;
            return this;
        }

        public Receipt build() {
            return new Receipt( this );
        }
    }
}
