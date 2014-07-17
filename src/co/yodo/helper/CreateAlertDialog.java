package co.yodo.helper;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import co.yodo.R;

/**
 * Created by luis on 24/07/13.
 */
public class CreateAlertDialog {
    public static void showAlertDialog(final Context context, View layout, EditText input, String title, String message,
                                       DialogInterface.OnClickListener okButtonClickListener,
                                       DialogInterface.OnClickListener cancelButtonClickListener) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setInverseBackgroundForced(true);
        builder.setIcon(R.drawable.ic_launcher);

        if(layout != null)
        	builder.setView(layout);

        if(title != null)
            builder.setTitle(title);

        if(message != null)
            builder.setMessage(message);

        builder.setCancelable(true);

        if(okButtonClickListener != null)
            builder.setPositiveButton(context.getString(R.string.ok), okButtonClickListener);

        if(cancelButtonClickListener == null) {
            builder.setNegativeButton(context.getString(R.string.cancel), null);
        } else {
            builder.setNegativeButton(context.getString(R.string.cancel), cancelButtonClickListener);
        }

        final AlertDialog alertDialog = builder.create();
        input.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus) {
                    alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            }
        });

        alertDialog.show();
    }

    public static void showAlertDialog(final Context context, String title, String message,
                                       DialogInterface.OnClickListener okButtonClickListener) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setInverseBackgroundForced(true);
        builder.setIcon(R.drawable.ic_launcher);

        if(title != null)
            builder.setTitle(title);

        if(message != null)
            builder.setMessage(message);

        builder.setCancelable(true);

        if(okButtonClickListener != null)
            builder.setPositiveButton(context.getString(R.string.ok), okButtonClickListener);

        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
