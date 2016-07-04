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
  
package co.yodo.mobile.ui;

import java.io.IOException;
import java.util.List;

import co.yodo.mobile.helper.CryptUtils;
import co.yodo.mobile.helper.PrefUtils;
import co.yodo.mobile.helper.SystemUtils;
import visidon.Lib.Parameters;
import visidon.Lib.VerificationAPI;
import visidon.Lib.VerificationAPI.InitState;
import visidon.Lib.VerificationAPI.LoadState;
import visidon.Lib.VerificationAPI.ReleaseState;
import visidon.Lib.VerificationAPI.ResetState;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

/**
 * A simple wrapper around a Camera and a SurfaceView that renders a centered preview of the Camera
 * to the surface. We need to center the SurfaceView because not all devices have cameras that
 * support preview sizes at the same aspect ratio as the device's display.
 */
public class CameraPreview extends ViewGroup implements SurfaceHolder.Callback {
    /** DEBUG */
	private final static String TAG = CameraPreview.class.getSimpleName();

	SurfaceView mSurfaceView;
	SurfaceHolder mHolder;
	Size mPreviewSize;
	List<Size> mSupportedPreviewSizes;
    private Integer mCameraId;
	private Camera mCamera;
	private Context mContext;
	public TextView mStatusView;
	public TextView mStatusView2;
	private String token;

	public FrameCallback mFrameCallback;

