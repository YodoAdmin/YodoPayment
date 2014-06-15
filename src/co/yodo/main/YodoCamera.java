/*
 * Copyright (C) 2013 Visidon Oy
 * 
 * This file is part of Visidon Android SDK (Android examples) and
 * is based on the Android camera example files (see original license below).
 *    
 */

/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package co.yodo.main;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.TextView.BufferType;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import visidon.Lib.FaceInfo;
import visidon.Lib.VerificationAPI;
import visidon.Lib.VerificationAPI.*;
import co.yodo.R;
import co.yodo.helper.Utils;
import co.yodo.helper.YodoGlobals;
import co.yodo.main.CameraFrameAnalyzer;
import co.yodo.main.InputStruct;
import co.yodo.main.SynchronizedQueue;

// ----------------------------------------------------------------------
public class YodoCamera extends Activity {
	private Preview mPreview;
	Camera mCamera;
	int numberOfCameras;
	int cameraCurrentlyLocked;

	private TextView status = null;
	private TextView status2 = null;
	
	// The first front facing camera
	int defaultCameraId;
	private String token;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Utils.changeLanguage(this);

		// Hide the window title.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

		// Create a RelativeLayout container that will hold a SurfaceView,
		// and set it as the content of our activity.
		
		setContentView(R.layout.activity_yodo_face);
		mPreview = (Preview) this.findViewById(R.id.preview);

		status = (TextView) this.findViewById(R.id.status);
		status2 = (TextView) this.findViewById(R.id.status2);
		mPreview.setTextViews(status, status2);
		
		mPreview.setKeepScreenOn(true);

		//pManager = (PowerManager) this.getSystemService(Activity.POWER_SERVICE);
		//wLock = pManager.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "FaceAPITest");

		// Find the total number of cameras available
		numberOfCameras = Camera.getNumberOfCameras();  

		// Find the ID of the default camera
		CameraInfo cameraInfo = new CameraInfo();
		for (int i = 0; i < numberOfCameras; i++) {
			Camera.getCameraInfo(i, cameraInfo);
			if (cameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT) {
				defaultCameraId = i;
			}
		}
		
		Bundle extras = getIntent().getExtras();
		if(extras != null) {
			token = extras.getString(YodoGlobals.ID_TOKEN);
			mPreview.setFace(token);
		} else {
			AlertDialog.Builder helper = new AlertDialog.Builder(this);  
			helper.setMessage(getString(R.string.start_biometric));            
			helper.setPositiveButton(getString(R.string.ok), null);  
         
			helper.show(); 
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		// Open the default i.e. the first front facing camera.
		mCamera = Camera.open(defaultCameraId);

		cameraCurrentlyLocked = defaultCameraId;
		mPreview.setCamera(mCamera);

		//wLock.acquire();

	}

	@Override
	protected void onPause() {
		super.onPause();

		//wLock.release();

		// Because the Camera object is a shared resource, it's very
		// important to release it when the activity is paused.
	
		
		if (mCamera != null) { 
		
			// Try to stop frame processing thread nicely before stopping camera
			mPreview.mFrameCallback.input.mStopRequest=1;
			mPreview.mFrameCallback.thread.interrupt();
			while (mPreview.mFrameCallback.input.mStopRequest<2)
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {

				} 

			
			mPreview.setCamera(null);
			mCamera.release();
			mCamera = null;
		}
	}

}

// ----------------------------------------------------------------------




/*
 * The callback class for camera preview
 */

class FrameCallback implements Camera.PreviewCallback {
	List<byte[]> callbackBuffer=new ArrayList<byte[]>();
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
	
