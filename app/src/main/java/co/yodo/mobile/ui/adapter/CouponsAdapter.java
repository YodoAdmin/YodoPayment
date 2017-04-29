package co.yodo.mobile.ui.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.yodo.mobile.R;
import co.yodo.mobile.YodoApplication;
import co.yodo.mobile.model.db.Coupon;
import co.yodo.mobile.ui.dialog.CouponDialog;

public class CouponsAdapter extends RecyclerView.Adapter<CouponsAdapter.CouponViewHolder> {
    /** The context object */
    @Inject
    Context context;

    /** Data */
    private List<Coupon> coupons = new ArrayList<>();
    private List<Coupon> couponsPendingRemoval;
 
    public CouponsAdapter( List<Coupon> coupons ) {
        // Get data
        this.coupons = coupons;
        this.couponsPendingRemoval = new ArrayList<>();

        // Injection
        YodoApplication.getComponent().inject( this );
    }

    @Override
    public CouponViewHolder onCreateViewHolder( ViewGroup parent, int viewType ) {
        View v = LayoutInflater.from( parent.getContext() ).inflate( R.layout.row_coupon, parent, false );
        return new CouponsAdapter.CouponViewHolder( v );
    }

    @Override
    public void onBindViewHolder( final CouponsAdapter.CouponViewHolder holder, final int position ) {
        final Coupon coupon = coupons.get( position );

        if( couponsPendingRemoval.contains( coupon ) ) {
            // Show the "undoLayout" state of the row
            holder.tvUndo.setVisibility( View.VISIBLE );
            holder.cvCoupon.setVisibility( View.GONE );

            holder.tvUndo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // user wants to undoLayout the removal, let's cancel the pending task
                    couponsPendingRemoval.remove( coupon );

                    // this will rebind the row in "normal" state
                    notifyItemChanged( coupons.indexOf( coupon ) );
                }
            });
        } else {
            // Show the "entryLogLayout" state of the row
            holder.tvUndo.setVisibility( View.GONE );
            holder.cvCoupon.setVisibility( View.VISIBLE );

            holder.tvCoupon.setText( coupon.getDescription() );
            Picasso.with( context )
                    .load( "file://" + coupon.getUrl() )
                    .placeholder( R.drawable.ic_loading )
                    .error( R.drawable.ic_no_image )
                    .fit()
                    .into( holder.ivCoupon );

            holder.cvCoupon.setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick( View v ) {
                    Bitmap image = BitmapFactory.decodeFile( coupon.getUrl() );
                    new CouponDialog.Builder( holder.cvCoupon.getContext() )
                            .cancelable( true )
                            .image( image )
                            .build();
                }
            } );
        }
    }

    @Override
    public int getItemCount() {
        return coupons.size();
    }

    public void onAttachedToRecyclerView( RecyclerView recyclerView ) {
        super.onAttachedToRecyclerView( recyclerView );
    }

    /**
     * Add the item to a pending removal
     * @param position The position to remove it
     */
    public void pendingRemoval( int position ) {
        // this will redraw row in "undoLayout" state
        Coupon item = coupons.get( position );
        if( !couponsPendingRemoval.contains( item ) ) {
            couponsPendingRemoval.add( item );
            notifyItemChanged( position );
        }
    }

    /**
     * Removes an element from the view
     * @param position The position
     */
    public void remove( int position ) {
        Coupon item = coupons.get( position );
        if( couponsPendingRemoval.contains( item ) ) {
            couponsPendingRemoval.remove( item );
        }

        if( coupons.contains( item ) ) {
            coupons.remove( position );
            notifyItemRemoved( position );
        }
    }

    /**
     * Verify if it is pending for removal
     * @param position The position in the original list
     * @return if it is in the removal list
     */
    public boolean isPendingRemoval( int position ) {
        Coupon item = coupons.get( position );
        return couponsPendingRemoval.contains( item );
    }

    /**
     * Get the accounts waiting for removal
     * @return The list with the accounts that will be removed
     */
    public List<Coupon> getCouponsPendingRemoval() {
        return this.couponsPendingRemoval;
    }

    static class CouponViewHolder extends RecyclerView.ViewHolder {
        /** GUI Controllers */
        @BindView(R.id.cvCoupon )
        CardView cvCoupon;

        @BindView( R.id.ivCoupon )
        ImageView ivCoupon;

        @BindView( R.id.tvCoupon )
        TextView tvCoupon;

        @BindView( R.id.tvUndo )
        TextView tvUndo;

        CouponViewHolder( View itemView ) {
            super( itemView );

            // Injection
            ButterKnife.bind( this, itemView );
        }
    }
}
