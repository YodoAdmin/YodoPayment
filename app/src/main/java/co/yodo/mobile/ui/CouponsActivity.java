package co.yodo.mobile.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import java.io.File;
import java.util.List;

import co.yodo.mobile.R;
import co.yodo.mobile.helper.GUIUtils;
import co.yodo.mobile.helper.SystemUtils;
import co.yodo.mobile.ui.adapter.CouponsGridViewAdapter;
import co.yodo.mobile.database.model.Coupon;
import co.yodo.mobile.database.CouponsDataSource;

public class CouponsActivity extends AppCompatActivity {
    /** DEBUG */
    @SuppressWarnings( "unused" )
    private final static String TAG = CouponsActivity.class.getSimpleName();

    /** The context object */
    private Context ac;

    /** Database */
    private CouponsDataSource couponsdb;

    /** GUI Controllers */
    private GridView couponsGridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GUIUtils.setLanguage( CouponsActivity.this );
        setContentView(R.layout.activity_coupons);

        setupGUI();
        updateData();
    }

    @Override
    public void onResume() {
        super.onResume();

        if( couponsdb != null )
            couponsdb.open();
    }

    @Override
    public void onPause() {
        super.onPause();

        if( couponsdb != null )
            couponsdb.close();
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

        if( v.getId() == R.id.couponsGrid ) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate( R.menu.menu_coupons, menu );
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch( item.getItemId() ) {
            case R.id.delete:
                CouponsGridViewAdapter adapter = (CouponsGridViewAdapter) couponsGridView.getAdapter();
                Coupon coupon           = adapter.getItem( info.position );
                List<Coupon> values     = adapter.getValues();

                File file = new File( coupon.getUrl() );
                boolean delete = file.delete();

                if( !delete )
                    SystemUtils.Logger( TAG, "File not found" );

                couponsdb.deleteCoupon( coupon );
                values.remove( info.position );
                adapter.notifyDataSetChanged();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void setupGUI() {
        // get the context
        ac = CouponsActivity.this;

        // Bootstrap
        couponsdb = CouponsDataSource.getInstance( ac );
        couponsdb.open();

        // GUI Controllers
        couponsGridView = (GridView) findViewById( R.id.couponsGrid );

        // Only used at creation
        Toolbar toolbar = (Toolbar) findViewById( R.id.actionBar );

        // Setup the toolbar
        setSupportActionBar( toolbar );
        ActionBar actionbar = getSupportActionBar();
        if( actionbar != null )
            actionbar.setDisplayHomeAsUpEnabled( true );
    }

    private void updateData() {
        List<Coupon> values = couponsdb.getAllCoupons();
        CouponsGridViewAdapter customGridAdapter = new CouponsGridViewAdapter( ac, R.layout.row_grid_coupons, values );
        couponsGridView.setAdapter( customGridAdapter );
        registerForContextMenu( couponsGridView );
    }
}
