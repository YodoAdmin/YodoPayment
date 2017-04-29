package co.yodo.mobile.ui.components;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import visidon.Lib.VerificationAPI;

/**
 * The callback class for camera preview
 */
public class FrameCallback implements Camera.PreviewCallback {
    private List<byte[]> callbackBuffer = new ArrayList<>();
    public InputStruct input;

    SynchronizedQueue<byte[]> queue;// = new SynchronizedQueue<byte[]>(10);
    Runnable run;// = new CameraFrameAnalyzer(queue);
    public Thread thread;//=new Thread(run);
    public int requestStop=0;

    long frameCount = 0;
    long fps = 15;
    long frameTime = 20;
    static int frameNumber = 0;
    static long lasttime;
    Context ctx;

    String versionString = "v1.2. 2013-01-15";

    FrameCallback( Camera camera, TextView statusView, TextView statusView2, Camera.Size previewSize, Context ctx){

		/*
		 * Initialization of the struct that is used to relay data between UI and the image analyzation thread.
		 */
        this.ctx = ctx;
        input = new InputStruct();
        input.camera = camera;
        input.statusTextView = statusView;

        input.status2TextView = statusView2;

        // Get initial db item count
        input.numberOfItems = VerificationAPI.getNbrOfDBItems();

		/*
		 * The buffer that stores the image data received from camera. The default image format is YCbCr_420_SP (NV21)
		 * so the size of the buffer should be 1.5 times the resolution. This buffer can be directly given to VDFaceAPI functions.
		 */
        for (int i=0;i<10;i++)
        {
            callbackBuffer.add(i,new byte[(int) (previewSize.width*previewSize.height*1.5)]);
            input.camera.addCallbackBuffer(callbackBuffer.get(i));
        }

        run = null;
        thread = null;

        queue = new SynchronizedQueue<>(10);
        //run = new CameraFrameAnalyzer(queue, input, ctx);
        run = new CameraFrameAnalyzer(queue, input, ctx);
        thread=new Thread(run);

        thread.start();

    }


    public void onPreviewFrame(byte[] data, Camera camera) {
        try {
            queue.add( data );
            // Show some statusTextView messages on the screen
            //input.status2TextView.setText(input.numberOfItems + " faces saved. Camera FPS: " + camFPS + " Processing FPS: " + processingFPS + " @ " + freq + "MHz " + versionString);*/

            if( input.enrollFlag ) {
                input.statusTextView.setText( "ENROLL STATE: " + input.enrollState );
            }
            else if( input.result != null ) {
                String a = "VERIFY STATE: ";
                String b = input.result.faceRecognition.toString();
                String c = " LIVENESS STATE: ";
                String d = input.result.livenessDetection.toString();
                input.statusTextView.setText( a + b + c + d, TextView.BufferType.SPANNABLE );
                Spannable s = (Spannable)input.statusTextView.getText();

                // Set color for verification (green allow, red deny)
                int start = a.length();
                int end = start + b.length();

                if( input.result.faceRecognition == VerificationAPI.VerifyState.ALLOW ) {
                    s.setSpan( new ForegroundColorSpan(0xFF00FF00), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE );
                    ((Activity ) ctx).setResult( Activity.RESULT_OK );
                    ((Activity) ctx).finish();
                }
                else
                    s.setSpan( new ForegroundColorSpan(0xFFFF0000), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE );

                // Set color for live (green allow, red deny)
                start = a.length() + b.length() + c.length();
                end = start + d.length();

                if( input.result.livenessDetection == VerificationAPI.VerifyState.ALLOW )
                    s.setSpan( new ForegroundColorSpan( 0xFF00FF00 ), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE );
                else
                    s.setSpan( new ForegroundColorSpan( 0xFFFF0000 ), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE );
            }
        }
        catch( InterruptedException e ) {
            e.printStackTrace();
        }
    }
}