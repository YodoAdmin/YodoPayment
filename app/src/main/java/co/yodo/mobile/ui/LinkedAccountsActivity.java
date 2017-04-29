package co.yodo.mobile.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.MenuItem;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import co.yodo.mobile.R;
import co.yodo.mobile.YodoApplication;
import co.yodo.mobile.business.network.ApiClient;
import co.yodo.mobile.business.network.model.ServerResponse;
import co.yodo.mobile.business.network.request.DeLinkRequest;
import co.yodo.mobile.helper.PrefUtils;
import co.yodo.mobile.model.dtos.ErrorEvent;
import co.yodo.mobile.model.dtos.LinkedAccount;
import co.yodo.mobile.ui.adapter.AccountsAdapter;

/**
 * Created by luis on 20/02/15.
 * Dialog to de-link accounts
 */
public class LinkedAccountsActivity extends BaseActivity {
    /** The context object */
    @Inject
    Context context;

    /** Manager for the server requests */
    @Inject
    ApiClient requestManager;

    /** GUI controllers */
    @BindView( R.id.layout_accounts_to )
    RecyclerView rvAccountsTo;

    @BindView( R.id.layout_accounts_from )
    RecyclerView rvAccountsFrom;

    /** Temporal pip */
    private String pip;

    /** Recycler view adapters */
    private AccountsAdapter toAdapter;
    private AccountsAdapter fromAdapter;

    /** Data to display */
    private final List<LinkedAccount> toAccounts = new ArrayList<>();
    private final List<LinkedAccount> fromAccounts = new ArrayList<>();

    /** Bundle keys for the intent */
    private static final String BUNDLE_ACC_TO   = "BUNDLE_ACC_TO";
    private static final String BUNDLE_ACC_FROM = "BUNDLE_ACC_FROM";
    private static final String BUNDLE_PIP      = "BUNDLE_PIP";

