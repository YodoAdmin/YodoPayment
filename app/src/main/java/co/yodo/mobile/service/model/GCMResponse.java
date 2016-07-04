package co.yodo.mobile.service.model;

import co.yodo.mobile.network.model.ServerResponse;

/**
 * Created by hei on 12/06/16.
 * POJO for the registration response of the gcm_id
 */
public class GCMResponse {
    private String code;
    private String authNumber;
    private String message;
    private long rtime;

    public GCMResponse( ServerResponse response ) {
        this.code = response.getCode();
        this.authNumber = response.getAuthNumber();
        this.message = response.getMessage();
        this.rtime = response.getRTime();
    }

    public GCMResponse( Exception exception ) {
        this.message = exception.getMessage();
        this.rtime = System.currentTimeMillis();
    }

    public String getCode() {
        return code;
    }

    public String getAuthNumber() {
        return authNumber;
    }

    public String getMessage() {
        return message;
    }

    public long getRtime() {
        return rtime;
    }
}
