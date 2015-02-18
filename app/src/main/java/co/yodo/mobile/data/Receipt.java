package co.yodo.mobile.data;

/**
 * Created by luis on 31/01/15.
 * POJO for the receipt
 */
public class Receipt {
    private long id;
    private String description;
    private String authNumber;
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

    public String getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(String totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getTenderAmount() {
        return tenderAmount;
    }

    public void setTenderAmount(String tenderAmount) {
        this.tenderAmount = tenderAmount;
    }

    public String getCashBackAmount() {
        return cashBackAmount;
    }

    public void setCashBackAmount(String cashBackAmount) {
        this.cashBackAmount = cashBackAmount;
    }

    public String getBalanceAmount() {
        return balanceAmount;
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
}
