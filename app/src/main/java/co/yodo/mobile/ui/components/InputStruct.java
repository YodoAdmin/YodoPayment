package co.yodo.mobile.ui.components;

import android.hardware.Camera;
import android.widget.TextView;

import visidon.Lib.FaceInfo;
import visidon.Lib.VerificationAPI;

/**
 * This class is a container of data (exchange between analysis thread and application)
 */
@SuppressWarnings( "deprecation" )
public class InputStruct {
    Camera camera;
    public int stopRequest = 0;
    boolean resetFlag;
    boolean enrollFlag;
    public long time = 0;
    float internalFPS = 0.0f;
    int frequency = 0;
    float cameraFPS = 0.0f;
    int numberOfItems = 0;
    FaceInfo result = null;
    TextView statusTextView;
    TextView status2TextView;
    VerificationAPI.EnrollState enrollState;
}
