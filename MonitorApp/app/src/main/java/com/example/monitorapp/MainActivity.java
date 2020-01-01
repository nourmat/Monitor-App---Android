package com.example.monitorapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.Serializable;

/** Steps for setting up the camera
 * 1- Create the Texture View and the Layout
 * 2- Setting up Surface TextureListner to set up the TextureView Object
 * 3- getting the camera ID in our case we will need for now the rear camera
 * 4- setting up a thread to not stop the main thread
 * 5- connect camera
 * 6- prepare a request session to start previewing the image
 ***/
public class MainActivity extends AppCompatActivity implements Serializable{

    public static final String MESSAGETAG = "OKOK";

    public static int mPort = 12000;
    public static String mAddr = "192.168.43.121";
//    public static String mAddr = "192.168.2.35";
//    private boolean mIsRecording = false;

    //Thread used to handle sending data in background
    private static Thread mSendDataThread;
    private static Boolean mKeepSendingDataRunning = false;

    private final static String BEAT = "check";
    private static Thread mCheckConnectionThread;
    private static Boolean mWantToConnect = false;

    private static RearCamera rearCamera;
    public static TCPConnector mConnector;

    private static ImageButton btn_video,btn_camera,btn_settings;
    private static TextureView mTextureView;
    private TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() { /** AS texture view takes some time to start... using this lister to initialize it **/
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
        Toast.makeText(getApplicationContext(),"Texture is available", Toast.LENGTH_SHORT).show();
        rearCamera.setupCamera();
        rearCamera.connectCamera();
        }
        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
        }
        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            return false;
        }
        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextureView = findViewById(R.id.textureView);
        rearCamera = new RearCamera(mTextureView,this);

        mConnector = new TCPConnector(getApplicationContext(), mAddr, mPort);

        btn_video = findViewById(R.id.btn_video);
        btn_camera = findViewById(R.id.btn_camera);
        btn_settings = findViewById(R.id.btn_settings);

        btn_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),SettingsActivity.class);
                startActivity(intent);
            }
        });

        btn_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mKeepSendingDataRunning){
                    btn_video.setImageResource(R.mipmap.video_on);
                    stopSendingDataHandler();
                }else if (mConnector != null && mConnector.isConnected()){
                    btn_video.setImageResource(R.mipmap.video_off);
                    startSendingDataHandler();
                }
            }
        });

        btn_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    private static void startSendingDataHandler() {
        stopSendingDataHandler();
        mKeepSendingDataRunning = true;
        mSendDataThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (mKeepSendingDataRunning) {
                    try {
                        byte[] imageBytes = rearCamera.getImagesFromTextureView();
                        mConnector.sendData(imageBytes);
                        Thread.sleep(5);
                    }catch (InterruptedException e){
                        break;
                    }
                }
            }
        });
        mSendDataThread.start();
    }

    private static void stopSendingDataHandler(){
        mKeepSendingDataRunning = false;
        if (mSendDataThread != null) {
            mSendDataThread.interrupt();
            mSendDataThread = null;
        }
    }

    /**
     * Still not working properly
     */
    private static void startHeartBeatCheckerHandler(){
        stopHeartBeatCheckerHandler();
        mCheckConnectionThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (mWantToConnect) {
                        mConnector.receiveData();
                    try {
                        Thread.sleep(2000);
                    }catch (InterruptedException e){
                        break;
                    }
                }
            }
        });
        mCheckConnectionThread.start();
    }

    private static void stopHeartBeatCheckerHandler(){
        if (mCheckConnectionThread != null) {
            mCheckConnectionThread.interrupt();
            mCheckConnectionThread = null;
        }
    }

    /**
     * used in other activity to control connection
     */
    public static void startConnection(){
        try {
            mConnector.connectToSocket(mAddr ,mPort);
//            Thread.sleep(100);
//            startHeartBeatCheckerHandler();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void endConnection (){
        btn_video.setImageResource(R.mipmap.video_on);
        stopSendingDataHandler();
        stopHeartBeatCheckerHandler();
        if (mConnector != null)
            mConnector.closeConnection();
    }
    /** Checks when app starts if texture view is avilable or not **/
    @Override
    protected void onResume() {
        super.onResume();
        rearCamera.startBackgroundThread();
        if (mTextureView.isAvailable()) {
            rearCamera.setupCamera();
            rearCamera.connectCamera();
        }
        else
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
    }

    @Override
    protected void onPause() {
        rearCamera.closeCamera();
        rearCamera.stopBackgroundThread();
        super.onPause();
    }

    /**
     * used to check if the camera permission is granted or not
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == rearCamera.REQUEST_CAMERA_PERMISSION_RESULT){
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED){ /** if request is rejected **/
                Toast.makeText(getApplicationContext(),"Application Will not run without Camera Thank you", Toast.LENGTH_LONG).show();
            }
        }
    }
}