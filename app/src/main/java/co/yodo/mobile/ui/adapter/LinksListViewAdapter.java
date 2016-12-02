package co.yodo.mobile.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import co.yodo.mobile.R;
import co.yodo.mobile.ui.adapter.model.LinkedAccount;

/**
 * Created by hei on 11/11/16.
 * Adapter for the Linked accounts
 */
public class LinksListViewAdapter extends BaseAdapter {
    /** Data for the list */
    private List<LinkedAccount> mData = new ArrayList<>();
    private LayoutInflater mInflater;

    public LinksListViewAdapter( Context context, List<LinkedAccount> data ) {
        mData  = data;
        this.mInflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem( int position ) {
        return mData.get( position );
    }

    @Override
    public long getItemId( int arg0 ) {
        return arg0;
    }

    @Override
    public View getView( int pos, View convertView, ViewGroup parent ) {
        ViewHolder holder;

        if( convertView == null ) {
            convertView = mInflater.inflate( R.layout.row_linked_accounts, parent, false );

            holder = new ViewHolder();
            holder.accountTitle = (TextView ) convertView.findViewById( R.id.tvLinkedAccount );

            convertView.setTag( holder );
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        LinkedAccount item = mData.get( pos );
        final String hardwareToken = item.getHardwareToken();
        final String title = item.getNickname() != null ? item.getNickname() :
                "..." + hardwareToken.substring( hardwareToken.length() - 5 );
        holder.accountTitle.setText( title );

        return convertView;
    }

    public synchronized void remove( int position ) {
        mData.remove( position );
    }

    private static class ViewHolder {
        TextView accountTitle;
    }
}
