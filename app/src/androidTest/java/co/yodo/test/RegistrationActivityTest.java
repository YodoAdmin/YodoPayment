package co.yodo.test;

import android.test.ActivityInstrumentationTestCase2;

import java.util.concurrent.Semaphore;

import co.yodo.mobile.network.model.ServerResponse;
import co.yodo.mobile.helper.AppConfig;
import co.yodo.mobile.helper.AppUtils;
import co.yodo.mobile.ui.RegistrationActivity;
import co.yodo.mobile.network.YodoRequest;

public class RegistrationActivityTest extends ActivityInstrumentationTestCase2<RegistrationActivity> implements YodoRequest.RESTListener {

    /** Authentication Number */
    private String hardwareToken;

    /** User PIP */
    private final static String userPIP   = "aaaa";

    /** Manager for the server requests */
    private YodoRequest mRequestManager;
    
    /** Server Response */
    private ServerResponse response;

    /** Semaphore */
    private Semaphore semaphore;

    public RegistrationActivityTest() {
        super( RegistrationActivity.class );
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        /* The activity object */
        RegistrationActivity activity = getActivity();
        hardwareToken = AppUtils.getHardwareToken( activity );
        semaphore     = new Semaphore( 0 );

        mRequestManager = YodoRequest.getInstance( activity );
        mRequestManager.setListener( this );
    }

    /**
     * Just in case the user is registered
     * @throws Exception
     */
    private void closeAccount() throws Exception {
        mRequestManager.requestCloseAccount( hardwareToken, userPIP );
        semaphore.acquire();

        assertNotNull( response );
        response = null;
    }

    public void test() throws Exception {
        assertNotNull( hardwareToken );
        assertNotNull( mRequestManager );
    }

    /**
     * Test different registration requests (hardware - PIP)
     * @throws Exception
     */
    public void testRegistration() throws Exception {
        // First close the account
        closeAccount();

        // All Correct
        mRequestManager.requestRegistration( hardwareToken, userPIP );
        semaphore.acquire();

        assertNotNull( response );
        assertEquals( ServerResponse.AUTHORIZED_REGISTRATION, response.getCode() );
        response = null;

        // No Hardware Token
        mRequestManager.requestRegistration( "", userPIP );
        semaphore.acquire();

        assertNotNull( response );
        assertEquals( ServerResponse.ERROR_FAILED, response.getCode() );
        response = null;

        // No Password
        mRequestManager.requestRegistration( "", "" );
        semaphore.acquire();

        assertNotNull( response );
        assertEquals( ServerResponse.ERROR_FAILED, response.getCode() );
        response = null;
    }

    /**
     * Test the registration of the biometric token
     * @throws Exception
     */
    public void testBiometricRegistration() throws Exception {
        // First close the account
        closeAccount();

        mRequestManager.requestRegistration( hardwareToken, userPIP );
        semaphore.acquire();

        String authNumber = response.getAuthNumber();
        assertNotNull( response );
        assertEquals( ServerResponse.AUTHORIZED_REGISTRATION, response.getCode() );
        response = null;

        // All Correct
        mRequestManager.requestBiometricRegistration( authNumber, AppConfig.YODO_BIOMETRIC );
        semaphore.acquire();

        assertNotNull( response );
        assertEquals( ServerResponse.AUTHORIZED, response.getCode() );
        response = null;

        // No Biometric Token
        mRequestManager.requestBiometricRegistration( authNumber, "" );
        semaphore.acquire();

        assertNotNull( response );
        assertEquals( ServerResponse.ERROR_FAILED, response.getCode() );
        response = null;

        // No Auth Number
        mRequestManager.requestBiometricRegistration( "", AppConfig.YODO_BIOMETRIC );
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
