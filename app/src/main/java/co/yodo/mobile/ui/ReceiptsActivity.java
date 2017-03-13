package co.yodo.mobile.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.ActionMode;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.yodo.mobile.R;
import co.yodo.mobile.YodoApplication;
import co.yodo.mobile.helper.FormatUtils;
import co.yodo.mobile.model.db.Receipt;
import co.yodo.mobile.ui.adapter.ReceiptsAdapter;
import co.yodo.mobile.ui.dialog.ReceiptDialog;
import co.yodo.mobile.utils.GuiUtils;

public class ReceiptsActivity extends BaseActivity implements
        /*AdapterView.OnItemClickListener,
        AdapterView.OnItemLongClickListener,
        ActionMode.Callback,*/
        LoaderManager.LoaderCallbacks<List<Receipt>> {
    /** The application context */
    @Inject
    Context context;

    /** GUI Controllers */
    @BindView( R.id.layout_receipts )
    RecyclerView rvReceipts;

    /** Receipts data */
    private final List<Receipt> receipts = new ArrayList<>();
    private ReceiptsAdapter adapter;

    private SearchView searchView;

    /** Action Mode (Action bar) */
    private ActionMode actionMode;
    private boolean isActionModeShowing = false;

    /** Identifier for the loader */
    private static final int THE_LOADER = 0x01;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        //GUIUtils.setLanguage( ReceiptsActivity.this );
        setContentView( R.layout.activity_receipts );

        setupGUI();
        updateData();
    }

    /*@Override
    protected void onNewIntent(Intent intent) {
        if ( Intent.ACTION_SEARCH.equals( intent.getAction() ) ) {
            String query = intent.getStringExtra( SearchManager.QUERY );
            searchView.setQuery( query, false );
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate( R.menu.menu_receipts, menu );

        SearchManager searchManager = (SearchManager) getSystemService( Context.SEARCH_SERVICE );
        searchView = (SearchView) menu.findItem( R.id.action_search ).getActionView();

        searchView.setSearchableInfo( searchManager.getSearchableInfo( getComponentName() ) );
        searchView.setOnQueryTextListener( new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange( String newText ) {
                rlvAdapter.getFilter().filter( newText );
                return true;
            }
            @Override
            public boolean onQueryTextSubmit( String query ) {
                rlvAdapter.getFilter().filter( query );
                searchView.clearFocus();
                return true;
            }
        } );

        return super.onCreateOptionsMenu( menu );
    }*/

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

    /*@Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu( menu, v, menuInfo );

        if( v.getId() == R.id.layout_receipts ) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate( R.menu.menu_receipts, menu );
        }
    }*/

    private void setupGUI() {
        // Injection
        ButterKnife.bind( this );
        YodoApplication.getComponent().inject( this );

        // Setup the toolbar
        GuiUtils.setActionBar( this );

        // Manager for the recycler view
        LinearLayoutManager llManager = new LinearLayoutManager( context );
        rvReceipts.setLayoutManager( llManager );

        // Create adapter
        adapter = new ReceiptsAdapter( context, receipts );
        rvReceipts.setAdapter( adapter );
    }

    @Override
    public void updateData() {
        //rvReceipts.setOnItemClickListener( this );
        //rvReceipts.setOnItemLongClickListener( this );

        getSupportLoaderManager().initLoader( THE_LOADER, null, this ).forceLoad();
    }

    /**
     * Builds the dialog for the receipt
     * @param params The receipt data
     */
    /*private void buildReceiptDialog( Receipt params ) {
        // The receipt dialog formats the values
        new ReceiptDialog.Builder( context )
                .cancelable( true )
                .description( params.getDescription() )
                .created( params.getCreated() )
                .total( params.getTotalAmount(), params.getTCurrency() )
                .authnumber( params.getAuthNumber() )
                .donor( params.getDonorAccount() )
                .recipient( params.getRecipientAccount() )
                .tender( params.getTenderAmount(), params.getDCurrency())
                .cashback( params.getCashBackAmount(), params.getTCurrency() )
                .build();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Receipt receipt = rlvAdapter.getItem( position );

        if( receipt != null && !receipt.isOpened() ) {
            receipt.setOpened( true );
            //receiptsdb.updateReceipt( receipt );
        }

        buildReceiptDialog( receipt );
        rlvAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onItemLongClick( AdapterView<?> parent, View view, int position, long id ) {
        //ReceiptsListViewAdapter adapter = (ReceiptsListViewAdapter) rvReceipts.getAdapter();
        Receipt receipt = rlvAdapter.getItem( position );

        ReceiptsAdapter.ViewHolder holder = (ReceiptsAdapter.ViewHolder) view.getTag();
        if( receipt != null )
            receipt.setChecked( !receipt.isChecked );
        rlvAdapter.toggleSelection( position );
        rlvAdapter.updateCheckedState( holder, receipt );

        // Look for items to delete
        int selectedCount = rlvAdapter.getSelectedCount();
        if( selectedCount > 0 ) {
            if( !isActionModeShowing ) {
                actionMode = ReceiptsActivity.this.startActionMode( ReceiptsActivity.this );
                isActionModeShowing = true;
            }
        }
        else if( actionMode != null ) {
            actionMode.finish();
            isActionModeShowing = false;
        }

        if( actionMode != null ) {
            actionMode.setTitle( String.valueOf( selectedCount ) );
            actionMode.invalidate();
        }

        return true;
    }

    @Override
    public boolean onCreateActionMode( ActionMode mode, Menu menu ) {
        // Inflate a menu resource providing context menu items
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate( R.menu.receipts_multi, menu );

        MenuItem item = menu.findItem( R.id.menu_item_share );
        ShareActionProvider mShareActionProvider = ( ShareActionProvider ) MenuItemCompat.getActionProvider( item );
        MenuItemCompat.setActionProvider(item, mShareActionProvider );

        return true;
    }

    @Override
    public boolean onPrepareActionMode( ActionMode mode, Menu menu ) {
        int selectedCount = rlvAdapter.getSelectedCount();
        if( selectedCount == 1 ) {
            MenuItem item = menu.findItem( R.id.menu_item_share );
            item.setVisible( true );
            return true;
        } else {
            MenuItem item = menu.findItem( R.id.menu_item_share );
            item.setVisible( false );
            return true;
        }
    }

    @Override
    public boolean onActionItemClicked( ActionMode mode, MenuItem item ) {
        switch( item.getItemId() ) {
            case R.id.delete:
                final List<Receipt> removed = rlvAdapter.removeSelected();
                for( Receipt receipt : removed )
                    receiptsdb.deleteReceipt( receipt );
                rlvAdapter.notifyDataSetChanged();

                // Show a notification which can reverse the delete
                String message = removed.size() + " " + getString( R.string.text_deleted );
                Snackbar.make( rvReceipts, message, Snackbar.LENGTH_LONG )
                        .setAction( R.string.text_undo, new View.OnClickListener() {
                            @Override
                            public void onClick( View v ) {
                                v.setEnabled( false );

                                rlvAdapter.addAll( removed );
                                rlvAdapter.sort( ReceiptsAdapter.ReceiptsComparator );

                                for( Receipt receipt : removed )
                                    receiptsdb.addReceipt( receipt );
                            }
                        } ).show();

                mode.finish();
                return true;

            case R.id.menu_item_share:
                Receipt receipt = rlvAdapter.getSelected();

                Intent sendIntent = new Intent();
                sendIntent.setAction( Intent.ACTION_SEND );
                sendIntent.putExtra( Intent.EXTRA_SUBJECT, getString( R.string.title_activity_receipts ) + ": " + receipt.getDescription() );
                sendIntent.putExtra( Intent.EXTRA_TEXT, formatReceipt( receipt ) );
                sendIntent.setType( "text/plain" );
                startActivity( Intent.createChooser( sendIntent, getResources().getText( R.string.action_share ) ) );
                return true;

            default:
                return false;
        }
    }

    @Override
    public void onDestroyActionMode( ActionMode mode ) {
        isActionModeShowing = false;
        // Clears the deleted list, delete aborted
        rlvAdapter.clearDeleteList();
        rlvAdapter.notifyDataSetChanged();
    }

    @SuppressWarnings("unused") // receives GCM receipts
    @Subscribe( threadMode = ThreadMode.MAIN )
    public void onReceiptEvent( Receipt receipt ) {
        //adapter.addReceipt( receipt );
        rlvAdapter.add( receipt );
        rlvAdapter.sort( ReceiptsAdapter.ReceiptsComparator );
        rlvAdapter.notifyDataSetChanged();
    }*/

    @Override
    public Loader<List<Receipt>> onCreateLoader( int id, Bundle args ) {
        return new LoadTask( this );
    }

    @Override
    public void onLoadFinished( Loader<List<Receipt>> loader, List<Receipt> data ) {
        //rlvAdapter.addAll( data );
        //rvReceipts.setAdapter( rlvAdapter );
        receipts.addAll( data );
        adapter.notifyDataSetChanged();
        getSupportLoaderManager().destroyLoader( THE_LOADER );
    }

    @Override
    public void onLoaderReset( Loader<List<Receipt>> loader ) {
    }

    /**
     * Loads the receipts from the database
     */
    private static class LoadTask extends AsyncTaskLoader<List<Receipt>> {
        /**
         * Sets the receipts from the caller
         * @param context The application context
         */
        LoadTask( Context context ) {
            super( context );
        }

        @Override
        public List<Receipt> loadInBackground() {
            return Receipt.listAll( Receipt.class );
        }
    }

    /**
     * Gives a string format to a receipt
     * @param receipt The receipt data
     * @return A formatted String
     */
    private String formatReceipt( Receipt receipt ) {
        return String.format(
                "%s\n" +
                "AU# %s\t\t%s\n" +
                "Total:\t\t%s %s\n" +
                "CashTender:\t%s %s\n" +
                "CashBack:\t%s %s\n\n" +
                "Donor: %s\n" +
                "Recipient: %s",
                receipt.getDescription(),
                receipt.getAuthNumber() ,
                receipt.getCreated(),
                FormatUtils.truncateDecimal( receipt.getTotalAmount() ), receipt.getTCurrency(),
                FormatUtils.truncateDecimal( receipt.getTenderAmount() ), receipt.getDCurrency(),
                FormatUtils.truncateDecimal( receipt.getCashBackAmount() ), receipt.getTCurrency(),
                FormatUtils.replaceNull( receipt.getDonorAccount() ),
                receipt.getRecipientAccount()
        );
    }
}
