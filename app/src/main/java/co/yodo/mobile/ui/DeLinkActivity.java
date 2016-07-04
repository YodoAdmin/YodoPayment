package co.yodo.mobile.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import co.yodo.mobile.R;
import co.yodo.mobile.helper.GUIUtils;
import co.yodo.mobile.network.request.DeLinkRequest;
import co.yodo.mobile.ui.notification.ProgressDialogHelper;
import co.yodo.mobile.ui.notification.ToastMaster;
import co.yodo.mobile.ui.notification.YodoHandler;
import co.yodo.mobile.network.model.ServerResponse;
import co.yodo.mobile.helper.AppConfig;
import co.yodo.mobile.helper.PrefUtils;
import co.yodo.mobile.component.Intents;
import co.yodo.mobile.network.YodoRequest;

/**
 * Created by luis on 20/02/15.
 * Dialog to de-link accounts
 */
public class DeLinkActivity extends AppCompatActivity implements YodoRequest.RESTListener {
    /** DEBUG */
    @SuppressWarnings( "unused" )
    private static final String TAG = DeLinkActivity.class.getSimpleName();

    /** The context object */
    private Context ac;

    /** Account identifiers */
    private String hardwareToken;
    private String pip;

    /** Messages Handler */
    private YodoHandler handlerMessages;

    /** Manager for the server requests */
    private YodoRequest mRequestManager;

    /** GUI controllers */
    private LinearLayout llTo;
    private LinearLayout llFrom;
    private View vCurrentDeLink;

    /** Response codes for the server requests */
    private static final int DELINK_REQ = 0x00;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        GUIUtils.setLanguage( DeLinkActivity.this );
        setContentView( R.layout.activity_delink );

        setupGUI();
        updateData();

        if( savedInstanceState != null && savedInstanceState.getBoolean( AppConfig.IS_SHOWING ) ) {
            ProgressDialogHelper.getInstance().createProgressDialog( ac );
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(
                AppConfig.IS_SHOWING,
                ProgressDialogHelper.getInstance().isProgressDialogShowing()
        );
    }

    @Override
    public void onResume() {
        super.onResume();
        mRequestManager.setListener( this );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ProgressDialogHelper.getInstance().destroyProgressDialog();
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item ) {
        int itemId = item.getItemId();
        switch( itemId ) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected( item );
    }

    /**
     * Configures the main GUI Controllers
     */
    private void setupGUI() {
        // get the context
        ac = DeLinkActivity.this;
        handlerMessages = new YodoHandler( DeLinkActivity.this );
        mRequestManager = YodoRequest.getInstance( ac );

        // GUI Global components
        llTo   = (LinearLayout) findViewById( R.id.toLayout );
        llFrom = (LinearLayout) findViewById( R.id.fromLayout );

        // Only used at creation
        Toolbar toolbar = (Toolbar) findViewById( R.id.actionBar );

        // Setup the toolbar
        setSupportActionBar( toolbar );
        ActionBar actionBar = getSupportActionBar();
        if( actionBar != null )
            actionBar.setDisplayHomeAsUpEnabled( true );
    }

    /**
     * Sets the main values
     */
    private void updateData() {
        // Gets the hardware token - account identifier
        hardwareToken = PrefUtils.getHardwareToken( ac );
        if( hardwareToken == null ) {
            ToastMaster.makeText( ac, R.string.message_no_hardware, Toast.LENGTH_LONG ).show();
            finish();
        }

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
                llTo.addView( getAccountCheckBox( account, DeLinkRequest.DeLinkST.TO ) );
        }

        temp = fromAccounts.split( "-" );
        for( String account : temp ) {
            if( account != null && account.length() > 0 )
                llFrom.addView( getAccountCheckBox( account, DeLinkRequest.DeLinkST.FROM ) );
        }
    }

    /**
     * Creates checkbox for each one of the linked accounts
     * @param text The identifier of the account
     * @param requestST The account/request type (giving -- to, or receiving -- from)
     * @return A TextView behaving as a CheckBox
     */
    private TextView getAccountCheckBox( final String text, final DeLinkRequest.DeLinkST requestST ) {
        final String linkedAccount = "..." + text.substring( text.length() - 5 );

        final TextView account = new TextView( ac );
        account.setGravity( Gravity.CENTER_VERTICAL );
        account.setTypeface( Typeface.DEFAULT_BOLD );
        account.setText( linkedAccount );
        account.setCompoundDrawablesWithIntrinsicBounds( R.drawable.yodo_heart, 0, 0, 0 );
        account.setContentDescription( requestST.getValue() );

        account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View account) {
                vCurrentDeLink = account;

                ProgressDialogHelper.getInstance().createProgressDialog( ac );
                mRequestManager.invoke( new DeLinkRequest(
                        DELINK_REQ,
                        hardwareToken,
                        pip,
                        text,
                        requestST
                ) );
            }
        });

        return account;
    }

    @Override
    public void onPrepare() {
    }

    @Override
    public void onResponse( int responseCode, ServerResponse response ) {
        ProgressDialogHelper.getInstance().destroyProgressDialog();
        String code, message;

        switch( responseCode ) {
            case DELINK_REQ:
                code = response.getCode();

                if( code.equals( ServerResponse.AUTHORIZED ) ) {
                    ( (LinearLayout) vCurrentDeLink.getParent() ).removeView( vCurrentDeLink );
                }

                message = response.getMessage();
                YodoHandler.sendMessage( handlerMessages, code, message );
                break;
        }
    }
}
