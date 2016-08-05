package co.yodo.mobile.ui.validator;

import android.content.Context;
import android.support.design.widget.TextInputLayout;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.TextView;

import co.yodo.mobile.R;
import co.yodo.mobile.helper.AppConfig;

/**
 * Created by hei on 14/06/16.
 * Validates the PIP
 */
public class PIPValidator {
    /** The context object */
    protected Context ac;

    /** The shake animation for wrong inputs */
    protected Animation aShake;

    /**
     * Validation for the pip
     * @param ac The Application context
     */
    public PIPValidator( Context ac ) {
        this.ac = ac;
        this.aShake = AnimationUtils.loadAnimation( ac, R.anim.shake );
    }

    /**
     * Validates the pip
     * @param tvPIP The EditText to validate
     * @return true if the verification was successful, or false if something failed
     */
    public boolean validate( EditText tvPIP ) throws NoSuchFieldException {
        final String pip = tvPIP.getText().toString();
        final TextInputLayout tilPip = (TextInputLayout) tvPIP.getParent();

        if( tilPip == null )
            throw new NoSuchFieldException( "No Input layout present" );

        // Validates the size of the pip
        if( pip.length() < AppConfig.MIN_PIP_LENGTH ) {
            tilPip.setError( ac.getString( R.string.pip_short ) );
            tvPIP.startAnimation( aShake );
            return false;
        }

        tilPip.setError( null );
        return true;
    }

    /**
     * Validates the pip and its confirmation to be equal
     * @param tvPIP The EditText to validate
     * @return true if the verification was successful, or false if something failed
     */
    public boolean validate( EditText tvPIP, EditText tvConfirmPIP ) throws NoSuchFieldException {
        final String pip = tvPIP.getText().toString();
        final String confirmPip = tvConfirmPIP.getText().toString();
        final TextInputLayout tilConfirmPip = (TextInputLayout) tvConfirmPIP.getParent();

        if( tilConfirmPip == null )
            throw new NoSuchFieldException( "No Input layout present" );

        // Validates the size of the pip
        if( !pip.equals( confirmPip ) ) {
            tilConfirmPip.setError( ac.getString( R.string.pip_different ) );
            tvConfirmPIP.startAnimation( aShake );
            return false;
        }

        tilConfirmPip.setError( null );
        return true;
    }
}