    /** Accounts separator */
    private static final String ACC_SEP = "-";

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_linked_accounts );

        setupGUI( savedInstanceState );
        updateData();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Remove the pendant accounts
        removeAccountsRemote( toAdapter.getAccountsPendingRemoval() );
        removeAccountsRemote( fromAdapter.getAccountsPendingRemoval() );
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
     * Starts the activity with the required values
     * @param context The application context
     * @param to The accounts that the account give money to
     * @param from The accounts that gives money to the account
     * @param pip The pip
     */
    public static void newInstance( Context context, String to, String from, String pip ) {
        Intent i = new Intent( context, LinkedAccountsActivity.class );
        i.putExtra( BUNDLE_ACC_TO, to );
        i.putExtra( BUNDLE_ACC_FROM, from );
        i.putExtra( BUNDLE_PIP, pip );
        context.startActivity( i );
    }

    /**
     * Configures the main GUI Controllers
     */
    @Override
    protected void setupGUI( Bundle savedInstanceState ) {
        super.setupGUI( savedInstanceState );
        // Injection
        YodoApplication.getComponent().inject( this );
    }

    @Override
    public void updateData() {
        super.updateData();
        // Get values from main activity
        final Bundle extras = getIntent().getExtras();
        if( extras == null ) {
            finish();
        } else {
            // Get data from the bundle
            final String to = extras.getString( BUNDLE_ACC_TO, "" );
            final String from = extras.getString( BUNDLE_ACC_FROM, "" );
            pip = extras.getString( BUNDLE_PIP, "" );

            // Fill the lists
            addDataToArray( to.split( ACC_SEP ), toAccounts, DeLinkRequest.DeLinkST.TO );
            addDataToArray( from.split( ACC_SEP ), fromAccounts, DeLinkRequest.DeLinkST.FROM );

            // Setup recyclers view
            rvAccountsTo.setLayoutManager( new LinearLayoutManager( context ) );
            rvAccountsFrom.setLayoutManager( new LinearLayoutManager( context ) );

            toAdapter = new AccountsAdapter( toAccounts );
            fromAdapter = new AccountsAdapter( fromAccounts );

            rvAccountsTo.setAdapter( toAdapter );
            rvAccountsFrom.setAdapter( fromAdapter );

            // Setup listeners
            setSwipeListener( rvAccountsTo, toAdapter, toAccounts );
            setSwipeListener( rvAccountsFrom, fromAdapter, fromAccounts );
        }
    }

    /**
     * Fills lists with data from arrays
     * @param data The data in an array
     * @param accounts The list to fill
     * @param st The type of sub-request used to select the list view
     */
    private void addDataToArray( String[] data, List<LinkedAccount> accounts, DeLinkRequest.DeLinkST st ) {
        for( String account : data ) {
            if( account != null && account.length() > 0 ) {
                LinkedAccount linked = new LinkedAccount( account, st );
                linked.setNickname( PrefUtils.getNickname( account ) );
                accounts.add( linked );
            }
        }
    }

    /**
     * Sets the listener for the recycler views to delete elements
     * @param rvAccounts The recycler view
     * @param adapter The adapter of the recycler view
     * @param accounts The list of accounts
     */
    private void setSwipeListener( RecyclerView rvAccounts, final AccountsAdapter adapter, final List<LinkedAccount> accounts ) {
        ItemTouchHelper.SimpleCallback ithCallback = new ItemTouchHelper.SimpleCallback( 0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT ) {
            @Override
            public boolean onMove( RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target ) {
                return false;
            }

            @Override
            public int getSwipeDirs( RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder ) {
                return super.getSwipeDirs( recyclerView, viewHolder );
            }

            @Override
            public void onSwiped( RecyclerView.ViewHolder viewHolder, int swipeDir ) {
                final int swipedPosition = viewHolder.getAdapterPosition();
                if( !adapter.isPendingRemoval( swipedPosition ) ) {
                    adapter.pendingRemoval( swipedPosition );
                } else {
                    final LinkedAccount account = accounts.get( swipedPosition );
                    adapter.remove( swipedPosition );
                    requestManager.invoke(
                            new DeLinkRequest( hardwareToken, pip, account.getHardwareToken(), account.getRequestST() ),
                            new ApiClient.RequestCallback() {
                                @Override
                                public void onResponse( ServerResponse response ) {
                                    if( !response.getCode().equals( ServerResponse.AUTHORIZED ) ) {
                                        // Show a notification about the error
                                        final String message = getString( R.string.error_server );
                                        EventBus.getDefault().post( new ErrorEvent( ErrorEvent.TYPE.DIALOG, message ) );

                                        // Restore the view
                                        accounts.add( account );
                                        adapter.notifyDataSetChanged();
                                    }
                                }

                                @Override
                                public void onError( String message ) {
                                    // Show a notification about the error
                                    EventBus.getDefault().post( new ErrorEvent( ErrorEvent.TYPE.DIALOG, message ) );

                                    // Restore the view
                                    accounts.add( account );
                                    adapter.notifyDataSetChanged();
                                }
                            }
                    );
                }
            }
        };

        // Attach the listener to delete entries
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper( ithCallback );
        itemTouchHelper.attachToRecyclerView( rvAccounts );
    }

    /**
     * Request the removal of a list of linked accounts from the server
     * @param removed The list of removed accounts
     */
    private void removeAccountsRemote( List<LinkedAccount> removed ) {
        for( LinkedAccount account : removed ) {
            requestManager.invoke(
                    new DeLinkRequest( hardwareToken, pip, account.getHardwareToken(), account.getRequestST() ),
                    new ApiClient.RequestCallback() {
                        @Override
                        public void onResponse( ServerResponse response ) {
                            if( !response.getCode().equals( ServerResponse.AUTHORIZED ) ) {
                                final String message = getString( R.string.error_server );
                                EventBus.getDefault().post( new ErrorEvent( ErrorEvent.TYPE.SNACKBAR, message ) );
                            }
                        }

                        @Override
                        public void onError( String message ) {
                            // Show a notification about the error
                            EventBus.getDefault().post( new ErrorEvent( ErrorEvent.TYPE.SNACKBAR, message ) );
                        }
                    }
            );
        }
    }
}
