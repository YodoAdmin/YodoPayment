package co.yodo.test;

import android.test.ActivityInstrumentationTestCase2;

import java.util.concurrent.Semaphore;

import co.yodo.mobile.network.model.ServerResponse;
import co.yodo.mobile.helper.AppUtils;
import co.yodo.mobile.ui.PipResetActivity;
import co.yodo.mobile.network.YodoRequest;

public class PipResetActivityTest extends ActivityInstrumentationTestCase2<PipResetActivity> implements YodoRequest.RESTListener {
    /** The activity object */
    private PipResetActivity activity;

    /** Authentication Number */
    private String hardwareToken;

    /** User information */
    private final static String userPIP    = "aaaa";
    private final static String userNewPIP = "aaaa";

    /** Manager for the server requests */
    private YodoRequest mRequestManager;
    
    /** Server Response */
    private ServerResponse response;

    /** Semaphore */
    private Semaphore semaphore;

    public PipResetActivityTest() {
        super( PipResetActivity.class );
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        activity      = getActivity();
        hardwareToken = AppUtils.getHardwareToken(activity);
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
     * Test the reset PIP with a new PIP, current pip required
     * @throws Exception
     */
    public void testPIPReset() throws Exception {
        // Register the user
        userRegistration();

        // All Correct
        mRequestManager.requestPIPReset( hardwareToken, userPIP, userNewPIP );
        semaphore.acquire();

        assertNotNull( response );
        assertEquals( ServerResponse.AUTHORIZED, response.getCode() );
        response = null;

        // Wrong PIP
        mRequestManager.requestPIPReset( hardwareToken, "", userNewPIP );
        semaphore.acquire();

        assertNotNull( response );
        assertEquals( ServerResponse.ERROR_INCORRECT_PIP, response.getCode() );
        response = null;

        // Wrong New PIP
        mRequestManager.requestPIPReset( hardwareToken, userPIP, "" );
        semaphore.acquire();

        assertNotNull( response );
        assertEquals( ServerResponse.ERROR_FAILED, response.getCode() );
        response = null;

        // Wrong Hardware Token
        mRequestManager.requestPIPReset( "", userPIP, userNewPIP );
        semaphore.acquire();

        assertNotNull( response );
        assertEquals( ServerResponse.ERROR_FAILED, response.getCode() );
        response = null;
    }

    /**
     * Test the reset PIP using a biometric token
     * @throws Exception
     */
    public void testBiometricPIPReset() throws Exception {
        // Register the user
        userRegistration();

        mRequestManager.requestBiometricToken( hardwareToken );
        semaphore.acquire();

        String authNumber = response.getAuthNumber();
        assertNotNull( response );
        assertEquals( ServerResponse.AUTHORIZED, response.getCode() );
        response = null;

        // All Correct
        mRequestManager.requestBiometricPIPReset( authNumber, hardwareToken , userNewPIP );
        semaphore.acquire();

        assertNotNull( response );
        assertEquals( ServerResponse.AUTHORIZED, response.getCode() );
        response = null;

        // Wrong Auth Number
        mRequestManager.requestBiometricPIPReset( "", hardwareToken , userNewPIP );
        semaphore.acquire();

        assertNotNull( response );
        assertEquals( ServerResponse.ERROR_FAILED, response.getCode() );
        response = null;

        // Wrong Hardware Token
        mRequestManager.requestBiometricPIPReset( authNumber, "" , userNewPIP );
        semaphore.acquire();

        assertNotNull( response );
        assertEquals( ServerResponse.ERROR_FAILED, response.getCode() );
        response = null;

        // No New PIP
        mRequestManager.requestBiometricPIPReset( authNumber, hardwareToken , "" );
        semaphore.acquire();

        assertNotNull( response );
        assertEquals( ServerResponse.ERROR_FAILED, response.getCode() );
        response = null;
    }

    /**
     * Test the request of the biometric token to the server
     * @throws Exception
     */
    public void testBiometricToken() throws Exception {
        // Register the user
        userRegistration();

        // All Correct
        mRequestManager.requestBiometricToken( hardwareToken );
        semaphore.acquire();

        assertNotNull( response );
        assertEquals( ServerResponse.AUTHORIZED, response.getCode() );
        response = null;

        // Wrong Hardware Token
        mRequestManager.requestBiometricToken( "" );
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
