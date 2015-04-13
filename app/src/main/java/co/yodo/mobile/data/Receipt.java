package co.yodo.mobile.data;

/**
 * Created by luis on 31/01/15.
 * POJO for the receipt
 */
public class Receipt {
    private long id;
    private String description;
    private String authNumber;
    private String currency;
    private String totalAmount;
    private String tenderAmount;
    private String cashBackAmount;
    private String balanceAmount;
    private String created;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAuthNumber() {
        return authNumber;
    }

    public void setAuthNumber(String authNumber) {
        this.authNumber = authNumber;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
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

    public void setTenderAmount(String tenderAmount) {
        this.tenderAmount = tenderAmount;
    }

    public String getCashBackAmount() {
        return cashBackAmount.replaceAll( ",", "." );
    }

    public void setCashBackAmount(String cashBackAmount) {
        this.cashBackAmount = cashBackAmount;
    }

    public String getBalanceAmount() {
        return balanceAmount.replaceAll( ",", "." );
    }

    public void setBalanceAmount(String balanceAmount) {
        this.balanceAmount = balanceAmount;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    @Override
    public String toString() {
        return "ID: "           + id             + "\n" + "AuthNumber: "    + authNumber   + "\n" +
               "Total Amount: " + totalAmount    + "\n" + "Tender Amount: " + tenderAmount + "\n" +
               "CashBack: "     + cashBackAmount + "\n" + "Created: "       + created;
    }
}
