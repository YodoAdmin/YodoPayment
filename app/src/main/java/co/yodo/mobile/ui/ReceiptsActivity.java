package co.yodo.mobile.ui;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.ShareActionProvider;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.orm.query.Select;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import co.yodo.mobile.R;
import co.yodo.mobile.YodoApplication;
import co.yodo.mobile.model.db.Receipt;
import co.yodo.mobile.ui.adapter.ReceiptsAdapter;
import timber.log.Timber;

public class ReceiptsActivity extends BaseActivity implements
        ReceiptsAdapter.OnLongClickListener,
        ActionMode.Callback,
        LoaderManager.LoaderCallbacks<List<Receipt>> {
    /** The application context */
    @Inject
    Context context;

    /** GUI Controllers */
    @BindView( R.id.rvReceipts )
    RecyclerView rvReceipts;

    /** Receipts data */
    private ReceiptsAdapter adapter;

    /** Action Mode (Action bar) */
    private ActionMode actionMode;
    private boolean isActionModeShowing = false;
    private SearchView searchView;

    /** Identifier for the loader */
    private static final int THE_LOADER = 0x01;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_receipts );

        setupGUI( savedInstanceState );
        updateData();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if ( Intent.ACTION_SEARCH.equals( intent.getAction() ) ) {
            String query = intent.getStringExtra( SearchManager.QUERY );
            searchView.setQuery( query, false );
        }
    }

    @Override
    public boolean onCreateOptionsMenu( Menu menu ) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate( R.menu.menu_receipts, menu );

        SearchManager searchManager = (SearchManager) getSystemService( Context.SEARCH_SERVICE );
        searchView = (SearchView) menu.findItem( R.id.action_search ).getActionView();

        searchView.setSearchableInfo( searchManager.getSearchableInfo( getComponentName() ) );
        searchView.setOnQueryTextListener( new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange( String newText ) {
                adapter.getFilter().filter( newText );
                return true;
            }

            @Override
            public boolean onQueryTextSubmit( String query ) {
                adapter.getFilter().filter( query );
                searchView.clearFocus();
                return true;
            }
        } );

        return super.onCreateOptionsMenu( menu );
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
    public void onCreateContextMenu( ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo ) {
        super.onCreateContextMenu( menu, v, menuInfo );

        if( v.getId() == R.id.rvReceipts ) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate( R.menu.menu_receipts, menu );
        }
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
        int selectedCount = adapter.getSelectedCount();
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
                final List<Receipt> removed = adapter.removeSelected();
                adapter.removeAll( removed );
                adapter.notifyDataSetChanged();

                // Show a notification which can reverse the delete
                String message = removed.size() + " " + getString( R.string.text_deleted );
                Snackbar.make( rvReceipts, message, Snackbar.LENGTH_LONG )
                        .setAction( R.string.text_undo, new View.OnClickListener() {
                            @Override
                            public void onClick( View v ) {
                                v.setEnabled( false );
                                adapter.addAll( removed );
                                adapter.sort();
                                for( Receipt receipt : removed ) {
                                    receipt.save();
                                }
                                adapter.notifyDataSetChanged();
                            }
                        } ).show();
                break;

            case R.id.menu_item_share:
                Receipt receipt = adapter.getSelected();

                Intent sendIntent = new Intent();
                sendIntent.setAction( Intent.ACTION_SEND );
                sendIntent.putExtra( Intent.EXTRA_SUBJECT, getString( R.string.title_activity_receipts ) + ": " + receipt.getDescription() );
                sendIntent.putExtra( Intent.EXTRA_TEXT, receipt.toString() );
                sendIntent.setType( "text/plain" );
                startActivity( Intent.createChooser( sendIntent, getResources().getText( R.string.action_share ) ) );
                break;
        }

        mode.finish();
        return true;
    }

    @Override
    public void onDestroyActionMode( ActionMode mode ) {
        isActionModeShowing = false;
        adapter.clearSelected();
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void setupGUI( Bundle savedInstanceState ) {
        super.setupGUI( savedInstanceState );
        // Injection
        YodoApplication.getComponent().inject( this );

        // Manager for the recycler view
        LinearLayoutManager llManager = new LinearLayoutManager( context );
        rvReceipts.setLayoutManager( llManager );

        // Create adapter
        adapter = new ReceiptsAdapter( context, this );
        rvReceipts.setAdapter( adapter );
    }

    @Override
    public void updateData() {
        getSupportLoaderManager().initLoader( THE_LOADER, null, this ).forceLoad();
    }

    @Override
    public Loader<List<Receipt>> onCreateLoader( int id, Bundle args ) {
        return new LoadTask( this );
    }

    @Override
    public void onLoadFinished( Loader<List<Receipt>> loader, List<Receipt> data ) {
        adapter.addAll( data );
        adapter.notifyDataSetChanged();
        getSupportLoaderManager().destroyLoader( THE_LOADER );
    }

    @Override
    public void onLoaderReset( Loader<List<Receipt>> loader ) {
        adapter.clear();
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onSaveReceipt( Receipt receipt ) {
        super.onSaveReceipt( receipt );
        adapter.add( receipt );
        adapter.sort();
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onDeleteReceipt( Receipt receipt ) {
        super.onDeleteReceipt( receipt );
        adapter.remove( receipt );
        adapter.sort();
        adapter.notifyDataSetChanged();
    }

    @Override
    public void OnLongClick() {
        // Look for items to delete
        int selectedCount = adapter.getSelectedCount();
        if( selectedCount > 0 ) {
            if( !isActionModeShowing ) {
                actionMode = startActionMode( this );
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
            return Select.from( Receipt.class )
                    .orderBy( "created DESC" )
                    .list();
        }
    }
}
