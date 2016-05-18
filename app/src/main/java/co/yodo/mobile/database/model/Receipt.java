package co.yodo.mobile.database.model;

/**
 * Created by luis on 31/01/15.
 * POJO for the receipt
 */
public class Receipt {
    /** Main Attributes */
    private long id;
    private String authNumber;
    private String description;
    private String tCurrency;
    private String exchRate; // Exchange rate
    private String dCurrency;
    private String totalAmount;
    private String tenderAmount;
    private String cashBackAmount;
    private String balanceAmount;
    private String currency;
    private String donorAccount;
    private String receiverAccount;
    private String created;
    private boolean opened;

    /** Handles the animation in the ListView */
    public boolean isChecked;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getAuthNumber() {
        return authNumber;
    }

    public void setAuthNumber(String authNumber) {
        this.authNumber = authNumber;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTCurrency() {
        return tCurrency;
    }

    public void setTCurrency( String tCurrency ) {
        this.tCurrency = tCurrency;
    }

    public String getExchRate() {
        return exchRate;
    }

    public void setExchRate( String exchRate ) {
        this.exchRate = exchRate;
    }

    public String getDCurrency() {
        return dCurrency;
    }

    public void setDCurrency( String dCurrency ) {
        this.dCurrency = dCurrency;
    }

    public String getTotalAmount() {
        return totalAmount.replaceAll( ",", "." );
    }

    public void setTotalAmount(String totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getTenderAmount() {
        return tenderAmount.replaceAll( ",", "." );
    }

    public void setTenderAmount( String tenderAmount ) {
        this.tenderAmount = tenderAmount;
    }

    public String getCashBackAmount() {
        return cashBackAmount.replaceAll( ",", "." );
    }

    public void setCashBackAmount( String cashBackAmount ) {
        this.cashBackAmount = cashBackAmount;
    }

    public String getBalanceAmount() {
        return balanceAmount.replaceAll( ",", "." );
    }

    public void setBalanceAmount( String balanceAmount ) {
        this.balanceAmount = balanceAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getDonorAccount() {
        return donorAccount;
    }

    public void setDonorAccount( String donorAccount ) {
        this.donorAccount = donorAccount;
    }

    public String getReceiverAccount() {
        return receiverAccount;
    }

    public void setReceiverAccount( String receiverAccount ) {
        this.receiverAccount = receiverAccount;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated( String created ) {
        this.created = created;
    }

    public boolean isOpened() {
        return opened;
    }

    public void setOpened( boolean opened ) {
        this.opened = opened;
    }

    public void setChecked( boolean checked ) {
        this.isChecked = checked;
    }

    @Override
    public String toString() {
        return "\nID: "         + id             + "\n" + "AuthNumber: "    + authNumber   + "\n" +
               "Balance: "      + balanceAmount  + "\n" + "Currency: "      + currency     + "\n" +
               "Total Amount: " + totalAmount    + "\n" + "Tender Amount: " + tenderAmount + "\n" +
               "CashBack: "     + cashBackAmount + "\n" + "Created: "       + created      + "\n" +
               "Opened: "       + opened         + "\n" + "Checked: "       + isChecked;
    }
}
