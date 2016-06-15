package co.yodo.mobile.ui.extension.contract;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import co.yodo.mobile.ui.components.ClearEditText;

/**
 * Created by hei on 14/06/16.
 * The abstract class used to implement the Command Design Pattern for the
 * different options
 */
public abstract class IOption {
    /** Main options elements */
    protected final Activity mActivity;
    protected final EditText etInput;
    protected AlertDialog mAlertDialog;

    /**
     * Sets up the main elements of the options
     * @param activity The Activity to handle
     */
    public IOption( Activity activity ) {
        this.mActivity = activity;
        this.etInput = new ClearEditText( this.mActivity );
    }

    /**
     * Builds a listener for the positive button
     * @param onPositive The new procedure for the positive button
     * @return The listener
     */
    protected DialogInterface.OnShowListener buildOnClick( final View.OnClickListener onPositive ) {
        this.etInput.setText( "" );
        return new DialogInterface.OnShowListener() {
            @Override
            public void onShow( DialogInterface dialog ) {
                // Get the AlertDialog and the positive Button
                mAlertDialog = AlertDialog.class.cast( dialog );
                final Button button = mAlertDialog.getButton( AlertDialog.BUTTON_POSITIVE );

                // Sets the action for the positive Button
                button.setOnClickListener( onPositive );
            }
        };
    }

    /**
     * Executes an option
     */
    public abstract void execute();
}
