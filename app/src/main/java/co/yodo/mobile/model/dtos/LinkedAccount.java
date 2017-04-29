package co.yodo.mobile.model.dtos;

import co.yodo.mobile.business.network.request.DeLinkRequest;

/**
 * Created by hei on 11/11/16.
 * POJO for the linked accounts
 */
public class LinkedAccount {
    /** Data of the linked account */
    private String hardwareToken;
    private String nickname;
    private DeLinkRequest.DeLinkST requestST;

    public LinkedAccount( String hardwareToken, DeLinkRequest.DeLinkST requestST ) {
        this.hardwareToken = hardwareToken;
        this.requestST = requestST;
    }

    public String getHardwareToken() {
        return hardwareToken;
    }

    public DeLinkRequest.DeLinkST getRequestST() {
        return requestST;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname( String nickname ) {
        this.nickname = nickname;
    }
}
