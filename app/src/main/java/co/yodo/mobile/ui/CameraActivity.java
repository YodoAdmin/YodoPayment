package co.yodo.mobile.ui;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import co.yodo.mobile.R;
import co.yodo.mobile.ui.components.CameraPreview;
import co.yodo.mobile.ui.notification.ToastMaster;
import co.yodo.mobile.business.component.Intents;
import timber.log.Timber;

public class CameraActivity extends Activity {
    /** GUI Controllers */
    private CameraPreview previewCamera;
    private Camera camera;
    private TextView status;
    private TextView status2;

    // The first front facing camera
    private int defaultCameraId;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
        requestWindowFeature( Window.FEATURE_NO_TITLE );
        getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN );
        //GUIUtils.setLanguage( CameraActivity.this );
        setContentView( R.layout.activity_camera );

        setupGUI();
    }

    @Override
    protected void onResume() {
        super.onResume();

        camera = Camera.open( defaultCameraId );
        previewCamera.setCamera( camera, defaultCameraId );
    }

    @Override
    protected void onPause() {
        super.onPause();

        if( camera != null ) {
            // Try to stop frame processing thread nicely before stopping camera
            previewCamera.mFrameCallback.input.stopRequest = 1;
            previewCamera.mFrameCallback.thread.interrupt();

            while( previewCamera.mFrameCallback.input.stopRequest < 2 ) {
                try {
                    Thread.sleep( 10 );
                } catch(InterruptedException e) {
                    Timber.e( e.getMessage() );
                }
            }

            previewCamera.setCamera( null, null );
            camera.release();
            camera = null;
        }
    }

    private void setupGUI() {
        previewCamera = (CameraPreview) findViewById(R.id.cpPreview );

        status  = (TextView) findViewById( R.id.tvStatus );
        status2 = (TextView) findViewById( R.id.tvStatus2 );
        previewCamera.setTextViews( status, status2 );

        previewCamera.setKeepScreenOn( true );

        // Find the total number of cameras available
        int numberOfCameras = Camera.getNumberOfCameras();

        // Find the ID of the default camera
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for( int i = 0; i < numberOfCameras; i++ ) {
            Camera.getCameraInfo( i, cameraInfo );
            if( cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT ) {
                defaultCameraId = i;
            }
        }

        Bundle extras = getIntent().getExtras();
        if( extras != null ) {
            String token = extras.getString( Intents.BIOMETRIC_TOKEN );
            previewCamera.setFace( token );

            ToastMaster.makeText( this, R.string.text_locate_face_recognition, Toast.LENGTH_LONG ).show();
        } else {
            ToastMaster.makeText( this, R.string.text_train_face_recognition, Toast.LENGTH_LONG ).show();
        }
    }
}
