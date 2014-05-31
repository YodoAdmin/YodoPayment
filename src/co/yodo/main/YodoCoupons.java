package co.yodo.main;

import java.io.File;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import co.yodo.R;
import co.yodo.database.Coupon;
import co.yodo.database.CouponsDataSource;
import co.yodo.helper.GridViewAdapter;
import co.yodo.helper.Language;

public class YodoCoupons extends Activity {
	/*!< Database */
	private CouponsDataSource datasource; 
	
	/*!< GUI Controllers */
	private GridView gridview;
	private GridViewAdapter customGridAdapter;
	private List<Coupon> values;
	
	/*!< Coupons size */
	private final static float window_size = 0.7f;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Language.changeLanguage(this);
    	setContentView(R.layout.activity_yodo_coupons);
    	
    	setupGUI();
    	updateData();
    }
	
	@SuppressLint("NewApi")
	private void setupGUI() {
		datasource = new CouponsDataSource(this);
		datasource.open();
		
		gridview = (GridView)findViewById(R.id.gridView);
		
		if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
        	getActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
        gridview.setOnItemClickListener(new OnItemClickListener() {
			@Override
	        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				ListAdapter listAdapter = gridview.getAdapter();
				Coupon item = (Coupon) listAdapter.getItem(position);
				Bitmap image = BitmapFactory.decodeFile(item.getUrl());
				
				Builder dialog = new AlertDialog.Builder(YodoCoupons.this);
				Rect displayRectangle = new Rect();
				Window window = getWindow();
				window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);

				// inflate and adjust layout
				LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				View layout = inflater.inflate(R.layout.dialog_coupon, null);
				
				ImageView couponImage = (ImageView) layout.findViewById(R.id.coupon);
				couponImage.setImageBitmap(image);
				
				layout.setMinimumWidth((int)(displayRectangle.width() * window_size));
				layout.setMinimumHeight((int)(displayRectangle.height() * window_size));
				
				dialog.setView(layout);
				dialog.show();
	        }
		});
	}
	
	private void updateData() {
		values = datasource.getAllCoupons();
		customGridAdapter = new GridViewAdapter(this, R.layout.row_grid, values);
		gridview.setAdapter(customGridAdapter);
		registerForContextMenu(gridview);
		
		setTitle(getString(R.string.coupons_title) + " (" + datasource.getAmount() + ")");
	}
	
	@SuppressLint("NewApi")
	@Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                finish();
            break;
            
            case R.id.action_refresh:
            	updateData();
            break;

            default:
            break;
        }
        return true;
    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    // Inflate the menu items for use in the action bar
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.yodo_coupons, menu);
	    return super.onCreateOptionsMenu(menu);
	}
	
	@Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
          super.onCreateContextMenu(menu, v, menuInfo);
          
          if(v.getId() == R.id.gridView) {
              MenuInflater inflater = getMenuInflater();
              inflater.inflate(R.menu.yodo_menu_coupon, menu);
          }
    }
    
	@Override
    public boolean onContextItemSelected(MenuItem item) {
          AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
          switch(item.getItemId()) {
              case R.id.delete:
            	  ListAdapter listAdapter = gridview.getAdapter();
            	  Coupon coupon = (Coupon) listAdapter.getItem(info.position);
            	  
            	  File file = new File(coupon.getUrl());
            	  file.delete();
            	  
            	  datasource.deleteCoupon(coupon);
            	  values.remove(info.position);
            	  customGridAdapter.notifyDataSetChanged();
            	  
            	  setTitle(getString(R.string.coupons_title) + " (" + datasource.getAmount() + ")");
            	  return true;
              default:
                    return super.onContextItemSelected(item);
          }
    }
}
