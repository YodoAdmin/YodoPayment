package co.yodo.mobile.ui.components;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.os.SystemClock;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import co.yodo.mobile.business.component.Intents;
import co.yodo.mobile.utils.CryptUtils;
import timber.log.Timber;
import visidon.Lib.VerificationAPI;

class CameraFrameAnalyzer implements Runnable {
    /** Analyzer elements */
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
        camera = aInput.camera;
        input = aInput;
        this.ctx = ctx;
    }

    public void run() {
        int succesivenonzeos = 0;
        int extraremoveperframe = 0;

        try {
            while( input.stopRequest == 0 ) {
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

                if( input.stopRequest == 0 ) {
                    analyzeFrame( data );
                    counter++;
                    camera.addCallbackBuffer( data );
                }
            }
        } catch(InterruptedException e) {
            Timber.e( e.getMessage() );
        }
        input.stopRequest = 2;
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
            Timber.e( e.getMessage());
        }

        return currentFreq;
    }

    /**
     *  Main method to apply facial image analysis for input frames
     */
    public void analyzeFrame(byte[] data) {
        long t1 = SystemClock.elapsedRealtime();

        // User has requested DB reset (long touch event)
        if( input.resetFlag ){
            // Reset face database.
            VerificationAPI.ResetState state = VerificationAPI.resetDatabase();
            Timber.e( "ResetState: " + state );

            input.resetFlag = false;
            input.numberOfItems = VerificationAPI.getNbrOfDBItems();
        }
        // User has requested enrolling (touch event)
        else if( input.enrollFlag ) {
            // Enroll the face in the image. The NV12 image format holds gray level information at the start of
            // the buffer, so it can be directly given to the method.
            input.enrollState = VerificationAPI.enrollFace( data );

            // Example to show how to get the enrolled face template data
            // and how to load data from template to internal database
            if( input.enrollState == VerificationAPI.EnrollState.DONE ) {
                input.enrollFlag = false;
                input.numberOfItems = VerificationAPI.getNbrOfDBItems();

                input.enrollFlag = false;
                input.numberOfItems = VerificationAPI.getNbrOfDBItems();
                byte [] faceTemplate = VerificationAPI.getEnrolledFaceTemplate();
                // save template somewhere
                String image_str = CryptUtils.bytesToHex( faceTemplate );

                Timber.e( image_str );

                Intent resultIntent = new Intent();
                resultIntent.putExtra( Intents.RESULT_FACE, image_str );
                ((Activity )ctx).setResult( Activity.RESULT_OK, resultIntent );
                ((Activity)ctx).finish();

                Timber.e( "face template obtained, size = " + faceTemplate.length );
                Timber.e( "deleting face database" );

                // delete recently enrolled face
                VerificationAPI.ResetState resState = VerificationAPI.resetDatabase();

                Timber.e( "reset state = " + resState );

                // load template
                VerificationAPI.LoadState lstate = VerificationAPI.loadFaceTemplate( faceTemplate );
                Timber.e( "load face template state = " + lstate );
                input.numberOfItems = VerificationAPI.getNbrOfDBItems();
            }
        } else { // Otherwise apply verification
            // Verify the face in the image. The NV12 image format holds gray level information at the start of
            // the buffer, so it can be directly given to the method.
            VerificationAPI.verifyFace( data );
            // Get specific information about face verification
            input.result = VerificationAPI.getFaceInfo();
        }

        // Calculate some additional statusTextView information such as
        // a) camera FPS
        // b) internal face engine processing fps
        // c) current CPU frequency

        long t2 = SystemClock.elapsedRealtime();
        long time = ( t2 - t1 );
        totaltime += time;

        frameNumber++;

        if( frameNumber < 2 ) {
            input.internalFPS = 30.0f;
            starttime = t1;
        }

        // do some filtering to FPS and frequency values
        input.internalFPS = 0.95f * input.internalFPS + 0.05f * time;
        int currentFrequency=getFrequency();

        if( frameNumber < 2 )
            input.cameraFPS = 30.0f;
        else
            input.cameraFPS = 0.95f * input.cameraFPS + 0.05f * ( t2 - lasttime );

        lasttime = t2;
        input.frequency = (int)( 0.95 * input.frequency + 0.05 * currentFrequency );
    }
}