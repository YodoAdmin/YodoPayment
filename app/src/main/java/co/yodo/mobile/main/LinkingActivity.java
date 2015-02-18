package co.yodo.mobile.main;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import co.yodo.mobile.R;
import co.yodo.mobile.data.ServerResponse;
import co.yodo.mobile.helper.AppUtils;
import co.yodo.mobile.net.YodoRequest;

public class LinkingActivity extends ActionBarActivity implements YodoRequest.RESTListener {
    /** The context object */
    private Context ac;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppUtils.setLanguage( LinkingActivity.this );
        setContentView(R.layout.activity_linking);

        setupGUI();
        updateData();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch( itemId ) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupGUI() {
        // get the context
        ac = LinkingActivity.this;

        // Only used at creation
        Toolbar actionBarToolbar = (Toolbar) findViewById( R.id.actionBar );

        setSupportActionBar( actionBarToolbar );
        getSupportActionBar().setDisplayHomeAsUpEnabled( true );
    }

    private void updateData() {
    }

    @Override
    public void onResponse(YodoRequest.RequestType type, ServerResponse response) {

    }
}
