package co.yodo.mobile.model.dtos;

/**
 * Created by ltalavera on 3/1/17.
 * An event that represents an error with a message
 */
public class ErrorEvent {
    /** Related information to the error */
    public TYPE tag;
    public String message;

    /** Type of alerts */
    public enum TYPE {
        DIALOG,
        SNACKBAR
    }

    public ErrorEvent( TYPE tag, String message ) {
        this.tag = tag;
        this.message = message;
    }
}
