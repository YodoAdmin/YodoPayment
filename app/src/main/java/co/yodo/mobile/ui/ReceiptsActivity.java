package co.yodo.mobile.ui;

import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import co.yodo.mobile.R;
import co.yodo.mobile.ui.adapter.ReceiptsListViewAdapter;
import co.yodo.mobile.database.model.Receipt;
import co.yodo.mobile.database.ReceiptsDataSource;
import co.yodo.mobile.helper.AppUtils;

public class ReceiptsActivity extends AppCompatActivity implements AdapterView.OnItemClickListener,
        AdapterView.OnItemLongClickListener, ActionMode.Callback {
    /** DEBUG */
    private final static String TAG = ReceiptsActivity.class.getSimpleName();

    /** The context object */
    private Context ac;

    /** Database */
    private ReceiptsDataSource receiptsdb;

    /** GUI Controllers */
    private SearchView searchView;
    private ListView receiptsListView;
    private ReceiptsListViewAdapter customListAdapter;

    /** Action Mode (Action bar) */
    private ActionMode mActionMode;
    private boolean isActionModeShowing = false;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        AppUtils.setLanguage( ReceiptsActivity.this );
        setContentView(R.layout.activity_receipts);

        setupGUI();
        updateData();
    }

    @Override
    public void onResume() {
        super.onResume();

        if( receiptsdb != null ) {
            receiptsdb.open();

            customListAdapter.clear();
            List<Receipt> values = receiptsdb.getAllReceipts();
            customListAdapter.addAll( values );
            customListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if( receiptsdb != null )
            receiptsdb.close();
    }

    @Override
    public void onStart() {
        super.onStart();
        // register to event bus
        EventBus.getDefault().register( this );
    }

    @Override
    public void onStop() {
        super.onStop();
        // unregister from event bus
        EventBus.getDefault().unregister( this );
    }

    @Override
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
                customListAdapter.getFilter().filter( newText );
                return true;
            }
            @Override
            public boolean onQueryTextSubmit( String query ) {
                customListAdapter.getFilter().filter( query );
                searchView.clearFocus();
                return true;
            }
        } );

        return super.onCreateOptionsMenu( menu );
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch( itemId ) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected( item );
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu( menu, v, menuInfo );

        if( v.getId() == R.id.receiptsList ) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate( R.menu.menu_receipts, menu );
        }
    }

    private void setupGUI() {
        // get the context
        ac = ReceiptsActivity.this;
        // Get controllers
        receiptsListView = (ListView) findViewById( R.id.receiptsList );
        // Only used at creation
        Toolbar actionBarToolbar = (Toolbar) findViewById( R.id.actionBar );
        // Bootstrap
        //receiptsdb = new ReceiptsDataSource( ac );
        receiptsdb = ReceiptsDataSource.getInstance( ac );
        receiptsdb.open();
        setSupportActionBar( actionBarToolbar );
        ActionBar temp = getSupportActionBar();
        if( temp != null )
            temp.setDisplayHomeAsUpEnabled( true );
    }

    private void updateData() {
        List<Receipt> values = receiptsdb.getAllReceipts();
        customListAdapter = new ReceiptsListViewAdapter( ac, R.layout.row_list_receipts, values );
        receiptsListView.setAdapter( customListAdapter );
        receiptsListView.setOnItemClickListener( this );
        receiptsListView.setOnItemLongClickListener( this );
    }

    private void receiptDialog( Receipt params ) {
        AppUtils.Logger( TAG, params.toString() );

        final Dialog receipt = new Dialog( ReceiptsActivity.this );
        receipt.requestWindowFeature( Window.FEATURE_NO_TITLE );

        LayoutInflater inflater = (LayoutInflater) getSystemService( LAYOUT_INFLATER_SERVICE );
        View layout = inflater.inflate(R.layout.dialog_receipt, new LinearLayout( this ), false);

        TextView descriptionText    = (TextView)  layout.findViewById( R.id.descriptionText );
        TextView authNumberText     = (TextView)  layout.findViewById( R.id.authNumberText );
        TextView createdText        = (TextView)  layout.findViewById( R.id.createdText );
        TextView currencyText       = (TextView)  layout.findViewById( R.id.currencyText );
        TextView totalAmountText    = (TextView)  layout.findViewById( R.id.paidText );
        TextView tenderAmountText   = (TextView)  layout.findViewById( R.id.cashTenderText );
        TextView cashBackAmountText = (TextView)  layout.findViewById( R.id.cashBackText );
        TextView tvDonorText        = (TextView)  layout.findViewById( R.id.tvDonorText );
        TextView tvReceiverText     = (TextView)  layout.findViewById( R.id.tvReceiverText );
        ImageView deleteButton      = (ImageView) layout.findViewById( R.id.deleteButton );
        ImageView saveButton        = (ImageView) layout.findViewById( R.id.saveButton );
        LinearLayout donorLayout    = (LinearLayout) layout.findViewById( R.id.donorAccountLayout );

        descriptionText.setText( params.getDescription() );
        authNumberText.setText( params.getAuthNumber() );
        currencyText.setText( params.getDCurrency() );
        createdText.setText( AppUtils.UTCtoCurrent( ac, params.getCreated() ) );
        totalAmountText.setText(
                AppUtils.truncateDecimal( params.getTotalAmount() ) + " " +
                AppUtils.replaceNull( params.getTCurrency() ) );
        tenderAmountText.setText( AppUtils.truncateDecimal( params.getTenderAmount() ) );
        cashBackAmountText.setText( AppUtils.truncateDecimal( params.getCashBackAmount() ) );

        final String donor = params.getDonorAccount();
        if( donor != null ) {
            donorLayout.setVisibility( View.VISIBLE );
            tvDonorText.setText( donor );
        }
        tvReceiverText.setText( params.getReceiverAccount() );

        deleteButton.setVisibility( View.GONE );
        saveButton.setVisibility( View.GONE );

        receipt.setContentView( layout );
        receipt.show();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ReceiptsListViewAdapter adapter = (ReceiptsListViewAdapter) receiptsListView.getAdapter();
        Receipt receipt = adapter.getItem( position );

        if( !receipt.isOpened() ) {
            receipt.setOpened( true );
            receiptsdb.updateReceipt( receipt );
        }

        receiptDialog( receipt );
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onItemLongClick( AdapterView<?> parent, View view, int position, long id ) {
        ReceiptsListViewAdapter adapter = (ReceiptsListViewAdapter) receiptsListView.getAdapter();
        Receipt receipt = adapter.getItem( position );

        ReceiptsListViewAdapter.ViewHolder holder = (ReceiptsListViewAdapter.ViewHolder) view.getTag();
        receipt.setChecked( !receipt.isChecked );
        adapter.toggleSelection( position );
        adapter.updateCheckedState( holder, receipt );

        // Look for items to delete
        int selectedCount = adapter.getDeleteCount();
        if( selectedCount > 0 ) {
            if( !isActionModeShowing ) {
                mActionMode = ReceiptsActivity.this.startActionMode( ReceiptsActivity.this );
                isActionModeShowing = true;
            }
        }
        else if( mActionMode != null ) {
            mActionMode.finish();
            isActionModeShowing = false;
        }

        if( mActionMode != null )
            mActionMode.setTitle( String.valueOf( selectedCount ) );

        return true;
    }

    @Override
    public boolean onCreateActionMode( ActionMode mode, Menu menu ) {
        // Inflate a menu resource providing context menu items
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate( R.menu.receipts_multi, menu );
        return true;
    }

    @Override
    public boolean onPrepareActionMode( ActionMode mode, Menu menu ) {
        return false;
    }

    @Override
    public boolean onActionItemClicked( ActionMode mode, MenuItem item ) {
        switch( item.getItemId() ) {
            case R.id.delete:
                final List<Receipt> removed = customListAdapter.removeSelected();
                for( Receipt receipt : removed )
                    receiptsdb.deleteReceipt( receipt );

                // Show a notification which can reverse the delete
                String message = removed.size() + " " + getString( R.string.message_deleted );
                Snackbar.make( receiptsListView, message, Snackbar.LENGTH_LONG )
                        .setAction( R.string.message_undo, new View.OnClickListener() {
                            @Override
                            public void onClick( View v ) {
                                v.setEnabled( false );

                                customListAdapter.addAll( removed );
                                customListAdapter.sort( ReceiptsListViewAdapter.ReceiptsComparator );

                                for( Receipt receipt : removed )
                                    receiptsdb.addReceipt( receipt );
                            }
                        } ).show();

                mode.finish();
                return true;

            default:
                return false;
        }
    }

    @Override
    public void onDestroyActionMode( ActionMode mode ) {
        isActionModeShowing = false;

        ReceiptsListViewAdapter adapter = (ReceiptsListViewAdapter) receiptsListView.getAdapter();
        adapter.clearDeleteList();
        adapter.notifyDataSetChanged();
    }

    @SuppressWarnings("unused") // receives GCM receipts
    @Subscribe( threadMode = ThreadMode.MAIN )
    public void onReceiptEvent( Receipt receipt ) {
        ReceiptsListViewAdapter adapter = (ReceiptsListViewAdapter) receiptsListView.getAdapter();
        //adapter.addReceipt( receipt );
        adapter.add( receipt );
        adapter.sort( ReceiptsListViewAdapter.ReceiptsComparator );
        adapter.notifyDataSetChanged();
    }
}
