package co.yodo.mobile.ui.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.yodo.mobile.R;
import co.yodo.mobile.helper.FormatUtils;
import co.yodo.mobile.model.db.Receipt;
import co.yodo.mobile.ui.dialog.ReceiptDialog;
import timber.log.Timber;

public class ReceiptsAdapter extends RecyclerView.Adapter<ReceiptsAdapter.ReceiptViewHolder> implements Filterable {
    /** Application Context */
    private List<Receipt> originalData;
    private List<Receipt> filteredData;
    private List<Receipt> selectedData;
    //private SparseBooleanArray selectedItemsIds;

    /** Animations for multiple selection */
    private Animation anim_in;
    private Animation anim_out;

    /** Long click listener */
    private OnLongClickListener listener;

    /** Compare receipts */
    private static Comparator<Receipt> ReceiptsComparator = new Comparator<Receipt>() {
        @Override
        public int compare( Receipt o1, Receipt o2 ) {
            return o2.getCreated().compareTo( o1.getCreated() );
        }
    };

    public ReceiptsAdapter( Context context, OnLongClickListener listener ) {
        //selectedItemsIds = new SparseBooleanArray();

        anim_in  = AnimationUtils.loadAnimation( context, R.anim.to_middle );
        anim_out = AnimationUtils.loadAnimation( context, R.anim.from_middle );

        // Setup data
        this.originalData = new ArrayList<>();
        this.filteredData = new ArrayList<>();
        this.selectedData = new ArrayList<>();
        this.listener = listener;
    }

    @Override
    public ReceiptViewHolder onCreateViewHolder( ViewGroup parent, int viewType ) {
        View v = LayoutInflater.from(parent.getContext()).inflate( R.layout.row_receipt, parent, false );
        return new ReceiptViewHolder( v );
    }