	FrameCallback(Camera camera, TextView statusView, TextView statusView2, Size previewSize, Context ctx){


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
		try
		{
			queue.add(data);
			
			/*int camFPS = (int) (0.5f+1000.0f/input.mCameraFPS);
			int processingFPS = (int) (0.5f+1000.0f/input.mInternalFPS);
			
			int freq = 10*(int)(0.5+input.mFrequency/10000);
			
			// Show some status messages on the screen
			input.mStatus2.setText(input.mNbrOfItems + " faces saved. Camera FPS: " + camFPS + " Processing FPS: " + processingFPS + " @ " + freq + "MHz " + versionString);*/
			
			if(input.mEnrollFlag == true)
			{
				input.mStatus.setText("ENROLL STATE: " + input.enrollState);
			}
			else if(input.mResult != null)
			{
				String a = "VERIFY STATE: ";
				String b = input.mResult.faceRecognition.toString();
				String c = " LIVENESS STATE: ";
				String d = input.mResult.livenessDetection.toString();
				input.mStatus.setText(a + b + c + d, BufferType.SPANNABLE);
				//input.mStatus.setText(a + b, BufferType.SPANNABLE);
				Spannable s = (Spannable)input.mStatus.getText();
				
				// Set color for verification (green allow, red deny)
				int start = a.length();
				int end = start + b.length();
				
				if(input.mResult.faceRecognition == VerificationAPI.VerifyState.ALLOW) {
					s.setSpan(new ForegroundColorSpan(0xFF00FF00), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					Intent resultIntent = new Intent();
					((Activity)ctx).setResult(Activity.RESULT_OK, resultIntent);
					((Activity)ctx).finish();
				}
				else
					s.setSpan(new ForegroundColorSpan(0xFFFF0000), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

				
				// Set color for liveness (green allow, red deny)
				start = a.length() + b.length() + c.length();
				end = start + d.length();
				/*start = a.length();
				end = start + b.length();*/
				if(input.mResult.livenessDetection == VerificationAPI.VerifyState.ALLOW)
					s.setSpan(new ForegroundColorSpan(0xFF00FF00), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				else
					s.setSpan(new ForegroundColorSpan(0xFFFF0000), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				
				//input.mStatus.setText("VERIFY STATE: " + input.mResult.faceRecognition + " LIVENESS STATE: " + input.mResult.livenessDetection);
			}

		}
		catch (InterruptedException exception)
		{
		}

	}


}


/*
 * This class is a container of data (exhange between analysis thread and application)
 */
class InputStruct{
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
	public EnrollState enrollState;
}


//----------------------------------------------------------------------

class CameraFrameAnalyzer implements Runnable {
	/*!< DEBUG */
	private final static boolean DEBUG = false;
	
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

	// Example to show how to read enrolled face template and
	// how to load it from the data buffer to internal database
	private boolean saveTemplateDemo = true;
	
	public CameraFrameAnalyzer(SynchronizedQueue<byte[]> aQueue, InputStruct aInput, Context ctx) {
		queue = aQueue;
		camera=aInput.mCamera;		 
		input = aInput;
		this.ctx = ctx;
	}

	public void run() {
		int succesivenonzeos=0;
		int extraremoveperframe=0;
		try {
			while (input.mStopRequest == 0) {

				// Check frame queue size and determine if there are too many frames 
				// pending.
				
				if (queue.size==0)
				{
					succesivenonzeos=0;
					extraremoveperframe--;
				}
				else
					succesivenonzeos++;

				if (succesivenonzeos>10)
				{
					succesivenonzeos=0;
					extraremoveperframe++;
				}

				if (extraremoveperframe>(queue.size-1))
					extraremoveperframe=(queue.size-1);

				if (extraremoveperframe<0)
					extraremoveperframe=0;

				for (int j=0;j<extraremoveperframe;j++)
				{
					// remove frames from queue to avoid latency 
					byte[] data = queue.remove();
					camera.addCallbackBuffer(data);
					counter++;
				}
	
				// Get frame data from queue
				byte[] data = queue.remove();

				if (input.mStopRequest==0)
				{
					analyzeFrame(data);
					counter++;
					camera.addCallbackBuffer(data);
				}

			}
		} catch (InterruptedException exception) {
		}
		input.mStopRequest=2;		
	}

	/*
	 * Helper method for getting the current CPU operating frequency.
	 */
	public int getFrequency()
	{
		int currentFreq=-1;
		File file = new File("/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq");

		//Read text from file
		//StringBuilder text = new StringBuilder();

		try {
			@SuppressWarnings("resource")
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;

			while ((line = br.readLine()) != null) {
				//Log.i("Facet","CPU freq "+line);
				currentFreq=Integer.parseInt(line);
			}
		} catch (IOException e) {
			//You'll need to add proper error handling here
			//Log.i("Facet","homma kusee");
		}

		return currentFreq;
	}

	/*
	 * Helper method for getting the maximum CPU frequency.
	 */
	public int getMaxFrequency()
	{
		int currentFreq=-1;
		File file = new File("/sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq");

		//Read text from file
		//StringBuilder text = new StringBuilder();

		try {
			@SuppressWarnings("resource")
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;

			while ((line = br.readLine()) != null) {
				currentFreq=Integer.parseInt(line);
			}
		} catch (IOException e) {
			//You'll need to add proper error handling here
		}

		return currentFreq;
	}

	/* Main method to apply facial image analysis for input frames
	 */
	public void analyzeFrame(byte[] data)
	{
		long t1 = SystemClock.elapsedRealtime();

		// User has requested DB reset (long touch event)
		if(input.mResetFlag){

			// Reset face database.
			ResetState state = VerificationAPI.resetDatabase();
			Log.d("FaceAPI", "ResetState: " + state);

			input.mResetFlag = false;

			input.mNbrOfItems = VerificationAPI.getNbrOfDBItems();
		}
		// User has requested enrolling (touch event)
		else if(input.mEnrollFlag){

			// Enroll the face in the image. The NV12 image format holds gray level information at the start of
			// the buffer, so it can be directly given to the method.
			input.enrollState = VerificationAPI.enrollFace(data);			

			// Example to show how to get the enrolled face template data
			// and how to load data from template to internal database 
			if(saveTemplateDemo == true) {
				if(input.enrollState==VerificationAPI.EnrollState.DONE){				
					input.mEnrollFlag = false;
					input.mNbrOfItems = VerificationAPI.getNbrOfDBItems();
					
					input.mEnrollFlag = false;
					input.mNbrOfItems = VerificationAPI.getNbrOfDBItems();	
					byte [] faceTemplate = VerificationAPI.getEnrolledFaceTemplate();
					// save template somewhere
					String image_str = Utils.bytesToHex(faceTemplate);
					
					if(DEBUG)
						Log.e("Data", image_str);
					
					Intent resultIntent = new Intent();
					resultIntent.putExtra(YodoGlobals.FACE_DATA, image_str);
					((Activity)ctx).setResult(Activity.RESULT_OK, resultIntent);
					((Activity)ctx).finish();
					
					Log.i("FaceAPI","face template obtained, size = " + faceTemplate.length);
					Log.i("FaceAPI", "deleting face database");
					// delete recently enrolled face
					ResetState resState = VerificationAPI.resetDatabase();
				
					Log.i("FaceAPI","reset state = " + resState);
					
					// load template
					LoadState lstate = VerificationAPI.loadFaceTemplate(faceTemplate);
					Log.i("FaceAPI","load face template state = " + lstate);
					input.mNbrOfItems = VerificationAPI.getNbrOfDBItems();	 
					 
				}
			}
			else {				
				if(input.enrollState==VerificationAPI.EnrollState.DONE){				
					input.mEnrollFlag = false;
					input.mNbrOfItems = VerificationAPI.getNbrOfDBItems();				
				}
			}
			
		}else{ // Otherwise apply verification

			// Verify the face in the image. The NV12 image format holds gray level information at the start of
			// the buffer, so it can be directly given to the method.		
			//Log.e("data", token);
			//data = YodoUtils.hexToBytes(token);
			VerificationAPI.verifyFace(data);

			// Get specific information about face verification
			FaceInfo info = VerificationAPI.getFaceInfo();

			input.mResult = info;
		}

		// Calculate some additional status information such as
		// a) camera FPS
		// b) internal face engine processing fps
		// c) current CPU frequency
		
		long t2 = SystemClock.elapsedRealtime();
		long time = (t2 - t1);   
		totaltime+=time;

		frameNumber++;

		if (frameNumber<2)
		{
			input.mInternalFPS=30.0f;
			starttime=t1;
		}

		// do some filtering to FPS and frequency values
		
		input.mInternalFPS=(float)(0.95f*input.mInternalFPS)+(float)(0.05f*time);
		
		int currentFrequency=getFrequency();
		if (frameNumber<2)
			input.mCameraFPS=30.0f;
		else
			input.mCameraFPS=(float)(0.95f*input.mCameraFPS)+(float)(0.05f*(t2-lasttime));
		lasttime=t2;
		input.mFrequency=(int) (0.95*input.mFrequency+0.05*currentFrequency);
		
	}
}

/* Helper class for containing frames. Frames got from camera are put to the queue, and
   the same queue is read by face analysis thread.
*/
class SynchronizedQueue<V> {

	private Object[] elements;

	private int head;

	private int tail;

	public int size;

	public SynchronizedQueue(int capacity) {
		elements = new Object[capacity];
		head = 0;
		tail = 0;
		size = 0;
	}

	public synchronized V remove() throws InterruptedException {
		while (size == 0)		
			wait();

		if (size==0)
			return null;
		@SuppressWarnings("unchecked")
		V r = (V) elements[head];
		head++;
		size--;
		if (head == elements.length)
			head = 0;
		notifyAll();
		return r;
	}

	public synchronized void add(V newValue) throws InterruptedException {
		while (size == elements.length)
		{
			wait();
		}
		elements[tail] = newValue;
		tail++;
		size++;
		if (tail == elements.length)
			tail = 0;
		notifyAll();
	}

}
//----------------------------------------------------------------------