	public CameraPreview(Context context, AttributeSet attrs) {
		super(context, attrs);
		// Simple UI configuration. Long press on the preview screen will reset the database and 
		// short press will start enroll mode. 

		mSurfaceView = new SurfaceView( context );
		mSurfaceView.setOnLongClickListener(new OnLongClickListener(){

			public boolean onLongClick(View v) {
				mFrameCallback.input.mResetFlag = true;
				return true;
			}

		});

		mSurfaceView.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				mFrameCallback.input.mEnrollFlag = true;
			}
		});

		addView( mSurfaceView );
		mContext = context;

		// Install a SurfaceHolder.Callback so we get notified when the
		// underlying surface is created and destroyed.
		mHolder = mSurfaceView.getHolder();
		mHolder.addCallback( this );
        mHolder.setType( SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS );
	}

	public void setCamera(Camera camera, Integer cameraId) {
        mCameraId = cameraId;
		mCamera = camera;
		if( mCamera != null ) {
			mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
			requestLayout();
		}
	}

	public void setTextViews(TextView aStatus, TextView aStatus2) {
		mStatusView = aStatus;
		mStatusView2 = aStatus2;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// We purposely disregard child measurements because act as a
		// wrapper to a SurfaceView that centers the camera preview instead
		// of stretching it.
		final int width = resolveSize( getSuggestedMinimumWidth(), widthMeasureSpec );
		final int height = resolveSize( getSuggestedMinimumHeight(), heightMeasureSpec );

		setMeasuredDimension( width, height );

		if( mSupportedPreviewSizes != null ) {
			mPreviewSize = getOptimalPreviewSize( mSupportedPreviewSizes, width, height );
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		if( changed && getChildCount() > 0 ) {
			final View child = getChildAt( 0 );

			final int width = r - l;
			final int height = b - t;

			int previewWidth = width;
			int previewHeight = height;
			if( mPreviewSize != null ) {
				previewWidth = mPreviewSize.width;
				previewHeight = mPreviewSize.height;
			}

			// Center the child SurfaceView within the parent.
			if( width * previewHeight > height * previewWidth ) {
				final int scaledChildWidth = previewWidth * height / previewHeight;
				child.layout( ( width - scaledChildWidth ) / 2, 0,
						      ( width + scaledChildWidth ) / 2, height );
			} else {
				final int scaledChildHeight = previewHeight * width / previewWidth;
				child.layout( 0, ( height - scaledChildHeight ) / 2,
						  width, ( height + scaledChildHeight ) / 2 );
			}
		}
	}

	public void surfaceCreated(SurfaceHolder holder) {
		// The Surface has been created, acquire the camera and tell it where
		// to draw.
		try {
			if( mCamera != null ) {
				mCamera.setPreviewDisplay( holder );
			}
		} catch (IOException exception) {
			SystemUtils.Logger( TAG, "IOException caused by setPreviewDisplay() : " + exception.getMessage() );
		}
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		// Surface will be destroyed when we return, so stop the preview.
		if( mCamera != null ) {
			mCamera.stopPreview();
		}

		// Release FaceAPI resources
		ReleaseState state = VerificationAPI.release();
		SystemUtils.Logger( TAG, "ReleaseState: " + state );
	} 

	private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
		final double ASPECT_TOLERANCE = 0.1;
		double targetRatio = (double) w / h;
		if( sizes == null ) return null;

		Size optimalSize = null;
		double minDiff = Double.MAX_VALUE;

        // Try to find an size match aspect ratio and size
		for( Size size : sizes ) {
			double ratio = (double) size.width / size.height;
			if( Math.abs( ratio - targetRatio ) > ASPECT_TOLERANCE ) continue;
			if( Math.abs( size.height - h ) < minDiff ) {
				optimalSize = size;
				minDiff = Math.abs( size.height - h );
			}
		}

		// Cannot find the one match the aspect ratio, ignore the requirement
		if( optimalSize == null ) {
			minDiff = Double.MAX_VALUE;
			for( Size size : sizes ) {
				if( Math.abs( size.height - h ) < minDiff ) {
					optimalSize = size;
					minDiff = Math.abs( size.height - h );
				}
			}
		} 
		return optimalSize;
	} 

	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		// Now that the size is known, set up the camera parameters and begin
		// the preview.
		Camera.Parameters parameters = mCamera.getParameters();
		parameters.setPreviewSize( mPreviewSize.width, mPreviewSize.height );
		requestLayout();

		// Initialize VerifyAPI resources.

		//1. Create parameters object
		Parameters params = new Parameters( mContext );

		//2. You MUST set the image size properly before passing parameters forward
		params.imageHeight = mPreviewSize.height;
		params.imageWidth = mPreviewSize.width;

		//3. Set all other settings you wish to change. (Optional)
		params.securityLevel = VerificationAPI.SecurityLevel.MEDIUM;
		params.livenessDetection = VerificationAPI.LivenessDetection.LOW;

		SystemUtils.Logger( TAG, "Initializing... " );
		SystemUtils.Logger( TAG, "Preview size: width = " + params.imageWidth + " height = " + params.imageHeight );

		//4. Initialize API 
		InitState state = VerificationAPI.initialize( params );

		SystemUtils.Logger( TAG, "InitState: " + state );
		SystemUtils.Logger( TAG, "Version string: " + VerificationAPI.getVersion() );
		
		// Create frame callback object and setup it with a camera
		// In this example UI components that are updated with face analysis information
		// are passed to the FrameCallback object
		mFrameCallback = new FrameCallback( mCamera, mStatusView, mStatusView2, mPreviewSize, getContext() );
		mCamera.setPreviewCallbackWithBuffer( mFrameCallback );
		 
		if( token != null ) {
			updateDB();
		}

        // Get camera data to rotate image if necessary
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo( mCameraId, info );

        WindowManager windowManager = (WindowManager) getContext().getSystemService( Context.WINDOW_SERVICE );
        int rotation = windowManager.getDefaultDisplay().getRotation();

        int degrees = 0;

        switch( rotation ) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        int result;
        if( info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT ) {
            result = ( info.orientation + degrees ) % 360;
            result = ( 360 - result ) % 360;  // compensate the mirror
        } else {  // back-facing
            result = ( info.orientation - degrees + 360 ) % 360;
        }
        mCamera.setDisplayOrientation( result );

        // Try to set camera fps to 30
		//parameters.setPreviewFpsRange(30000, 30000);
		// Apply desired camera parameters
		mCamera.setParameters( parameters );
		
		// Start camera preview
		mCamera.startPreview();

		SystemUtils.Logger( TAG, "Preview started" );
	}
	
	public void setFace(String token) {
		this.token = token;
		mSurfaceView.setOnLongClickListener( null );
		mSurfaceView.setOnClickListener( null );
	}
	
	private void updateDB() {
		mFrameCallback.input.mEnrollFlag = false;
		mFrameCallback.input.mNbrOfItems = VerificationAPI.getNbrOfDBItems();
		byte[] faceTemplate = CryptUtils.hexToBytes( token );

		SystemUtils.Logger( TAG, "face template obtained, size = " + faceTemplate.length );
		// save template somewhere
		SystemUtils.Logger( TAG, "deleting face database" );
		// delete recently enrolled face
		ResetState resState = VerificationAPI.resetDatabase();
		SystemUtils.Logger( TAG, "reset state = " + resState );
		
		// load template
		LoadState lstate = VerificationAPI.loadFaceTemplate(faceTemplate);
		SystemUtils.Logger( TAG, "load face template state = " + lstate );
		mFrameCallback.input.mNbrOfItems = VerificationAPI.getNbrOfDBItems();	 
	}
}


