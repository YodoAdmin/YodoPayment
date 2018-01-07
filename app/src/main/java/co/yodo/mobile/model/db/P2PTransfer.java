package co.yodo.mobile.model.db;

import com.orm.SugarRecord;

/**
 * Created by hei on 19/11/17.
 * Model object to store P2P transfers
 */
public class P2PTransfer extends SugarRecord {
    /** Attributes */
    private String phoneNumber;

    @SuppressWarnings("unused")
    public P2PTransfer() {}

    public P2PTransfer(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    @Override
    public String toString() {
        return phoneNumber;
    }
}
