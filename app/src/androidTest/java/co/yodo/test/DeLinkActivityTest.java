package co.yodo.test;

import android.test.ActivityInstrumentationTestCase2;

import java.util.concurrent.Semaphore;

import co.yodo.mobile.network.model.ServerResponse;
import co.yodo.mobile.helper.AppUtils;
import co.yodo.mobile.ui.DeLinkActivity;
import co.yodo.mobile.network.YodoRequest;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class DeLinkActivityTest extends ActivityInstrumentationTestCase2<DeLinkActivity> implements YodoRequest.RESTListener {
    /** Authentication Number */
    private String hardwareToken;

    /** User PIP */
    private final static String userPIP = "aaaa";

    /** Manager for the server requests */
    private YodoRequest mRequestManager;

    /** Server Response */
    private ServerResponse response;

    /** Semaphore */
    private Semaphore semaphore;

    public DeLinkActivityTest() {
        super( DeLinkActivity.class );
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        /* The activity object */
        DeLinkActivity activity = getActivity();
        hardwareToken = AppUtils.getHardwareToken( activity );
        semaphore     = new Semaphore( 0 );

        mRequestManager = YodoRequest.getInstance( activity );
        mRequestManager.setListener( this );
    }

    /**
     * Just in case the user is not registered
     * @throws Exception
     */
    private void userRegistration() throws Exception {
        mRequestManager.requestRegistration( hardwareToken, userPIP );
        semaphore.acquire();

        assertNotNull( response );
        response = null;
    }

    public void test() throws Exception {
        assertNotNull( hardwareToken );
        assertNotNull( mRequestManager );
    }

    /**
     * Test the linked accounts request
     * @throws Exception
     */
    public void testLinkedAccounts() throws Exception {
        // Register the user
        userRegistration();

        // All Correct
        mRequestManager.requestLinkedAccounts( hardwareToken, userPIP );
        semaphore.acquire();

        assertNotNull( response );
        assertEquals( ServerResponse.AUTHORIZED, response.getCode() );
        response = null;

        // Wrong PIP
        mRequestManager.requestLinkedAccounts( hardwareToken, "" );
        semaphore.acquire();

        assertNotNull( response );
        assertEquals( ServerResponse.ERROR_INCORRECT_PIP, response.getCode() );
        response = null;

        // Wrong Hardware Token
        mRequestManager.requestLinkedAccounts( "", "" );
        semaphore.acquire();

        assertNotNull( response );
        assertEquals( ServerResponse.ERROR_INCORRECT_PIP, response.getCode() );
        response = null;
    }

    /**
     * Test the de-link account, not possible to test it correctness,
     * cause we don't have a linked account
     * @throws Exception
     */
    public void testDeLinkAccount() throws Exception {
        // Register the user
        userRegistration();

        // Wrong Code
        mRequestManager.requestDeLinkAccount( hardwareToken, userPIP, "", "0" );
        semaphore.acquire();

        assertNotNull( response );
        assertEquals( ServerResponse.ERROR_FAILED, response.getCode() );
        response = null;

        // Wrong Account Type
        mRequestManager.requestDeLinkAccount( hardwareToken, userPIP, "", "2" );
        semaphore.acquire();

        assertNotNull( response );
        assertEquals( ServerResponse.ERROR_FAILED, response.getCode() );
        response = null;

        // Wrong PIP
        mRequestManager.requestDeLinkAccount( hardwareToken, "", "", "1" );
        semaphore.acquire();

        assertNotNull( response );
        assertEquals( ServerResponse.ERROR_FAILED, response.getCode() );
        response = null;

        // Wrong Hardware Token
        mRequestManager.requestDeLinkAccount( "", "", "", "0" );
        semaphore.acquire();

        assertNotNull( response );
        assertEquals( ServerResponse.ERROR_INCORRECT_PIP, response.getCode() );
        response = null;
    }

    @Override
    public void onResponse(YodoRequest.RequestType type, ServerResponse response) {
        this.response = response;
        semaphore.release();
    }
}
