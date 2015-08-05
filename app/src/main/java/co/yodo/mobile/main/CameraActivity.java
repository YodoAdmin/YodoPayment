package co.yodo.mobile.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import co.yodo.mobile.R;
import co.yodo.mobile.component.ToastMaster;
import co.yodo.mobile.helper.AppUtils;
import co.yodo.mobile.helper.Intents;
import visidon.Lib.FaceInfo;
import visidon.Lib.VerificationAPI;

public class CameraActivity extends Activity {
    /** DEBUG */
    private final static String TAG = CameraActivity.class.getSimpleName();

    /** GUI Controllers */
    private CameraPreview mPreview;
    private Camera mCamera;
    private TextView status;
    private TextView status2;

    private int cameraCurrentlyLocked;

    // The first front facing camera
    private int defaultCameraId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature( Window.FEATURE_NO_TITLE );
        getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN );
        AppUtils.setLanguage( CameraActivity.this );
        setContentView(R.layout.activity_camera);

        setupGUI();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mCamera = Camera.open( defaultCameraId );
        cameraCurrentlyLocked = defaultCameraId;
        mPreview.setCamera( mCamera, defaultCameraId );
    }

    @Override
    protected void onPause() {
        super.onPause();

        if( mCamera != null ) {
            // Try to stop frame processing thread nicely before stopping camera
            mPreview.mFrameCallback.input.mStopRequest = 1;
            mPreview.mFrameCallback.thread.interrupt();

            while( mPreview.mFrameCallback.input.mStopRequest < 2 ) {
                try {
                    Thread.sleep( 10 );
                } catch(InterruptedException e) {
                    AppUtils.Logger( TAG, e.getMessage() );
                }
            }

            mPreview.setCamera( null, null );
            mCamera.release();
            mCamera = null;
        }
    }

    private void setupGUI() {
        mPreview = (CameraPreview) findViewById(R.id.preview);

        status  = (TextView) findViewById( R.id.status );
        status2 = (TextView) findViewById( R.id.status2 );
        mPreview.setTextViews( status, status2 );

        mPreview.setKeepScreenOn( true );

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
            mPreview.setFace( token );

            ToastMaster.makeText( this, R.string.message_face_recognition, Toast.LENGTH_LONG ).show();
            /*new ShowcaseView.Builder( this )
                    .setTarget( Target.NONE )
                    .setContentTitle( R.string.title_face_recognition )
                    .setContentText( R.string.message_face_recognition )
                    .build();*/
        } else {
            ToastMaster.makeText( this, R.string.message_train_camera, Toast.LENGTH_LONG ).show();
            /*new ShowcaseView.Builder( this )
                    .setTarget( Target.NONE )
                    .setContentTitle( R.string.title_train_camera )
                    .setContentText( R.string.message_train_camera )
                    .build();*/
        }
    }
}

/**
 * The callback class for camera preview
 */
class FrameCallback implements Camera.PreviewCallback {
    /** DEBUG */
    private static final String TAG = FrameCallback.class.getSimpleName();

    private List<byte[]> callbackBuffer = new ArrayList<byte[]>();
    InputStruct input;

    SynchronizedQueue<byte[]> queue;// = new SynchronizedQueue<byte[]>(10);
    Runnable run;// = new CameraFrameAnalyzer(queue);
    Thread thread;//=new Thread(run);
    public int requestStop=0;

    long frameCount = 0;
    long fps = 15;
    long frameTime = 20;
    static int frameNumber = 0;
    static long lasttime;
    Context ctx;

    String versionString = "v1.2. 2013-01-15";

    FrameCallback(Camera camera, TextView statusView, TextView statusView2, Camera.Size previewSize, Context ctx){

		/*
		 * Initialization of the struct that is used to relay data between UI and the image analyzation thread.
		 */
        this.ctx = ctx;
        input = new InputStruct();
        input.mCamera = camera;
        input.mStatus = statusView;

        input.mStatus2 = statusView2;

        // Get initial db item count
        input.mNbrOfItems = VerificationAPI.getNbrOfDBItems();

		/*
		 * The buffer that stores the image data received from camera. The default image format is YCbCr_420_SP (NV21)
		 * so the size of the buffer should be 1.5 times the resolution. This buffer can be directly given to VDFaceAPI functions.
		 */
        for (int i=0;i<10;i++)
        {
            callbackBuffer.add(i,new byte[(int) (previewSize.width*previewSize.height*1.5)]);
            input.mCamera.addCallbackBuffer(callbackBuffer.get(i));
        }

        run = null;
        thread = null;

        queue = new SynchronizedQueue<byte[]>(10);
        //run = new CameraFrameAnalyzer(queue, input, ctx);
        run = new CameraFrameAnalyzer(queue, input, ctx);
        thread=new Thread(run);

        thread.start();

    }


