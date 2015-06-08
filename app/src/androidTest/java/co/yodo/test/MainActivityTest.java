package co.yodo.test;

import android.test.ActivityInstrumentationTestCase2;

import java.util.concurrent.Semaphore;

import co.yodo.mobile.data.ServerResponse;
import co.yodo.mobile.helper.AppUtils;
import co.yodo.mobile.main.MainActivity;
import co.yodo.mobile.net.YodoRequest;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> implements YodoRequest.RESTListener {
    /** The activity object */
    private MainActivity activity;

    /** Authentication Number */
    private String hardwareToken;

    /** User PIP */
    private final static String userPIP = "aaaa";

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
        activity      = getActivity();
        hardwareToken = AppUtils.getHardwareToken( activity );
        semaphore     = new Semaphore( 0 );

        YodoRequest.getInstance().setListener( this );
    }

    /**
     * Just in case the user is not registered
     * @throws Exception
     */
    private void userRegistration() throws Exception {
        YodoRequest.getInstance().requestRegistration( activity, hardwareToken, userPIP );
        semaphore.acquire();

        assertNotNull( response );
        response = null;
    }

    public void test() throws Exception {
        assertNotNull( hardwareToken );
        assertNotNull( YodoRequest.getInstance() );
    }

    /**
     * Test the authentication request (hardware)
     * @throws Exception
     */
    public void testAuthentication() throws Exception {
        // Register the user
        userRegistration();

        YodoRequest.getInstance().requestAuthentication( activity, hardwareToken );
        semaphore.acquire();

        assertNotNull( response );
        assertEquals( ServerResponse.AUTHORIZED, response.getCode() );
        response = null;

        YodoRequest.getInstance().requestAuthentication( activity, "" );
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
        YodoRequest.getInstance().requestPIPAuthentication( activity, hardwareToken, userPIP );
        semaphore.acquire();

        assertNotNull( response );
        assertEquals( ServerResponse.AUTHORIZED, response.getCode() );
        response = null;

        // Wrong PIP
        YodoRequest.getInstance().requestPIPAuthentication( activity, hardwareToken, "" );
        semaphore.acquire();

        assertNotNull( response );
        assertEquals( ServerResponse.ERROR_INCORRECT_PIP, response.getCode() );
        response = null;

        // Wrong Hardware Token
        YodoRequest.getInstance().requestPIPAuthentication( activity, "", "" );
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
        YodoRequest.getInstance().requestBalance( activity, hardwareToken, userPIP );
        semaphore.acquire();

        assertNotNull( response );
        assertEquals( ServerResponse.AUTHORIZED_BALANCE, response.getCode() );
        response = null;

        // Wrong PIP
        YodoRequest.getInstance().requestBalance( activity, hardwareToken, "" );
        semaphore.acquire();

        assertNotNull( response );
        assertEquals( ServerResponse.ERROR_INCORRECT_PIP, response.getCode() );
        response = null;

        // Wrong Hardware Token
        YodoRequest.getInstance().requestBalance( activity, "", "" );
        semaphore.acquire();

        assertNotNull( response );
        assertEquals( ServerResponse.ERROR_INCORRECT_PIP, response.getCode() );
        response = null;
    }

    /**
     * Test the query request to get the last receipt
     * @throws Exception
     */
    public void testReceipt() throws Exception {
        // Register the user
        userRegistration();

        // All Correct
        YodoRequest.getInstance().requestReceipt( activity, hardwareToken, userPIP );
        semaphore.acquire();

        assertNotNull( response );
        assertEquals( ServerResponse.ERROR_FAILED, response.getCode() );
        response = null;

        // Wrong PIP
        YodoRequest.getInstance().requestReceipt( activity, hardwareToken, "" );
        semaphore.acquire();

        assertNotNull( response );
        assertEquals( ServerResponse.ERROR_FAILED, response.getCode() );
        response = null;

        // Wrong Hardware Token
        YodoRequest.getInstance().requestReceipt( activity, "", "" );
        semaphore.acquire();

        assertNotNull( response );
        assertEquals( ServerResponse.ERROR_FAILED, response.getCode() );
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
        YodoRequest.getInstance().requestAdvertising( activity, hardwareToken, merchant );
        semaphore.acquire();

        assertNotNull( response );
        assertEquals( ServerResponse.AUTHORIZED, response.getCode() );
        response = null;

        // Wrong PIP
        YodoRequest.getInstance().requestAdvertising( activity, hardwareToken, "" );
        semaphore.acquire();

        assertNotNull( response );
        assertEquals( ServerResponse.ERROR_FAILED, response.getCode() );
        response = null;

        // Wrong Hardware Token
        YodoRequest.getInstance().requestAdvertising( activity, "", "" );
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
        YodoRequest.getInstance().requestCloseAccount( activity, hardwareToken, userPIP );
        semaphore.acquire();

        assertNotNull( response );
        assertEquals( ServerResponse.AUTHORIZED, response.getCode() );
        response = null;

        // Wrong PIP
        YodoRequest.getInstance().requestCloseAccount( activity, hardwareToken, "" );
        semaphore.acquire();

        assertNotNull( response );
        assertEquals( ServerResponse.ERROR_INCORRECT_PIP, response.getCode() );
        response = null;

        // Wrong Hardware Token
        YodoRequest.getInstance().requestCloseAccount( activity, "", "" );
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
        YodoRequest.getInstance().requestLinkingCode( activity, hardwareToken, userPIP );
        semaphore.acquire();

        assertNotNull( response );
        assertEquals( ServerResponse.AUTHORIZED, response.getCode() );
        response = null;

        // Wrong PIP
        YodoRequest.getInstance().requestLinkingCode( activity, hardwareToken, "" );
        semaphore.acquire();

        assertNotNull( response );
        assertEquals( ServerResponse.ERROR_INCORRECT_PIP, response.getCode() );
        response = null;

        // Wrong Hardware Token
        YodoRequest.getInstance().requestLinkingCode( activity, "", "" );
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
        YodoRequest.getInstance().requestLinkAccount( activity, hardwareToken, "" );
        semaphore.acquire();

        assertNotNull( response );
        assertEquals( ServerResponse.ERROR_FAILED, response.getCode() );
        response = null;

        // Wrong Hardware Token
        YodoRequest.getInstance().requestLinkingCode( activity, "", "" );
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