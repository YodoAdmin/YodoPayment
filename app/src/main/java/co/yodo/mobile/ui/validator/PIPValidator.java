package co.yodo.mobile.ui.validator;

import android.content.Context;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import co.yodo.mobile.R;
import co.yodo.mobile.helper.AppConfig;
import co.yodo.mobile.ui.notification.ToastMaster;

/**
 * Created by hei on 14/06/16.
 * Validates the PIP
 */
public class PIPValidator {
    /** The context object */
    private Context ac;

    /** GUI components to verify */
    private TextView tvPIP;
    private TextView tvConfirmPIP;

    /** The shake animation for wrong inputs */
    private Animation aShake;

    /** PIP characters filter */
    private static final InputFilter filter = new InputFilter() {
        public CharSequence filter( CharSequence source, int start, int end, Spanned s, int ds, int de ) {
            for( int i = start; i < end; i++ ) {
                if( !Character.isLetterOrDigit( source.charAt( i ) ) ) {
                    return "";
                }
            }
            return null;
        }
    };

    /**
     * Validates the pip
     * @param tvPIP The TextView with the PIP
     */
    public PIPValidator( TextView tvPIP ) {
        this.ac = tvPIP.getContext().getApplicationContext();
        this.tvPIP = tvPIP;
        this.aShake = AnimationUtils.loadAnimation( ac, R.anim.shake );
        this.tvPIP.setFilters( new InputFilter[]{filter} );
    }

    /**
     * Validates the PIP and the confirmation
     * @param tvPIP The TextView with the PIP
     * @param tvConfirmPIP The TextView with the confirmation PIP
     */
    public PIPValidator( TextView tvPIP, TextView tvConfirmPIP ) {
        this( tvPIP );
        this.tvConfirmPIP = tvConfirmPIP;
        this.tvConfirmPIP.setFilters( new InputFilter[]{filter} );
    }

    /**
     * Validates the pip (and confirmation if exists)
     * @return true if the verification was successful, or false if something failed
     */
    public boolean validate() {
        // Validates the size of the pip
        final String pip = this.tvPIP.getText().toString();
        if( pip.length() < AppConfig.MIN_PIP_LENGTH ) {
            ToastMaster.makeText( ac, R.string.pip_short, Toast.LENGTH_SHORT ).show();
            this.tvPIP.startAnimation( aShake );
            return false;
        }

        // If we have a confirmation PIP lets verify if it is equal to the pip
        if( this.tvConfirmPIP != null ) {
            final String confirmPip = tvConfirmPIP.getText().toString();
            if( !pip.equals( confirmPip ) ) {
                ToastMaster.makeText( ac, R.string.pip_different, Toast.LENGTH_SHORT ).show();
                this.tvConfirmPIP.startAnimation( aShake );
                return false;
            }
        }

        return true;
    }
}
