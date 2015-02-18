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

public class CouponsGridViewAdapter extends ArrayAdapter<Coupon> {
	private Context context;
    private int layoutResourceId;
    private List<Coupon> data = new ArrayList<>();
 
    public CouponsGridViewAdapter(Context context, int layoutResourceId, List<Coupon> data) {
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
            row = inflater.inflate(layoutResourceId, parent, false);
            holder = new ViewHolder();
            holder.couponTitle = (TextView) row.findViewById( R.id.text_row );
            holder.image       = (ImageView) row.findViewById( R.id.image_row );
            row.setTag( holder );
        } else {
            holder = (ViewHolder) row.getTag();
        }
 
        Coupon item = data.get(position);
        Bitmap image = BitmapFactory.decodeFile(item.getUrl());
        holder.couponTitle.setText(item.getDescription());
        holder.image.setImageBitmap(image);
        return row;
    }

    public List<Coupon> getValues() {
        return data;
    }
 
    static class ViewHolder {
        TextView couponTitle;
        ImageView image;
    }
}