    public void onPreviewFrame(byte[] data, Camera camera) {
        try {
            queue.add( data );
			// Show some status messages on the screen
			//input.mStatus2.setText(input.mNbrOfItems + " faces saved. Camera FPS: " + camFPS + " Processing FPS: " + processingFPS + " @ " + freq + "MHz " + versionString);*/

            if( input.mEnrollFlag ) {
                input.mStatus.setText( "ENROLL STATE: " + input.enrollState );
            }
            else if( input.mResult != null ) {
                String a = "VERIFY STATE: ";
                String b = input.mResult.faceRecognition.toString();
                String c = " LIVENESS STATE: ";
                String d = input.mResult.livenessDetection.toString();
                input.mStatus.setText( a + b + c + d, TextView.BufferType.SPANNABLE );
                Spannable s = (Spannable)input.mStatus.getText();

                // Set color for verification (green allow, red deny)
                int start = a.length();
                int end = start + b.length();

                if( input.mResult.faceRecognition == VerificationAPI.VerifyState.ALLOW ) {
                    s.setSpan( new ForegroundColorSpan(0xFF00FF00), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE );
                    ((Activity) ctx).setResult( Activity.RESULT_OK );
                    ((Activity) ctx).finish();
                }
                else
                    s.setSpan( new ForegroundColorSpan(0xFFFF0000), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE );


                // Set color for live (green allow, red deny)
                start = a.length() + b.length() + c.length();
                end = start + d.length();

                if( input.mResult.livenessDetection == VerificationAPI.VerifyState.ALLOW )
                    s.setSpan( new ForegroundColorSpan( 0xFF00FF00 ), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE );
                else
                    s.setSpan( new ForegroundColorSpan( 0xFFFF0000 ), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE );
            }

        }
        catch(InterruptedException e) {
            AppUtils.Logger( TAG, e.getMessage() );
        }
    }
}

/**
 * This class is a container of data (exchange between analysis thread and application)
 */
class InputStruct {
    public Camera mCamera;
    public int mStopRequest = 0;
    public boolean mResetFlag;
    public boolean mEnrollFlag;
    public double processingTime = 0;
    public int frameCounter = 0;
    public long time = 0;
    public float mInternalFPS = 0.0f;
    public int mFrequency = 0;
    public float mCameraFPS = 0.0f;
    public int mNbrOfItems = 0;
    public FaceInfo mResult = null;
    public TextView mStatus;
    public TextView mStatus2;
    public VerificationAPI.EnrollState enrollState;
}

//----------------------------------------------------------------------

class CameraFrameAnalyzer implements Runnable {
    /** DEBUG */
    private final static String TAG = CameraFrameAnalyzer.class.getSimpleName();

    private SynchronizedQueue<byte[]> queue;
    private Context ctx;
    public Camera camera;
    public InputStruct input;
    static int frameNumber = 0;
    static long starttime =0;
    static long totaltime =0;
    static int maxfrequency =0;
    static long lasttime;
    static int frequency=200000;

    static int nbrOfFrames = 0;
    static int counter = 0;
    static int nbrOfFaces = 0;

    public CameraFrameAnalyzer(SynchronizedQueue<byte[]> aQueue, InputStruct aInput, Context ctx) {
        queue = aQueue;
        camera = aInput.mCamera;
        input = aInput;
        this.ctx = ctx;
    }

    public void run() {
        int succesivenonzeos = 0;
        int extraremoveperframe = 0;

        try {
            while( input.mStopRequest == 0 ) {
                // Check frame queue size and determine if there are too many frames
                // pending.
                if( queue.size == 0 ) {
                    succesivenonzeos = 0;
                    extraremoveperframe--;
                }
                else
                    succesivenonzeos++;

                if( succesivenonzeos > 10 ) {
                    succesivenonzeos = 0;
                    extraremoveperframe++;
                }

                if( extraremoveperframe > ( queue.size - 1 ) )
                    extraremoveperframe = ( queue.size - 1 );

                if( extraremoveperframe < 0 )
                    extraremoveperframe = 0;

                for( int j = 0; j < extraremoveperframe; j++ ) {
                    // remove frames from queue to avoid latency
                    byte[] data = queue.remove();
                    camera.addCallbackBuffer( data );
                    counter++;
                }

                // Get frame data from queue
                byte[] data = queue.remove();

                if( input.mStopRequest == 0 ) {
                    analyzeFrame( data );
                    counter++;
                    camera.addCallbackBuffer( data );
                }
            }
        } catch(InterruptedException e) {
            AppUtils.Logger( TAG, e.getMessage() );
        }
        input.mStopRequest = 2;
    }

    /**
     * Helper method for getting the current CPU operating frequency.
     */
    public int getFrequency() {
        int currentFreq = -1;
        File file = new File( "/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq" );

        //Read text from file
        try {
            BufferedReader br = new BufferedReader( new FileReader( file ) );
            String line;

            while( ( line = br.readLine() ) != null ) {
                currentFreq = Integer.parseInt( line );
            }
        } catch(IOException e) {
            AppUtils.Logger(TAG, e.getMessage());
        }

        return currentFreq;
    }

