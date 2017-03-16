package co.yodo.mobile.model.dtos;

/**
 * Created by hei on 12/06/16.
 * POJO for the registration response of the gcm_id
 */
public class GCMResponse {
    /** GCM attributes */
    private String message;

    public GCMResponse( String message ) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
