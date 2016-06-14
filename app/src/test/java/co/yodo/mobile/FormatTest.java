package co.yodo.mobile;

import android.test.suitebuilder.annotation.SmallTest;

import org.junit.Test;

import co.yodo.mobile.helper.FormatUtils;

import static org.junit.Assert.*;

/**
 * Created by hei on 10/06/16.
 * Tests the format utils
 */
public class FormatTest {
    /** Original numbers */
    private static final String[] input = {
            "8.3580", "97.558", "340.91", "2542.2",
            "9.2879", "10.743", "112.03", "1214.6",
            "9.6898", "11.522", "516.31", "8246.5"
    };

    /** Truncated numbers */
    private static final String[] output = {
            "8.3", "97.55", "340.910", "2542.2000",
            "9.2", "10.74", "112.030", "1214.6000",
            "9.6", "11.52", "516.310", "8246.5000"
    };

    @Test
    @SmallTest
    public void testGetCurrentDate() {
        final String date = FormatUtils.getCurrentDate();
        assertNotNull( date );
    }

    @Test
    public void testTruncateDecimal() {
        for( int i = 0; i < input.length; i++ ) {
            String actual = FormatUtils.truncateDecimal( input[i], (i % 4) + 1 );
            assertEquals( "Incorrect truncate", output[i], actual );
        }
    }

    @Test
    @SmallTest
    public void testReplaceNull() {
        final String notNull = FormatUtils.replaceNull( null );
        assertNotNull( notNull );
    }
}
