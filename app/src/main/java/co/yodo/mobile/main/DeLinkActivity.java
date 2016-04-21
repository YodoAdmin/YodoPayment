package co.yodo.mobile.main;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import co.yodo.mobile.R;
import co.yodo.mobile.component.YodoHandler;
import co.yodo.mobile.data.ServerResponse;
import co.yodo.mobile.helper.AppConfig;
import co.yodo.mobile.helper.AppUtils;
import co.yodo.mobile.helper.Intents;
import co.yodo.mobile.net.ServerRequest;
import co.yodo.mobile.net.YodoRequest;

/**
 * Created by luis on 20/02/15.
 * Dialog to de-link accounts
 */
public class DeLinkActivity extends AppCompatActivity implements YodoRequest.RESTListener {
    /** The context object */
    private Context ac;

    /** Hardware Identifier */
    private String hardwareToken;

    /** PIP */
    private String pip;

    /** GUI controllers */
    private LinearLayout toLayout;
    private LinearLayout fromLayout;
    private View currentDeLink;

    /** Messages Handler */
    private static YodoHandler handlerMessages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppUtils.setLanguage( DeLinkActivity.this );
        setContentView(R.layout.activity_delink);

        setupGUI();
        updateData();

        if( savedInstanceState != null && savedInstanceState.getBoolean( AppConfig.IS_SHOWING ) ) {
            YodoRequest.getInstance().createProgressDialog(
                    DeLinkActivity.this ,
                    YodoRequest.ProgressDialogType.NORMAL
            );
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(
                AppConfig.IS_SHOWING,
                YodoRequest.getInstance().progressDialogShowing()
        );
    }

    @Override
    public void onResume() {
        super.onResume();
        YodoRequest.getInstance().setListener( this );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        YodoRequest.getInstance().destroyProgressDialog();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch( itemId ) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupGUI() {
        // get the context
        ac = DeLinkActivity.this;
        // Handler
        handlerMessages = new YodoHandler( DeLinkActivity.this );

        YodoRequest.getInstance().setListener( this );

        toLayout   = (LinearLayout) findViewById( R.id.toLayout );
        fromLayout = (LinearLayout) findViewById( R.id.fromLayout );

        // Only used at creation
        Toolbar actionBarToolbar = (Toolbar) findViewById( R.id.actionBar );

        setSupportActionBar( actionBarToolbar );
        if( getSupportActionBar() != null )
            getSupportActionBar().setDisplayHomeAsUpEnabled( true );
    }

    private void updateData() {
        hardwareToken = AppUtils.getHardwareToken( ac );

        Bundle extras = getIntent().getExtras();
        if( extras == null ) {
            finish();
            return;
        }

        String toAccounts   = extras.getString( Intents.LINKED_ACC_TO, "" );
        String fromAccounts = extras.getString( Intents.LINKED_ACC_FROM, "" );
        pip = extras.getString( Intents.LINKED_PIP, "" );

        String[] temp = toAccounts.split( "-" );
        for( String account : temp ) {
            if( account != null && account.length() > 0 )
                toLayout.addView( getAccountCheckBox( account, ServerRequest.DELINK_TO_SUBREQ ) );
        }

        temp = fromAccounts.split( "-" );
        for( String account : temp ) {
            if( account != null && account.length() > 0 )
                fromLayout.addView( getAccountCheckBox( account, ServerRequest.DELINK_FROM_SUBREQ ) );
        }
    }

    private TextView getAccountCheckBox(final String text, final String accountType) {
        final TextView account = new TextView( ac );
        account.setGravity( Gravity.CENTER_VERTICAL );
        account.setTypeface( Typeface.DEFAULT_BOLD );
        account.setText( "..." + text.substring( text.length() - 5 ) );
        account.setCompoundDrawablesWithIntrinsicBounds( R.drawable.yodo_heart, 0, 0, 0 );
        account.setContentDescription( accountType );

        account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View account) {
                /*final String title = ac.getString(R.string.input_pip);
                final EditText inputBox = new ClearEditText( ac );*/

                currentDeLink = account;

                YodoRequest.getInstance().createProgressDialog(
                        ac,
                        YodoRequest.ProgressDialogType.NORMAL
                );

                YodoRequest.getInstance().requestDeLinkAccount(
                        DeLinkActivity.this,
                        hardwareToken, pip,
                        text,
                        accountType
                );

                /*DialogInterface.OnClickListener okClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String pip = inputBox.getText().toString();

                        if (pip.length() < AppConfig.MIN_PIP_LENGTH) {
                            ToastMaster.makeText(ac, R.string.pip_short, Toast.LENGTH_SHORT).show();
                        } else {
                            currentDeLink = account;

                            YodoRequest.getInstance().createProgressDialog(
                                    ac,
                                    YodoRequest.ProgressDialogType.NORMAL
                            );

                            YodoRequest.getInstance().requestDeLinkAccount(
                                    DeLinkActivity.this,
                                    hardwareToken, pip,
                                    text,
                                    accountType
                            );
                        }
                    }
                };

                AlertDialogHelper.showAlertDialog(
                        ac,
                        title,
                        null,
                        inputBox,
                        okClickListener,
                        null
                );*/
            }
        });

        return account;
    }

    @Override
    public void onResponse(YodoRequest.RequestType type, ServerResponse response) {
        YodoRequest.getInstance().destroyProgressDialog();
        String code, message;

        switch( type ) {
            case ERROR_NO_INTERNET:
                handlerMessages.sendEmptyMessage( YodoHandler.NO_INTERNET );
                break;

            case ERROR_GENERAL:
                handlerMessages.sendEmptyMessage( YodoHandler.GENERAL_ERROR );
                break;

            case DELINK_ACC_REQUEST:
                code = response.getCode();

                if( code.equals( ServerResponse.AUTHORIZED ) ) {
                    ( (LinearLayout) currentDeLink.getParent() ).removeView( currentDeLink );
                    AppUtils.clearLinkedAccount( ac );
                }

                message = response.getMessage();
                AppUtils.sendMessage( handlerMessages, code, message );
                break;
        }
    }
}
