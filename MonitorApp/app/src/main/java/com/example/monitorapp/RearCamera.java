package com.example.monitorapp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

public class RearCamera {

    private TextureView mTextureView; //texture view to preview
    private Context mContext; //main activity context

    private String mCameraID;
    private CameraDevice mCameraDevice;
    private CameraDevice.StateCallback mCameraDeviceStateCallBack = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice cameraDevice) {
            mCameraDevice = cameraDevice;
            Toast.makeText(mContext, "We are Fully connected with your camera", Toast.LENGTH_LONG).show();
            startPreview();
        }

        @Override
        public void onDisconnected(CameraDevice cameraDevice) {
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(CameraDevice cameraDevice, int error) {
            cameraDevice.close();
            mCameraDevice = null;
        }
    };

    private HandlerThread mBackgroundHandlerThread;
    private Handler mBackgroundHandler;
    public static final int REQUEST_CAMERA_PERMISSION_RESULT = 0;
    private CaptureRequest.Builder mCaptureRequestBuilder;

    /**
     * constructor
     *
     * @param mTextureView
     */
    public RearCamera(TextureView mTextureView, Context context) {
        this.mTextureView = mTextureView;
        mContext = context;
    }

    public void setupCamera() {
        CameraManager cameraManager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
        /** loop to get characteristics and choose the back camera setting the camera ID**/
        try {
            for (String cameraId : cameraManager.getCameraIdList()) {
                CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);
                if (cameraCharacteristics.get(cameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT)
                    continue;
                mCameraID = cameraId;
                return;
            }
        } catch (Exception e) {
        }
    }

    public void connectCamera() {
        CameraManager cameraManager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
        try {
            /** needs permision if marchmello or greater**/
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    cameraManager.openCamera(mCameraID, mCameraDeviceStateCallBack, mBackgroundHandler);
                } else { /** user denied permission **/
                    if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) mContext, Manifest.permission.CAMERA)) {
                        Toast.makeText(mContext, "This app requires access to camera", Toast.LENGTH_SHORT).show();
                    }
                    ActivityCompat.requestPermissions((Activity) mContext, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION_RESULT); /** override the onRequestPermission Result to know what the user choose**/
                }
            } else
                cameraManager.openCamera(mCameraID, mCameraDeviceStateCallBack, mBackgroundHandler);
        } catch (Exception e) {
        }
    }

    public void closeCamera() {
        if (mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
    }

    public byte[] getImagesFromTextureView() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Bitmap bitmap = mTextureView.getBitmap(mTextureView.getWidth(), mTextureView.getHeight());
        bitmap.compress(Bitmap.CompressFormat.JPEG, 30, stream);
        return stream.toByteArray();
    }


    public void startPreview() {
        SurfaceTexture surfaceTexture = mTextureView.getSurfaceTexture();
        surfaceTexture.setDefaultBufferSize(mTextureView.getWidth(), mTextureView.getHeight());
        Surface previewSurface = new Surface(surfaceTexture);

        try {
            mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mCaptureRequestBuilder.addTarget(previewSurface);

            mCameraDevice.createCaptureSession(Arrays.asList(previewSurface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                    try {
                        cameraCaptureSession.setRepeatingRequest(mCaptureRequestBuilder.build(), null, mBackgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(mContext, "Unable to setup Camera Preview", Toast.LENGTH_SHORT).show();
                }
            }, null);
        } catch (Exception e) {
        }
    }

    public void startBackgroundThread() {
        mBackgroundHandlerThread = new HandlerThread("Camera Video");
        mBackgroundHandlerThread.start();
        mBackgroundHandler = new Handler(mBackgroundHandlerThread.getLooper());
    }

    public void stopBackgroundThread() {
        mBackgroundHandlerThread.quitSafely();
        try {
            mBackgroundHandlerThread.join();
            mBackgroundHandlerThread = null;
            mBackgroundHandler = null;
        } catch (Exception e) {
        }
    }
}