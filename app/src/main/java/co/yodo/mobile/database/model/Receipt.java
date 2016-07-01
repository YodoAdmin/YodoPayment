package co.yodo.mobile.database.model;

/**
 * Created by luis on 31/01/15.
 * POJO for the receipt
 */
public class Receipt {
    /** Main Attributes */
    private Long id;
    private String authnumber;
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
    private boolean opened;

    private Receipt( Builder builder ) {
        this.id = builder.id;
        this.authnumber = builder.authnumber;
        this.description = builder.description;
        this.tCurrency = builder.tCurrency;
        this.exchRate = builder.exchRate;
        this.dCurrency = builder.dCurrency;
        this.totalAmount = builder.totalAmount;
        this.tenderAmount = builder.tenderAmount;
        this.cashbackAmount = builder.cashbackAmount;
        this.balanceAmount = builder.balanceAmount;
        this.currency = builder.currency;
        this.donorAccount = builder.donorAccount;
        this.recipientAccount = builder.recipientAccount;
        this.created = builder.created;
        this.opened = builder.opened;
    }

    /** Handles the animation in the ListView */
    public boolean isChecked;

    public Long getId() {
        return id;
    }

    public String getAuthnumber() {
        return authnumber;
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

    public String getCashbackAmount() {
        return cashbackAmount.replaceAll( ",", "." );
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
    public void setId( Long id ) {
        this.id = id;
    }

    public void setOpened( boolean opened ) {
        this.opened = opened;
    }

    public void setChecked( boolean checked ) {
        this.isChecked = checked;
    }

    @Override
    public String toString() {
        return "\nID: "         + id             + "\n" + "AuthNumber: "    + authnumber   + "\n" +
               "Balance: "      + balanceAmount  + "\n" + "Currency: "      + currency     + "\n" +
               "Total Amount: " + totalAmount    + "\n" + "Tender Amount: " + tenderAmount + "\n" +
               "CashBack: "     + cashbackAmount + "\n" + "Created: "       + created      + "\n" +
               "Opened: "       + opened         + "\n" + "Checked: "       + isChecked;
    }

    /**
     * Receipt Builder
     */
    public static class Builder {
        /** Main Attributes */
        private Long id;
        private String authnumber;
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

        public Builder id( Long id ) {
            this.id = id;
            return this;
        }

        public Builder authnumber( String authnumber ) {
            this.authnumber = authnumber;
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
