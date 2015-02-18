package co.yodo.mobile.main;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import co.yodo.mobile.R;
import co.yodo.mobile.adapter.ReceiptsListViewAdapter;
import co.yodo.mobile.data.Receipt;
import co.yodo.mobile.database.ReceiptsDataSource;
import co.yodo.mobile.helper.AppUtils;

public class ReceiptsActivity extends ActionBarActivity {
    /** The context object */
    private Context ac;

    /** Database */
    private ReceiptsDataSource receiptsdb;

    /** GUI Controllers */
    private ListView receiptsListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppUtils.setLanguage( ReceiptsActivity.this );
        setContentView(R.layout.activity_receipts);

        setupGUI();
        updateData();
    }

    @Override
    public void onResume() {
        super.onResume();

        if( receiptsdb != null )
            receiptsdb.open();
    }

    @Override
    public void onPause() {
        super.onPause();

        if( receiptsdb != null )
            receiptsdb.close();
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

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        if( v.getId() == R.id.receiptsList ) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate( R.menu.menu_receipts, menu );
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch( item.getItemId() ) {
            case R.id.delete:
                ReceiptsListViewAdapter adapter = (ReceiptsListViewAdapter) receiptsListView.getAdapter();
                Receipt receipt          = adapter.getItem( info.position );
                List<Receipt> values     = adapter.getValues();

                receiptsdb.deleteReceipt(receipt);
                values.remove( info.position );
                adapter.notifyDataSetChanged();
                return true;
            default:
                return super.onContextItemSelected(item);
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
        receiptsdb = new ReceiptsDataSource( ac );
        receiptsdb.open();
        setSupportActionBar( actionBarToolbar );
        getSupportActionBar().setDisplayHomeAsUpEnabled( true );

        receiptsListView.setOnItemClickListener( new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ReceiptsListViewAdapter adapter = (ReceiptsListViewAdapter) receiptsListView.getAdapter();
                Receipt receipt = adapter.getItem( position );
                receiptDialog( receipt );
            }
        });
    }

    private void updateData() {
        List<Receipt> values = receiptsdb.getAllReceipts();
        ReceiptsListViewAdapter customListAdapter = new ReceiptsListViewAdapter( ac, R.layout.row_list_receipts, values );
        receiptsListView.setAdapter( customListAdapter );
        registerForContextMenu( receiptsListView );
    }

    private void receiptDialog(Receipt params) {
        final Dialog receipt = new Dialog( ReceiptsActivity.this );
        receipt.requestWindowFeature( Window.FEATURE_NO_TITLE );

        LayoutInflater inflater = (LayoutInflater) getSystemService( LAYOUT_INFLATER_SERVICE );
        View layout = inflater.inflate(R.layout.dialog_receipt, new LinearLayout( this ), false);

        TextView descriptionText    = (TextView)  layout.findViewById( R.id.descriptionText );
        TextView authNumberText     = (TextView)  layout.findViewById( R.id.authNumberText );
        TextView createdText        = (TextView)  layout.findViewById( R.id.createdText );
        TextView totalAmountText    = (TextView)  layout.findViewById( R.id.paidText );
        TextView tenderAmountText   = (TextView)  layout.findViewById( R.id.cashTenderText );
        TextView cashBackAmountText = (TextView)  layout.findViewById( R.id.cashBackText );
        ImageView deleteButton      = (ImageView) layout.findViewById( R.id.deleteButton );
        ImageView saveButton        = (ImageView) layout.findViewById( R.id.saveButton );

        descriptionText.setText( params.getDescription() );
        authNumberText.setText( params.getAuthNumber() );
        createdText.setText( AppUtils.UTCtoCurrent( params.getCreated() ) );
        totalAmountText.setText( params.getTotalAmount() );
        tenderAmountText.setText( params.getTenderAmount() );
        cashBackAmountText.setText( params.getCashBackAmount() );

        deleteButton.setVisibility( View.GONE );
        saveButton.setVisibility( View.GONE );

        receipt.setContentView( layout );
        receipt.show();
    }
}
