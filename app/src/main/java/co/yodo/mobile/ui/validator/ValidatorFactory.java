package co.yodo.mobile.ui.validator;

import android.widget.TextView;

import java.util.HashMap;

/**
 * Created by hei on 14/06/16.
 * Generates the validator objects using the Flyweight Design Pattern
 */
public class ValidatorFactory {
    /** The PIPValidators for the different TextViews */
    private static final HashMap<TextView, PIPValidator> valsByTextView = new HashMap<>();

    /**
     * Gets a PIP validator for a TextView and its confirmation
     * @param tvPIP The TextView with the pip
     * @param tvConfirmPIP The TextView with the confirmation pip
     * @return The PIPValidator object
     */
    public static PIPValidator getValidator( TextView tvPIP, TextView tvConfirmPIP ) {
        PIPValidator validator = valsByTextView.get( tvPIP );
        if( validator == null ) {
            if( tvConfirmPIP != null )
                validator = new PIPValidator( tvPIP, tvConfirmPIP );
            else
                validator = new PIPValidator( tvPIP );
            valsByTextView.put( tvPIP, validator );
        }
        return validator;
    }

    /**
     * Gets a PIP validator for a TextView
     * @param tvPIP The TextView with the pip
     * @return The PIPValidator object
     */
    public static PIPValidator getValidator( TextView tvPIP ) {
        return getValidator( tvPIP, null );
    }
}
