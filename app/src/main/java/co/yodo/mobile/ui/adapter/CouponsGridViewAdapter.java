package co.yodo.mobile.ui.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import java.util.ArrayList;
import java.util.List;

import co.yodo.mobile.R;
import co.yodo.mobile.model.db.Coupon;

public class CouponsGridViewAdapter extends ArrayAdapter<Coupon> {
    /** Data */
    private int layoutResourceId;
    private List<Coupon> data = new ArrayList<>();
    private LayoutInflater mInflater;

    /** Loader for the images */
    private ImageLoader imageLoader;
    private final DisplayImageOptions options = new DisplayImageOptions.Builder()
            .showImageOnLoading( R.mipmap.loading_image )
            .showImageOnFail( R.drawable.ic_no_image )
            .bitmapConfig( Bitmap.Config.RGB_565 )
            .imageScaleType( ImageScaleType.EXACTLY )
            .cacheInMemory( true )
            .cacheOnDisk( true )
            .build();
 
    public CouponsGridViewAdapter( Context context, int layoutResourceId, List<Coupon> data ) {
        super( context, layoutResourceId, data );
        this.mInflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        this.layoutResourceId = layoutResourceId;
        this.data = data;

        // Config loader
        this.imageLoader = ImageLoader.getInstance();
        this.imageLoader.init( ImageLoaderConfiguration.createDefault( context ) );
    }
 
    @NonNull
    @Override
    public View getView( int position, View convertView, @NonNull ViewGroup parent ) {
        ViewHolder holder;
 
        if( convertView == null ) {
            convertView = mInflater.inflate( layoutResourceId, parent, false );

            holder = new ViewHolder();
            holder.couponTitle = (TextView) convertView.findViewById( R.id.text_row );
            holder.image = (ImageView) convertView.findViewById( R.id.image_row );

            convertView.setTag( holder );
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
 
        Coupon item = data.get( position );
        holder.couponTitle.setText( item.getDescription() );
        imageLoader.displayImage( "file://" + item.getUrl(), holder.image, options );
        return convertView;
    }

    public List<Coupon> getValues() {
        return data;
    }
 
    private static class ViewHolder {
        TextView couponTitle;
        ImageView image;
    }
}