    @Override
    public void onBindViewHolder( final ReceiptViewHolder holder, int position ) {
        // Fills the holder with the item data
        final Receipt receipt = filteredData.get( position );

        holder.cvReceipt.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                if( !receipt.isOpened() ) {
                    receipt.setOpened( true );
                    receipt.save();
                }

                Context context = v.getContext();
                buildReceiptDialog( context, receipt );
                notifyItemChanged( holder.getAdapterPosition() );
            }
        } );

        holder.bind( receipt );
        holder.itemView.setOnLongClickListener( new View.OnLongClickListener() {
            @Override
            public boolean onLongClick( View v ) {
                receipt.setChecked( !receipt.isChecked );
                toggleSelection( receipt );
                updateCheckedState( holder, receipt );
                listener.OnLongClick();
                return true;
            }
        } );
    }

    @Override
    public int getItemCount() {
        return filteredData.size();
    }

    public void onAttachedToRecyclerView( RecyclerView recyclerView ) {
        super.onAttachedToRecyclerView( recyclerView );
    }

    /**
     * Add a receipt to both lists
     * @param receipt The receipt
     */
    public void add( Receipt receipt ) {
        originalData.add( receipt );
        filteredData.add( receipt );
    }

    /**
     * Add a list of receipts to both lists
     * @param receipts The list of receipts
     */
    public void addAll( List<Receipt> receipts ) {
        originalData.addAll( receipts );
        filteredData.addAll( receipts );
    }

    /**
     * Removes a receipt from both lists
     * @param receipt The receipt
     */
    public void remove( Receipt receipt ) {
        originalData.remove( receipt );
        filteredData.remove( receipt );
    }

    /**
     * Removes a list of receipts from both lists
     * @param receipts The list of receipts
     */
    public void removeAll( List<Receipt> receipts ) {
        originalData.removeAll( receipts );
        filteredData.removeAll( receipts );
    }

    /**
     * Sorts all the data filtered/original
     */
    public void sort() {
        Collections.sort( originalData, ReceiptsAdapter.ReceiptsComparator );
        Collections.sort( filteredData, ReceiptsAdapter.ReceiptsComparator );
    }

    /**
     * Clears all the data
     */
    public void clear() {
        originalData.clear();
        filteredData.clear();
    }

    /**
     * Starts an animation to update the checked state of an item
     * @param holder The The holder for the item
     * @param item The receipt item
     */
    private void updateCheckedState( final ReceiptViewHolder holder, final Receipt item ) {
        Animation.AnimationListener animListener = new Animation.AnimationListener() {
            @Override
            public void onAnimationStart( Animation animation ) {
            }

            @Override
            public void onAnimationEnd( Animation animation ) {
                notifyDataSetChanged();
            }

            @Override
            public void onAnimationRepeat( Animation animation ) {
            }
        };

        if( item.isChecked ) {
            holder.ivIconDescription.clearAnimation();
            holder.ivIconDescription.setAnimation( anim_out );
            holder.ivIconDescription.startAnimation( anim_out );
        }
        else {
            holder.ivIconDescription.clearAnimation();
            holder.ivIconDescription.setAnimation( anim_in );
            holder.ivIconDescription.startAnimation( anim_in );
        }

        anim_in.setAnimationListener( animListener );
        anim_out.setAnimationListener( animListener );
    }

    /**
     * removes all the selected items
     * @return The removed elements
     */
    public List<Receipt> removeSelected() {
        for( Receipt receipt : selectedData ) {
            receipt.setChecked( false );
            receipt.delete();
            remove( receipt );
        }

        List<Receipt> copy = new ArrayList<>( selectedData );
        selectedData.clear();
        return copy;
    }

    /**
     * Change the state of an item (selected or not)
     * @param receipt The item selected
     */
    private void toggleSelection( Receipt receipt ) {
        if( !selectedData.contains( receipt ) ) {
            selectedData.add( receipt );
        } else {
            selectedData.remove( receipt );
        }
    }

    /**
     * Get the deleted amount of items
     * @return the number of deleted items
     */
    public int getSelectedCount() {
        return selectedData.size();
    }

    /** Gets the selected item */
    public Receipt getSelected() {
        return ( selectedData.size() == 1 ) ? selectedData.get( 0 ) : null;
    }

    /**
     * Clears the items selected to delete
     * by removing them from the list of selectedItemsIds
     */
    public void clearSelected() {
        for( Receipt receipt : selectedData ) {
            receipt.setChecked( false );
            receipt.save();
        }
        selectedData.clear();
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    private Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering( CharSequence constraint ) {
            String filterString = constraint.toString().toLowerCase();
            FilterResults results = new FilterResults();
            ArrayList<Receipt> filterList = new ArrayList<>();

            for( int i = 0; i < originalData.size(); i++ ) {
                Receipt item = originalData.get( i );
                if( item.getDescription().toLowerCase().contains( filterString ) ) {
                    filterList.add( item );
                }
            }

            results.values = filterList;
            results.count = filterList.size();
            return results;
        }

        @Override
        protected void publishResults( CharSequence constraint, FilterResults results ) {
            filteredData = FormatUtils.castList( results.values, Receipt.class );
            notifyDataSetChanged();
        }
    };

    /**
     * Builds the dialog for the receipt
     * @param receipt The receipt data
     */
    private void buildReceiptDialog( Context context, Receipt receipt ) {
        // The receipt dialog formats the values
        new ReceiptDialog.Builder( context )
                .cancelable( true )
                .description( receipt.getDescription() )
                .created( receipt.getCreated() )
                .total( receipt.getTotalAmount(), receipt.getTCurrency() )
                .authnumber( receipt.getAuthNumber() )
                .donor( receipt.getDonorAccount() )
                .recipient( receipt.getRecipientAccount() )
                .tender( receipt.getTenderAmount(), receipt.getDCurrency())
                .cashback( receipt.getCashBackAmount(), receipt.getTCurrency() )
                .build();
    }

    /** Holder for the receipts */
    static class ReceiptViewHolder extends RecyclerView.ViewHolder {
        /** View context */
        private Context context;

        /** GUI Controllers */
        @BindView( R.id.cvReceipt )
        CardView cvReceipt;

        @BindView( R.id.tvCreated )
        TextView tvCreated;

        @BindView( R.id.tvDescription )
        TextView tvDescription;

        @BindView( R.id.tvTotal )
        TextView tvTotal;

        @BindView( R.id.ivIconDescription )
        ImageView ivIconDescription;

        @BindView( R.id.ivIconSelection)
        ImageView ivIconSelection;

        /** Declare the color generator and drawable builder */
        private static ColorGenerator colorGenerator = ColorGenerator.MATERIAL;
        private static TextDrawable.IBuilder drawableBuilder = TextDrawable.builder()
                .beginConfig()
                    .bold()
                    .withBorder( 4 )
                .endConfig().rect();

        ReceiptViewHolder( View itemView ) {
            super( itemView );

            // Injection
            ButterKnife.bind( this, itemView );

            // Get context
            context = itemView.getContext();
        }

        void bind( Receipt receipt ) {
            final String total = String.format( "%s %s %s",
                    context.getString( R.string.text_receipt_total ),
                    FormatUtils.truncateDecimal( receipt.getTotalAmount() ),
                    FormatUtils.replaceNull( receipt.getTCurrency() )
            );

            tvCreated.setText( FormatUtils.UTCtoCurrent( context, receipt.getCreated() ) );
            tvDescription.setText( receipt.getDescription() );
            tvTotal.setText( total );

            // If it is opened, change the text style
            if( receipt.isOpened() ) {
                tvDescription.setTypeface( Typeface.DEFAULT );
                tvCreated.setTypeface( Typeface.DEFAULT );
            } else {
                tvDescription.setTypeface( Typeface.DEFAULT_BOLD );
                tvCreated.setTypeface( Typeface.DEFAULT_BOLD );
            }

            // If it is selected choose a check image, else the default
            if( receipt.isChecked ) {
                ivIconDescription.setImageDrawable( drawableBuilder.build( " ", 0xff616161 ) );
                ivIconSelection.setVisibility( View.VISIBLE );
            }
            else {
                TextDrawable drawable = drawableBuilder.build(
                        String.valueOf( receipt.getDescription().charAt( 0 ) ),
                        colorGenerator.getColor( receipt.getDescription() )
                );
                ivIconDescription.setImageDrawable( drawable );
                ivIconSelection.setVisibility( View.GONE );
            }
        }
    }

    public interface OnLongClickListener {
        void OnLongClick();
    }
}
