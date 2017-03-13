package co.yodo.mobile.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
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
import java.util.Comparator;
import java.util.List;

import co.yodo.mobile.R;
import co.yodo.mobile.model.db.Receipt;
import co.yodo.mobile.helper.FormatUtils;

public class ReceiptsAdapter extends RecyclerView.Adapter<ReceiptsAdapter.ReceiptViewHolder> implements Filterable {
    /** Application Context */
	private Context context;
    private List<Receipt> originalData = new ArrayList<>();
    private List<Receipt> filteredData = new ArrayList<>();
    private SparseBooleanArray mSelectedItemsIds;

    /** Animations for multiple selection */
    private Animation anim_in;
    private Animation anim_out;

    /** Declare the color generator and drawable builder */
    private ColorGenerator mColorGenerator = ColorGenerator.MATERIAL;
    private TextDrawable.IBuilder mDrawableBuilder;

    public ReceiptsAdapter( Context context, List<Receipt> data ) {
        mDrawableBuilder = TextDrawable.builder()
                .beginConfig()
                    .bold()
                    .withBorder( 4 )
                .endConfig().rect();
        mSelectedItemsIds = new SparseBooleanArray();

        anim_in  = AnimationUtils.loadAnimation( context, R.anim.to_middle );
        anim_out = AnimationUtils.loadAnimation( context, R.anim.from_middle );

        this.context = context;
        this.originalData = data;
        this.filteredData = data ;
    }
 
    /*@NonNull
    @Override
    public View getView( final int position, View convertView, @NonNull ViewGroup parent ) {
        View row = convertView;
        final ViewHolder holder;
 
        if( row == null ) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate( layoutResourceId, parent, false );
            holder = new ViewHolder( row );

            holder.created     = (TextView) row.findViewById( R.id.createdView );
            holder.description = (TextView) row.findViewById( R.id.descriptionView );
            holder.total       = (TextView) row.findViewById( R.id.totalView );

            row.setTag( holder );
        } else {
            holder = (ViewHolder) row.getTag();
        }

        // Fills the holder with the item data
        Receipt item = filteredData.get( position );

        final String total = String.format( "%s %s %s",
                context.getString( R.string.text_receipt_total ),
                FormatUtils.truncateDecimal( item.getTotalAmount() ),
                FormatUtils.replaceNull( item.getTCurrency() )
        );

        holder.created.setText( FormatUtils.UTCtoCurrent( context, item.getCreated() ) );
        holder.description.setText( item.getDescription() );
        holder.total.setText( total );

        // If it is opened, change the text style
        if( item.isOpened() ) {
            holder.description.setTypeface( Typeface.DEFAULT );
            holder.created.setTypeface( Typeface.DEFAULT );
        } else {
            holder.description.setTypeface( Typeface.DEFAULT_BOLD );
            holder.created.setTypeface( Typeface.DEFAULT_BOLD );
        }

        // If it is selected choose a check image, else the default
        if( item.isChecked ) {
            holder.descIcon.setImageDrawable( mDrawableBuilder.build( " ", 0xff616161 ) );
            holder.view.setBackgroundColor( ContextCompat.getColor( context, R.color.colorGreySoft ) );
            holder.checkIcon.setVisibility( View.VISIBLE );
        }
        else {
            TextDrawable drawable = mDrawableBuilder.build(
                    String.valueOf( item.getDescription().charAt( 0 ) ),
                    mColorGenerator.getColor( item.getDescription() )
            );
            holder.descIcon.setImageDrawable( drawable );
            holder.view.setBackgroundColor( Color.TRANSPARENT );
            holder.checkIcon.setVisibility( View.GONE );
        }

        return row;
    }*/

    /**
     * Gets an item by its position
     * @param position The position of the item
     * @return The item in the filtered list
     */
    /*public Receipt getItem( int position ) {
        return filteredData.get( position );
    }*/

    @Override
    public ReceiptViewHolder onCreateViewHolder( ViewGroup parent, int viewType ) {
        View v = LayoutInflater.from(parent.getContext()).inflate( R.layout.row_receipt, parent, false );
        return new ReceiptViewHolder( v );
    }

    @Override
    public void onBindViewHolder( ReceiptViewHolder holder, int position ) {
        // Fills the holder with the item data
        Receipt item = filteredData.get( position );

        final String total = String.format( "%s %s %s",
                context.getString( R.string.text_receipt_total ),
                FormatUtils.truncateDecimal( item.getTotalAmount() ),
                FormatUtils.replaceNull( item.getTCurrency() )
        );

        holder.created.setText( FormatUtils.UTCtoCurrent( context, item.getCreated() ) );
        holder.description.setText( item.getDescription() );
        holder.total.setText( total );

        // If it is opened, change the text style
        if( item.isOpened() ) {
            holder.description.setTypeface( Typeface.DEFAULT );
            holder.created.setTypeface( Typeface.DEFAULT );
        } else {
            holder.description.setTypeface( Typeface.DEFAULT_BOLD );
            holder.created.setTypeface( Typeface.DEFAULT_BOLD );
        }

        // If it is selected choose a check image, else the default
        if( item.isChecked ) {
            holder.descIcon.setImageDrawable( mDrawableBuilder.build( " ", 0xff616161 ) );
            //holder.view.setBackgroundColor( ContextCompat.getColor( context, R.color.colorGreySoft ) );
            holder.checkIcon.setVisibility( View.VISIBLE );
        }
        else {
            TextDrawable drawable = mDrawableBuilder.build(
                    String.valueOf( item.getDescription().charAt( 0 ) ),
                    mColorGenerator.getColor( item.getDescription() )
            );
            holder.descIcon.setImageDrawable( drawable );
            //holder.view.setBackgroundColor( Color.TRANSPARENT );
            holder.checkIcon.setVisibility( View.GONE );
        }

    }

