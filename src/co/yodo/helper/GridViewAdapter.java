package co.yodo.helper;

import java.util.ArrayList;
import java.util.List;

import co.yodo.R;
import co.yodo.database.Coupon;
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

public class GridViewAdapter extends ArrayAdapter<Coupon> {
	private Context context;
    private int layoutResourceId;
    private List<Coupon> data = new ArrayList<Coupon>();
 
    public GridViewAdapter(Context context, int layoutResourceId, List<Coupon> data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }
 
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ViewHolder holder = null;
 
        if(row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            holder = new ViewHolder();
            holder.couponTitle = (TextView) row.findViewById(R.id.text_row);
            holder.image = (ImageView) row.findViewById(R.id.image_row);
            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }
 
        Coupon item = data.get(position);
        Bitmap image = BitmapFactory.decodeFile(item.getUrl());
        holder.couponTitle.setText(item.getDescription());
        holder.image.setImageBitmap(image);
        return row;
    }
 
    static class ViewHolder {
        TextView couponTitle;
        ImageView image;
    }
}
