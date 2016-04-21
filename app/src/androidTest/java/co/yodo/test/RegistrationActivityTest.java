package co.yodo.test;

import android.test.ActivityInstrumentationTestCase2;

import java.util.concurrent.Semaphore;

import co.yodo.mobile.data.ServerResponse;
import co.yodo.mobile.helper.AppConfig;
import co.yodo.mobile.helper.AppUtils;
import co.yodo.mobile.main.RegistrationActivity;
import co.yodo.mobile.net.YodoRequest;

public class RegistrationActivityTest extends ActivityInstrumentationTestCase2<RegistrationActivity> implements YodoRequest.RESTListener {
    /** The activity object */
    private RegistrationActivity activity;

    /** Authentication Number */
    private String hardwareToken;

    /** User PIP */
    private final static String userPIP   = "aaaa";

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
        activity      = getActivity();
        hardwareToken = AppUtils.getHardwareToken( activity );
        semaphore     = new Semaphore( 0 );

        YodoRequest.getInstance().setListener( this );
    }

    /**
     * Just in case the user is registered
     * @throws Exception
     */
    private void closeAccount() throws Exception {
        YodoRequest.getInstance().requestCloseAccount( activity, hardwareToken, userPIP );
        semaphore.acquire();

        assertNotNull( response );
        response = null;
    }

    public void test() throws Exception {
        assertNotNull( hardwareToken );
        assertNotNull( YodoRequest.getInstance() );
    }

    /**
     * Test different registration requests (hardware - PIP)
     * @throws Exception
     */
    public void testRegistration() throws Exception {
        // First close the account
        closeAccount();

        // All Correct
        YodoRequest.getInstance().requestRegistration( activity, hardwareToken, userPIP );
        semaphore.acquire();

        assertNotNull( response );
        assertEquals( ServerResponse.AUTHORIZED_REGISTRATION, response.getCode() );
        response = null;

        // No Hardware Token
        YodoRequest.getInstance().requestRegistration( activity, "", userPIP );
        semaphore.acquire();

        assertNotNull( response );
        assertEquals( ServerResponse.ERROR_FAILED, response.getCode() );
        response = null;

        // No Password
        YodoRequest.getInstance().requestRegistration( activity, "", "" );
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

        YodoRequest.getInstance().requestRegistration( activity, hardwareToken, userPIP );
        semaphore.acquire();

        String authNumber = response.getAuthNumber();
        assertNotNull( response );
        assertEquals( ServerResponse.AUTHORIZED_REGISTRATION, response.getCode() );
        response = null;

        // All Correct
        YodoRequest.getInstance().requestBiometricRegistration( activity, authNumber, AppConfig.YODO_BIOMETRIC );
        semaphore.acquire();

        assertNotNull( response );
        assertEquals( ServerResponse.AUTHORIZED, response.getCode() );
        response = null;

        // No Biometric Token
        YodoRequest.getInstance().requestBiometricRegistration( activity, authNumber, "" );
        semaphore.acquire();

        assertNotNull( response );
        assertEquals( ServerResponse.ERROR_FAILED, response.getCode() );
        response = null;

        // No Auth Number
        YodoRequest.getInstance().requestBiometricRegistration( activity, "", AppConfig.YODO_BIOMETRIC );
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
