package co.yodo.mobile.ui.option;

import android.content.Context;
import android.support.design.widget.TextInputEditText;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import co.yodo.mobile.R;
import co.yodo.mobile.helper.AlertDialogHelper;
import co.yodo.mobile.ui.BaseActivity;
import co.yodo.mobile.ui.option.contract.IRequestOption;

/**
 * Created by hei on 14/06/16.
 * Implements the Input Linking Code Option of the MainActivity
 */
public class LinkAccountOption extends IRequestOption {
    /**
     * Sets up the main elements of the options
     * @param activity The Activity to handle
     */
    public LinkAccountOption( BaseActivity activity ) {
        super( activity );

        // Dialog
        LayoutInflater inflater = (LayoutInflater) this.activity.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        final View layout = inflater.inflate( R.layout.dialog_with_code, new LinearLayout( this.activity ), false );
        etInput = (TextInputEditText ) layout.findViewById( R.id.cetLinkingCode );
        final View.OnClickListener okClick = new View.OnClickListener() {
            @Override
            public void onClick( View view  ) {
                alertDialog.dismiss();
                final String linkingCode = etInput.getText().toString();

                // Start the request, and set the listener to this object
                progressManager.create( LinkAccountOption.this.activity );
                //requestManager.setListener( LinkAccountOption.this );
                /*requestManager.invoke( new LinkRequest(
                        LINK_REQ,
                        hardwareToken,
                        linkingCode
                ) );*/
            }
        };

        alertDialog = AlertDialogHelper.create(
                activity,
                layout,
                buildOnClick( okClick )
        );
    }

    @Override
    public void execute() {
        etInput.setText( "" );
        alertDialog.show();
        etInput.requestFocus();
    }

    /*@Override
    public void onPrepare() {
        PrefUtils.setSubscribing( activity, false );
    }

    @Override
    public void onResponse( int responseCode, ServerResponse response ) {
        // Set listener to the principal activity
        progressManager.destroy();
        requestManager.setListener( (MainActivity) activity );

        // Get the response code
        String code = response.getCode();

        switch( responseCode ) {
            case LINK_REQ:
                String message = response.getMessage();
                YodoHandler.sendMessage( mHandlerMessages, code, message );
                break;
        }
    }*/
}
