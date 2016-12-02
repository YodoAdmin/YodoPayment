package co.yodo.mobile.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.yodo.mobile.R;
import co.yodo.mobile.YodoApplication;
import co.yodo.mobile.component.Intents;
import co.yodo.mobile.helper.AppConfig;
import co.yodo.mobile.helper.GUIUtils;
import co.yodo.mobile.helper.PrefUtils;
import co.yodo.mobile.network.ApiClient;
import co.yodo.mobile.network.model.ServerResponse;
import co.yodo.mobile.network.request.DeLinkRequest;
import co.yodo.mobile.network.request.LinkRequest;
import co.yodo.mobile.ui.adapter.LinksListViewAdapter;
import co.yodo.mobile.ui.adapter.model.LinkedAccount;
import co.yodo.mobile.ui.notification.AlertDialogHelper;
import co.yodo.mobile.ui.notification.ProgressDialogHelper;
import co.yodo.mobile.ui.notification.ToastMaster;
import co.yodo.mobile.ui.notification.YodoHandler;
import co.yodo.mobile.ui.option.LinkAccountOption;

/**
 * Created by luis on 20/02/15.
 * Dialog to de-link accounts
 */
public class DeLinkActivity extends AppCompatActivity implements ApiClient.RequestsListener {
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
    @Inject
    ApiClient mRequestManager;

    /** Progress dialog for the requests */
    @Inject
    ProgressDialogHelper mProgressManager;

    /** GUI controllers */
    @BindView( R.id.lvToLayout )
    ListView lvToLayout;

    @BindView( R.id.lvFromLayout )
    ListView lvFromLayout;

    private LinearLayout llTo;
    private LinearLayout llFrom;
    private View vCurrentDeLink;

    private int mTempPosition;

    private LinksListViewAdapter mToAdapter;
    private LinksListViewAdapter mFromAdapter;
    private LinksListViewAdapter mSelectedAdapter;

    private final ArrayList<LinkedAccount> alToAccounts = new ArrayList<>();
    private final ArrayList<LinkedAccount> alFromAccounts = new ArrayList<>();

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
            mProgressManager.createProgressDialog( ac );
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(
                AppConfig.IS_SHOWING,
                mProgressManager.isProgressDialogShowing()
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
        mProgressManager.destroyProgressDialog();
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


    @Override
    public void onCreateContextMenu( android.view.ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo ) {
        super.onCreateContextMenu( menu, v, menuInfo );
        MenuInflater inflater = getMenuInflater();
        inflater.inflate( R.menu.menu_links, menu );

        ListView selectedListView = (ListView) v;
        mSelectedAdapter = (LinksListViewAdapter) selectedListView.getAdapter();
    }

    @Override
    public boolean onContextItemSelected( MenuItem item ) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        final LinkedAccount account = (LinkedAccount) mSelectedAdapter.getItem( info.position );

        switch( item.getItemId() ) {
            case R.id.action_nickname:
                // Dialog
                LayoutInflater inflater = (LayoutInflater) ac.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
                final View layout = inflater.inflate( R.layout.dialog_with_nickname, new LinearLayout( ac ), false );
                final EditText etInput = (EditText) layout.findViewById( R.id.cetNickname );

                // Set old nickname
                final String nickname = PrefUtils.getNickname( ac, account.getHardwareToken() );
                if( nickname != null )
                    etInput.setText( nickname );

                final DialogInterface.OnClickListener okClick = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick( DialogInterface dialogInterface, int i ) {
                        String nickname = etInput.getText().toString();

                        if( nickname.length() == 0 )
                            nickname = null;

                        account.setNickname( nickname );
                        PrefUtils.saveNickname( ac, account.getHardwareToken(), nickname );
                        mSelectedAdapter.notifyDataSetChanged();
                    }
                };

                AlertDialogHelper.create(
                        ac,
                        null, null,
                        layout,
                        okClick
                ).show();

                return true;

            case R.id.action_delink:
                // Save the element position
                mTempPosition = info.position;

                // Start the de-link process
                mProgressManager.createProgressDialog( ac );
                mRequestManager.invoke( new DeLinkRequest(
                        DELINK_REQ,
                        hardwareToken,
                        pip,
                        account.getHardwareToken(),
                        account.getRequestST()
                ) );
                return true;

            default:
                return super.onContextItemSelected( item );
        }
    }

    /**
     * Configures the main GUI Controllers
     */
    private void setupGUI() {
        // get the context
        ac = DeLinkActivity.this;
        handlerMessages = new YodoHandler( DeLinkActivity.this );

        // Injection
        ButterKnife.bind( this );
        YodoApplication.getComponent().inject( this );

        // Setup the toolbar
        GUIUtils.setActionBar( this, R.string.title_activity_de_link );
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
            if( account != null && account.length() > 0 ) {
                LinkedAccount linked = new LinkedAccount( account, DeLinkRequest.DeLinkST.TO );

                String nickname = PrefUtils.getNickname( ac, account );
                if( nickname != null )
                    linked.setNickname( nickname );

                alToAccounts.add( linked );
            }
        }

        temp = fromAccounts.split( "-" );
        for( String account : temp ) {
            if( account != null && account.length() > 0 ) {
                LinkedAccount linked = new LinkedAccount( account, DeLinkRequest.DeLinkST.FROM );

                String nickname = PrefUtils.getNickname( ac, account );
                if( nickname != null )
                    linked.setNickname( nickname );

                alFromAccounts.add( linked );
            }
        }

        mToAdapter = new LinksListViewAdapter( ac, alToAccounts );
        mFromAdapter = new LinksListViewAdapter( ac, alFromAccounts );
        lvToLayout.setAdapter( mToAdapter );
        lvFromLayout.setAdapter( mFromAdapter );

        registerForContextMenu( lvToLayout );
        registerForContextMenu( lvFromLayout );
    }

    @Override
    public void onPrepare() {
    }

    @Override
    public void onResponse( int responseCode, ServerResponse response ) {
        mProgressManager.destroyProgressDialog();
        String code, message;

        switch( responseCode ) {
            case DELINK_REQ:
                code = response.getCode();

                if( code.equals( ServerResponse.AUTHORIZED ) ) {
                    mSelectedAdapter.remove( mTempPosition );
                    mSelectedAdapter.notifyDataSetChanged();
                }

                message = response.getMessage();
                YodoHandler.sendMessage( handlerMessages, code, message );
                break;
        }
    }
}