    /**
     * Helper method for getting the maximum CPU frequency.
     */
    public int getMaxFrequency() {
        int currentFreq = -1;
        File file = new File( "/sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq" );

        //Read text from file
        try {
            BufferedReader br = new BufferedReader( new FileReader( file ) );
            String line;

            while( ( line = br.readLine() ) != null ) {
                currentFreq = Integer.parseInt( line );
            }
        } catch(IOException e) {
            AppUtils.Logger( TAG, e.getMessage() );
        }

        return currentFreq;
    }

    /**
     *  Main method to apply facial image analysis for input frames
     */
    public void analyzeFrame(byte[] data) {
        long t1 = SystemClock.elapsedRealtime();

        // User has requested DB reset (long touch event)
        if( input.mResetFlag ){
            // Reset face database.
            VerificationAPI.ResetState state = VerificationAPI.resetDatabase();
            AppUtils.Logger( TAG, "ResetState: " + state );

            input.mResetFlag = false;
            input.mNbrOfItems = VerificationAPI.getNbrOfDBItems();
        }
        // User has requested enrolling (touch event)
        else if( input.mEnrollFlag ) {
            // Enroll the face in the image. The NV12 image format holds gray level information at the start of
            // the buffer, so it can be directly given to the method.
            input.enrollState = VerificationAPI.enrollFace( data );

            // Example to show how to get the enrolled face template data
            // and how to load data from template to internal database
            if( input.enrollState == VerificationAPI.EnrollState.DONE ) {
                input.mEnrollFlag = false;
                input.mNbrOfItems = VerificationAPI.getNbrOfDBItems();

                input.mEnrollFlag = false;
                input.mNbrOfItems = VerificationAPI.getNbrOfDBItems();
                byte [] faceTemplate = VerificationAPI.getEnrolledFaceTemplate();
                // save template somewhere
                String image_str = AppUtils.bytesToHex( faceTemplate );

                AppUtils.Logger( TAG, image_str );

                Intent resultIntent = new Intent();
                resultIntent.putExtra( Intents.RESULT_FACE, image_str );
                ((Activity)ctx).setResult( Activity.RESULT_OK, resultIntent );
                ((Activity)ctx).finish();

                AppUtils.Logger( TAG, "face template obtained, size = " + faceTemplate.length );
                AppUtils.Logger( TAG, "deleting face database" );

                // delete recently enrolled face
                VerificationAPI.ResetState resState = VerificationAPI.resetDatabase();

                AppUtils.Logger( TAG, "reset state = " + resState );

                // load template
                VerificationAPI.LoadState lstate = VerificationAPI.loadFaceTemplate( faceTemplate );
                AppUtils.Logger( TAG, "load face template state = " + lstate );
                input.mNbrOfItems = VerificationAPI.getNbrOfDBItems();
            }
        } else { // Otherwise apply verification
            // Verify the face in the image. The NV12 image format holds gray level information at the start of
            // the buffer, so it can be directly given to the method.
            VerificationAPI.verifyFace( data );
            // Get specific information about face verification
            input.mResult = VerificationAPI.getFaceInfo();
        }

        // Calculate some additional status information such as
        // a) camera FPS
        // b) internal face engine processing fps
        // c) current CPU frequency

        long t2 = SystemClock.elapsedRealtime();
        long time = ( t2 - t1 );
        totaltime += time;

        frameNumber++;

        if( frameNumber < 2 ) {
            input.mInternalFPS = 30.0f;
            starttime = t1;
        }

        // do some filtering to FPS and frequency values
        input.mInternalFPS = 0.95f * input.mInternalFPS + 0.05f * time;
        int currentFrequency=getFrequency();

        if( frameNumber < 2 )
            input.mCameraFPS = 30.0f;
        else
            input.mCameraFPS = 0.95f * input.mCameraFPS + 0.05f * ( t2 - lasttime );

        lasttime = t2;
        input.mFrequency = (int)( 0.95 * input.mFrequency + 0.05 * currentFrequency );
    }
}

/** Helper class for containing frames. Frames got from camera are put to the queue, and
 *  the same queue is read by face analysis thread.
 */
class SynchronizedQueue<V> {
    private Object[] elements;
    private int head;
    private int tail;
    public int size;

    public SynchronizedQueue(int capacity) {
        elements = new Object[ capacity ];
        head = 0;
        tail = 0;
        size = 0;
    }

    public synchronized V remove() throws InterruptedException {
        while( size == 0 )
            wait();

        if( size==0 )
            return null;

        @SuppressWarnings( "unchecked" )
        V r = (V) elements[ head ];
        head++;
        size--;

        if( head == elements.length )
            head = 0;

        notifyAll();
        return r;
    }

    public synchronized void add(V newValue) throws InterruptedException {
        while( size == elements.length )
            wait();

        elements[ tail ] = newValue;
        tail++;
        size++;

        if( tail == elements.length )
            tail = 0;
        notifyAll();
    }
}