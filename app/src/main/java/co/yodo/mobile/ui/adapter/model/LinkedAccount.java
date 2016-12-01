package co.yodo.mobile.ui.adapter.model;

import co.yodo.mobile.network.request.DeLinkRequest;

/**
 * Created by hei on 11/11/16.
 * POJO for the linked accounts
 */
public class LinkedAccount {
    /** Data of the linked account */
    private String mHardwareToken;
    private String mNickname;
    private DeLinkRequest.DeLinkST mRequestST;

    public LinkedAccount( String hardwareToken, DeLinkRequest.DeLinkST requestST ) {
        this.mHardwareToken = hardwareToken;
        this.mRequestST = requestST;
    }

    public String getHardwareToken() {
        return mHardwareToken;
    }

    public DeLinkRequest.DeLinkST getRequestST() {
        return mRequestST;
    }

    public String getNickname() {
        return mNickname;
    }

    public void setNickname( String nickname ) {
        this.mNickname = nickname;
    }
}
