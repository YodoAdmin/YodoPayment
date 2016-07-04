package co.yodo.mobile;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import org.junit.Rule;
import org.junit.runner.RunWith;

import co.yodo.mobile.ui.CouponsActivity;

/**
 * Created by hei on 30/06/16.
 * Tests for the coupons activity
 */
@RunWith( AndroidJUnit4.class )
@LargeTest
public class CouponsActivityTest {

    @Rule
    public ActivityTestRule mActivityRule = new ActivityTestRule<>( CouponsActivity.class );
}
