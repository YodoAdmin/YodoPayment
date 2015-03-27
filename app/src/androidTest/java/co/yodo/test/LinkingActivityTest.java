package co.yodo.test;

import android.test.ActivityInstrumentationTestCase2;

import co.yodo.mobile.data.ServerResponse;
import co.yodo.mobile.main.LinkingActivity;
import co.yodo.mobile.net.YodoRequest;

public class LinkingActivityTest extends ActivityInstrumentationTestCase2<LinkingActivity> implements YodoRequest.RESTListener {

    public LinkingActivityTest() {
        super(LinkingActivity.class);
    }

    @Override
    public void onResponse(YodoRequest.RequestType type, ServerResponse response) {

    }
}
