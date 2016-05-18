package co.yodo.test;

import android.test.ActivityInstrumentationTestCase2;

import java.util.concurrent.Semaphore;

import co.yodo.mobile.network.model.ServerResponse;
import co.yodo.mobile.helper.AppUtils;
import co.yodo.mobile.ui.MainActivity;
import co.yodo.mobile.network.YodoRequest;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> implements YodoRequest.RESTListener {
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

    public MainActivityTest() {
        super( MainActivity.class );
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        /* The activity object */
        MainActivity activity = getActivity();
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
        assertNotNull(mRequestManager );
    }

    /**
     * Test the authentication request (hardware)
     * @throws Exception
     */
    public void testAuthentication() throws Exception {
        // Register the user
        userRegistration();

       mRequestManager.requestClientAuth( hardwareToken );
        semaphore.acquire();

        assertNotNull( response );
        assertEquals( ServerResponse.AUTHORIZED, response.getCode() );
        response = null;

       mRequestManager.requestClientAuth( "" );
        semaphore.acquire();

        assertNotNull( response );
        assertEquals( ServerResponse.ERROR_FAILED, response.getCode() );
        response = null;
    }

    /**
     * Test the authentication request with the PIP
     * @throws Exception
     */
    public void testPIPAuthentication() throws Exception {
        // Register the user
        userRegistration();

        // All Correct
       mRequestManager.requestClientAuth( hardwareToken, userPIP );
        semaphore.acquire();

        assertNotNull( response );
        assertEquals( ServerResponse.AUTHORIZED, response.getCode() );
        response = null;

        // Wrong PIP
       mRequestManager.requestClientAuth( hardwareToken, "" );
        semaphore.acquire();

        assertNotNull( response );
        assertEquals( ServerResponse.ERROR_INCORRECT_PIP, response.getCode() );
        response = null;

        // Wrong Hardware Token
       mRequestManager.requestClientAuth( "", "" );
        semaphore.acquire();

        assertNotNull( response );
        assertEquals( ServerResponse.ERROR_INCORRECT_PIP, response.getCode() );
        response = null;
    }

    /**
     * Test a query request to get the balance
     * @throws Exception
     */
    public void testBalance() throws Exception {
        // Register the user
        userRegistration();

        // All Correct
       mRequestManager.requestBalance( hardwareToken, userPIP );
        semaphore.acquire();

        assertNotNull( response );
        assertEquals( ServerResponse.AUTHORIZED_BALANCE, response.getCode() );
        response = null;

        // Wrong PIP
       mRequestManager.requestBalance( hardwareToken, "" );
        semaphore.acquire();

        assertNotNull( response );
        assertEquals( ServerResponse.ERROR_INCORRECT_PIP, response.getCode() );
        response = null;

        // Wrong Hardware Token
       mRequestManager.requestBalance( "", "" );
        semaphore.acquire();

        assertNotNull( response );
        assertEquals( ServerResponse.ERROR_INCORRECT_PIP, response.getCode() );
        response = null;
    }

    /**
     * Request a path (URL) for an advertising image
     * @throws Exception
     */
    public void testAdvertising() throws Exception {
        // Register the user
        userRegistration();

        // Merchant name for advertising
        String merchant = "Oreganos";

        // All Correct
       mRequestManager.requestAdvertising( hardwareToken, merchant );
        semaphore.acquire();

        assertNotNull( response );
        assertEquals( ServerResponse.AUTHORIZED, response.getCode() );
        response = null;

        // Wrong PIP
       mRequestManager.requestAdvertising( hardwareToken, "" );
        semaphore.acquire();

        assertNotNull( response );
        assertEquals( ServerResponse.ERROR_FAILED, response.getCode() );
        response = null;

        // Wrong Hardware Token
       mRequestManager.requestAdvertising( "", "" );
        semaphore.acquire();

        assertNotNull( response );
        assertEquals( ServerResponse.ERROR_FAILED, response.getCode() );
        response = null;
    }

    /**
     * Test the close account request
     * @throws Exception
     */
    public void testCloseAccount() throws Exception {
        // Register the user
        userRegistration();

        // All Correct
       mRequestManager.requestCloseAccount( hardwareToken, userPIP );
        semaphore.acquire();

        assertNotNull( response );
        assertEquals( ServerResponse.AUTHORIZED, response.getCode() );
        response = null;

        // Wrong PIP
       mRequestManager.requestCloseAccount( hardwareToken, "" );
        semaphore.acquire();

        assertNotNull( response );
        assertEquals( ServerResponse.ERROR_INCORRECT_PIP, response.getCode() );
        response = null;

        // Wrong Hardware Token
       mRequestManager.requestCloseAccount( "", "" );
        semaphore.acquire();

        assertNotNull( response );
        assertEquals( ServerResponse.ERROR_INCORRECT_PIP, response.getCode() );
        response = null;
    }

    /**
     * Test the request linking code
     * @throws Exception
     */
    public void testLinkingCode() throws Exception {
        // Register the user
        userRegistration();

        // All Correct
       mRequestManager.requestLinkingCode( hardwareToken, userPIP );
        semaphore.acquire();

        assertNotNull( response );
        assertEquals( ServerResponse.AUTHORIZED, response.getCode() );
        response = null;

        // Wrong PIP
       mRequestManager.requestLinkingCode( hardwareToken, "" );
        semaphore.acquire();

        assertNotNull( response );
        assertEquals( ServerResponse.ERROR_INCORRECT_PIP, response.getCode() );
        response = null;

        // Wrong Hardware Token
       mRequestManager.requestLinkingCode( "", "" );
        semaphore.acquire();

        assertNotNull( response );
        assertEquals( ServerResponse.ERROR_INCORRECT_PIP, response.getCode() );
        response = null;
    }

    /**
     * Test the link accounts, not possible to test it correctness,
     * cause we don't have a linking code
     * @throws Exception
     */
    public void testLinkAccounts() throws Exception {
        // Register the user
        userRegistration();

        // Wrong Code
       mRequestManager.requestLinkAccount( hardwareToken, "" );
        semaphore.acquire();

        assertNotNull( response );
        assertEquals( ServerResponse.ERROR_FAILED, response.getCode() );
        response = null;

        // Wrong Hardware Token
       mRequestManager.requestLinkingCode( "", "" );
        semaphore.acquire();

        assertNotNull( response );
        assertEquals( ServerResponse.ERROR_FAILED, response.getCode() );
        response = null;
    }

    @Override
    public void onResponse(YodoRequest.RequestType type, ServerResponse response) {
        this.response = response;
        semaphore.release();
    }
}