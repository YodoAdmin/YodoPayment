package co.yodo.helper;

import java.lang.ref.WeakReference;

import co.yodo.R;
import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

public class YodoHandler extends Handler {
	private final WeakReference<Activity> wMain;
	
	public YodoHandler(Activity main) {
        super();
        this.wMain = new WeakReference<Activity>(main);
    }

    @Override
    public void handleMessage(Message msg) {
    	 super.handleMessage(msg);
         Activity main = wMain.get();
         
         // message arrived after activity death
         if(main == null)
        	 return;
         
         if(msg.what == YodoGlobals.SUCCESS) {
             ToastMaster.makeText(main, R.string.change_successfull, Toast.LENGTH_LONG).show();
         }
         else if(msg.what == YodoGlobals.NO_INTERNET) {
             ToastMaster.makeText(main, R.string.no_internet, Toast.LENGTH_LONG).show();
         }
         else if(msg.what == YodoGlobals.GENERAL_ERROR) {
             ToastMaster.makeText(main, R.string.error, Toast.LENGTH_LONG).show();
         }
         else if(msg.what == YodoGlobals.UNKOWN_ERROR) {
				String response = msg.getData().getString("message");
				ToastMaster.makeText(main, response, Toast.LENGTH_LONG).show();
         } 
    }
}
