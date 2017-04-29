package co.yodo.mobile.ui.option;

import android.content.Context;
import android.content.Intent;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import co.yodo.mobile.BuildConfig;
import co.yodo.mobile.R;
import co.yodo.mobile.YodoApplication;
import co.yodo.mobile.helper.AlertDialogHelper;
import co.yodo.mobile.helper.PrefUtils;
import co.yodo.mobile.ui.BaseActivity;
import co.yodo.mobile.ui.option.contract.IOption;

/**
 * Created by hei on 14/06/16.
 * Implements the About Option of the MainActivity
 */
public class AboutOption extends IOption {
    /**
     * Sets up the main elements of the options
     * @param activity The Activity to handle
     */
    public AboutOption( BaseActivity activity ) {
        super( activity );

        // Gets and sets the dialog layout
        LayoutInflater inflater = (LayoutInflater) this.activity.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        final View layout = inflater.inflate( R.layout.dialog_about, new LinearLayout( this.activity ), false );
        setupLayout( layout );
    }

    @Override
    public void execute() {
        alertDialog.show();
    }

    /**
     * Prepares a layout for the About dialog
     * @param layout The layout to be prepared
     */
    private void setupLayout( View layout ) {
        // GUI controllers of the dialog
        TextView emailView = (TextView) layout.findViewById( R.id.tvEmail );
        TextView messageView = (TextView) layout.findViewById( R.id.tvMessage );

        // Get data
        final String hardwareToken = PrefUtils.getHardwareToken();
        final String message = activity.getString( R.string.text_version ) + " " +
                BuildConfig.VERSION_NAME + "/" +
                YodoApplication.getSwitch()    + "\n\n" +
                activity.getString( R.string.text_about );
        final String email = activity.getString( R.string.text_about_email );

        // Set text to the controllers
        SpannableString ssEmail = new SpannableString( email );
        ssEmail.setSpan( new UnderlineSpan(), 0, ssEmail.length(), 0 );
        emailView.setText( ssEmail );
        messageView.setText( message  );

        // Create the onClick listener
        emailView.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent( Intent.ACTION_SEND );
                String[] recipients = { email };
                intent.putExtra( Intent.EXTRA_EMAIL, recipients ) ;
                intent.putExtra( Intent.EXTRA_SUBJECT, hardwareToken );
                intent.setType( "text/html" );
                activity.startActivity(
                        Intent.createChooser( intent, activity.getString( R.string.text_send_mail ) )
                );
            }
        });

        // Generate the AlertDialog
        alertDialog = AlertDialogHelper.create(
                activity,
                R.string.action_about,
                layout
        );
    }
}
