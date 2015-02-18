package co.yodo.mobile.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import co.yodo.mobile.R;
import co.yodo.mobile.data.Coupon;
import co.yodo.mobile.data.Receipt;
import co.yodo.mobile.helper.AppUtils;

public class ReceiptsListViewAdapter extends ArrayAdapter<Receipt> {
	private Context context;
    private int layoutResourceId;
    private List<Receipt> data = new ArrayList<>();

    public ReceiptsListViewAdapter(Context context, int layoutResourceId, List<Receipt> data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }
 
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ViewHolder holder;
 
        if( row == null ) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate( layoutResourceId, parent, false );
            holder = new ViewHolder();
            holder.created     = (TextView)  row.findViewById( R.id.createdView );
            holder.description = (TextView) row.findViewById( R.id.descriptionView );
            row.setTag( holder );
        } else {
            holder = (ViewHolder) row.getTag();
        }

        Receipt item = data.get( position );
        holder.created.setText( AppUtils.UTCtoCurrent( item.getCreated() ) );
        holder.description.setText( item.getDescription() );
        return row;
    }

    public List<Receipt> getValues() {
        return data;
    }
 
    static class ViewHolder {
        TextView created;
        TextView description;
    }
}
