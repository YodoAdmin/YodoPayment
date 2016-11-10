package co.yodo.mobile.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import co.yodo.mobile.R;
import co.yodo.mobile.database.CouponsDataSource;
import co.yodo.mobile.database.model.Coupon;
import co.yodo.mobile.helper.GUIUtils;
import co.yodo.mobile.helper.SystemUtils;
import co.yodo.mobile.ui.adapter.CouponsGridViewAdapter;
import co.yodo.mobile.ui.dialog.CouponDialog;

public class CouponsActivity extends AppCompatActivity implements
        AdapterView.OnItemClickListener,
        LoaderManager.LoaderCallbacks<List<Coupon>>{
    /** DEBUG */
    @SuppressWarnings( "unused" )
    private final static String TAG = CouponsActivity.class.getSimpleName();

    /** The context object */
    private Context ac;

    /** Database */
    private CouponsDataSource couponsdb;

    /** GUI Controllers */
    private ProgressBar pbLoading;
    private GridView gvCoupons;
    private CouponsGridViewAdapter cgvAdapter;

    /** Identifier for the loader */
    private static final int THE_LOADER = 0x01;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        GUIUtils.setLanguage( CouponsActivity.this );
        setContentView( R.layout.activity_coupons );

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
                Coupon coupon = cgvAdapter.getItem( info.position );
                List<Coupon> values = cgvAdapter.getValues();

                File file = new File( coupon.getUrl() );
                boolean delete = file.delete();

                if( !delete )
                    SystemUtils.iLogger( TAG, "File not found" );

                couponsdb.deleteCoupon( coupon );
                values.remove( info.position );
                cgvAdapter.notifyDataSetChanged();
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
        pbLoading = (ProgressBar ) findViewById( R.id.pbLoading );
        gvCoupons = (GridView) findViewById( R.id.couponsGrid );

        // Setup the toolbar
        GUIUtils.setActionBar( this, R.string.title_activity_coupons );
    }

    private void updateData() {
        gvCoupons.setOnItemClickListener( this );
        cgvAdapter = new CouponsGridViewAdapter( ac, R.layout.row_grid_coupons, new ArrayList<Coupon>() );
        getSupportLoaderManager().initLoader( THE_LOADER, null, this ).forceLoad();
    }

    @Override
    public void onItemClick( AdapterView<?> parent, View view, int position, long id ) {
        Coupon coupon = cgvAdapter.getItem( position );
        Bitmap image = BitmapFactory.decodeFile( coupon.getUrl() );

        new CouponDialog.Builder( ac )
                .cancelable( true )
                .image( image )
                .build();
    }

    @Override
    public Loader<List<Coupon>> onCreateLoader( int id, Bundle args ) {
        return new LoadTask( this, couponsdb );
    }

    @Override
    public void onLoadFinished( Loader<List<Coupon>> loader, List<Coupon> data ) {
        SystemUtils.iLogger( TAG, Collections.singletonList( data ).toString() );
        cgvAdapter.addAll( data );
        gvCoupons.setAdapter( cgvAdapter );
        registerForContextMenu( gvCoupons );

        pbLoading.setVisibility( View.GONE );
        gvCoupons.setVisibility( View.VISIBLE );

        getSupportLoaderManager().destroyLoader( THE_LOADER );
    }

    @Override
    public void onLoaderReset( Loader<List<Coupon>> loader ) {
        gvCoupons.setAdapter( null );
    }

    /**
     * Loads the receipts from the database
     */
    private static class LoadTask extends AsyncTaskLoader<List<Coupon>> {
        /** Receipts database */
        private CouponsDataSource mCouponsdb;

        /**
         * Sets the receipts from the caller
         * @param context The activity context
         * @param coupondb The coupons database
         */
        LoadTask( Context context, CouponsDataSource coupondb ) {
            super( context );
            this.mCouponsdb = coupondb;
        }

        @Override
        public List<Coupon> loadInBackground() {
            return mCouponsdb.getAllCoupons();
        }
    }
}
