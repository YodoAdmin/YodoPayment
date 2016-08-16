package co.yodo.mobile.ui.option.contract;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.method.DigitsKeyListener;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;

import javax.inject.Inject;

import co.yodo.mobile.R;
import co.yodo.mobile.YodoApplication;
import co.yodo.mobile.helper.GUIUtils;
import co.yodo.mobile.network.ApiClient;
import co.yodo.mobile.ui.components.ClearEditText;
import co.yodo.mobile.ui.notification.ProgressDialogHelper;
import co.yodo.mobile.ui.validator.PIPValidator;

/**
 * Created by hei on 14/06/16.
 * The abstract class used to implement the Command Design Pattern for the
 * different options
 */
public abstract class IOption {
    /** Main options elements */
    protected final Activity mActivity;
    protected AlertDialog mAlertDialog;

    /**
     * Sets up the main elements of the options
     * @param activity The Activity to handle
     */
    protected IOption( Activity activity ) {
        this.mActivity = activity;
    }

    /**
     * Executes an option
     */
    public abstract void execute();
}