    @Override
    public int getItemCount() {
        return filteredData.size();
    }

    public long getItemId( int position) {
        return position;
    }

    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    /**
     * Starts an animation to update the checked state of an item
     * @param holder The The holder for the item
     * @param item The receipt item
     */
    /*public void updateCheckedState( final ViewHolder holder, final Receipt item ) {
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
            holder.descIcon.clearAnimation();
            holder.descIcon.setAnimation( anim_out );
            holder.descIcon.startAnimation( anim_out );
        }
        else {
            holder.descIcon.clearAnimation();
            holder.descIcon.setAnimation( anim_in );
            holder.descIcon.startAnimation( anim_in );
        }

        anim_in.setAnimationListener( animListener );
        anim_out.setAnimationListener( animListener );
    }*/

    /**
     * removes all the selected items
     * @return The removed elements
     */
    /*public List<Receipt> removeSelected() {
        List<Receipt> removed = new ArrayList<>( mSelectedItemsIds.size() );
        for( int i = (mSelectedItemsIds.size() - 1); i >= 0; i-- ) {
            Receipt item = getItem( mSelectedItemsIds.keyAt( i ) );
            if( item != null )
                item.setChecked( false );
            // Remove selected items following the ids
            removed.add( item );
            //remove( item );
        }
        mSelectedItemsIds.clear();

        return removed;
    }

    public Receipt getSelected() {
        return getItem( mSelectedItemsIds.keyAt( 0 ) );
    }*/

    /*public void addReceipt( Receipt receipt ) {
        originalData.add( receipt );
        filteredData.add( receipt );
        add( receipt );
    }*/

    /**
     * Change the state of an item (selected or not)
     * @param position The position of the item
     */
    public void toggleSelection( int position ) {
        selectView( position, !mSelectedItemsIds.get( position ) );
    }

    /**
     * Add the item to the list of deleted items or remove it
     * @param position The position of the item
     * @param value True for delete, false for remove from the list
     */
    private void selectView( int position, boolean value ) {
        if( value )
            mSelectedItemsIds.put( position, true );
        else
            mSelectedItemsIds.delete( position );
    }

    /**
     * Get the deleted amount of items
     * @return the number of deleted items
     */
    public int getSelectedCount() {
        return mSelectedItemsIds.size();
    }

    /**
     * Clears the items selected to delete
     * by removing them from the list of mSelectedItemsIds
     */
    /*public void clearDeleteList() {
        for( int i = (mSelectedItemsIds.size() - 1); i >= 0; i-- ) {
            Receipt receipt = getItem( mSelectedItemsIds.keyAt( i ) );
            if( receipt != null )
                receipt.setChecked( !receipt.isChecked );
        }
        mSelectedItemsIds.clear();
    }*/

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
                if( item.getDescription().toLowerCase().contains( filterString ) )
                    filterList.add( item );
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

    public static Comparator<Receipt> ReceiptsComparator = new Comparator<Receipt>() {
        @Override
        public int compare( Receipt o1, Receipt o2 ) {
            return o2.getCreated().compareTo( o1.getCreated() );
        }
    };

    /** Holder for the receipts */
    static class ReceiptViewHolder extends RecyclerView.ViewHolder {
        /** GUI Controllers */
        /*CardView entryLayout;
        ImageView officeImageView;
        TextView nameTextView;
        TextView documentIdTextView;
        ImageView signatureImageView;*/
        private View view;
        private TextView created;
        private TextView description;
        private TextView total;
        private ImageView descIcon;
        private ImageView checkIcon;

        ReceiptViewHolder( View itemView) {
            super( itemView );

            /*entryLayout = (CardView) itemView.findViewById(R.id.layout_entry);
            officeImageView = (ImageView) itemView.findViewById(R.id.image_branch_office);
            nameTextView = (TextView) itemView.findViewById(R.id.text_full_name);
            documentIdTextView = (TextView) itemView.findViewById(R.id.entry_document_id);
            signatureImageView = (ImageView) itemView.findViewById(R.id.image_signature);*/
            created = (TextView) itemView.findViewById( R.id.createdView );
            description = (TextView) itemView.findViewById( R.id.descriptionView );
            total = (TextView) itemView.findViewById( R.id.totalView );
            descIcon  = (ImageView) itemView.findViewById( R.id.desc_icon );
            checkIcon = (ImageView) itemView.findViewById( R.id.check_icon );
        }
    }

}
