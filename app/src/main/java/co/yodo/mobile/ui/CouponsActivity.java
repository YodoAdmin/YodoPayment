package co.yodo.mobile.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.yodo.mobile.R;
import co.yodo.mobile.YodoApplication;
import co.yodo.mobile.model.db.Coupon;
import co.yodo.mobile.ui.adapter.CouponsAdapter;
import co.yodo.mobile.utils.GuiUtils;
import timber.log.Timber;

public class CouponsActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<List<Coupon>>{
    /** The context object */
    @Inject
    Context context;

    /** GUI Controllers */
    @BindView( R.id.pbLoading )
    ProgressBar pbLoading;

    @BindView( R.id.rvCoupons )
    RecyclerView rvCoupons;

    /** Coupons data and adapter */
    private final List<Coupon> coupons = new ArrayList<>();
    private CouponsAdapter adapter;

    /** Identifier for the loader */
    private static final int THE_LOADER = 0x01;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_coupons );

        setupGUI( savedInstanceState );
        updateData();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        new Thread( new Runnable() {
            public void run() {
                // Remove the pendant coupons
                for( Coupon coupon : adapter.getCouponsPendingRemoval() ) {
                    File file = new File( coupon.getUrl() );
                    coupon.delete();

                    // Try to delete the file from the memory
                    if( !file.delete() ) {
                        Timber.e( "Error at deleting the coupon file: " + coupon.getUrl() );
                    }
                }
            }
        } ).start();
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
    protected void setupGUI( Bundle savedInstanceState ) {
        super.setupGUI( savedInstanceState );
        // Injection
        YodoApplication.getComponent().inject( this );
    }

    @Override
    public void updateData() {
        GridLayoutManager glManager = new GridLayoutManager( CouponsActivity.this, 3 );
        rvCoupons.setLayoutManager( glManager );

        adapter = new CouponsAdapter( coupons );
        rvCoupons.setAdapter( adapter );

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
                    Coupon coupon = coupons.get( swipedPosition );
                    File file = new File( coupon.getUrl() );
                    coupon.delete();

                    // Try to delete the file from the memory
                    if( !file.delete() ) {
                        Timber.e( "Error at deleting the coupon file: " + coupon.getUrl() );
                    }

                    adapter.remove( swipedPosition );
                }
            }
        };

        // Attach the listener to delete entries
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper( ithCallback );
        itemTouchHelper.attachToRecyclerView( rvCoupons );

        getSupportLoaderManager().initLoader( THE_LOADER, null, this ).forceLoad();
    }

    @Override
    public Loader<List<Coupon>> onCreateLoader( int id, Bundle args ) {
        return new LoadTask( this );
    }

    @Override
    public void onLoadFinished( Loader<List<Coupon>> loader, List<Coupon> data ) {
        coupons.addAll( data );
        adapter.notifyDataSetChanged();

        pbLoading.setVisibility( View.GONE );
        rvCoupons.setVisibility( View.VISIBLE );

        getSupportLoaderManager().destroyLoader( THE_LOADER );
    }

    @Override
    public void onLoaderReset( Loader<List<Coupon>> loader ) {
    }

    /**
     * Loads the receipts from the database
     */
    private static class LoadTask extends AsyncTaskLoader<List<Coupon>> {
        /**
         * Sets the receipts from the caller
         * @param context The activity context
         */
        LoadTask( Context context) {
            super( context );
        }

        @Override
        public List<Coupon> loadInBackground() {
            return Coupon.listAll( Coupon.class );
        }
    }
}
