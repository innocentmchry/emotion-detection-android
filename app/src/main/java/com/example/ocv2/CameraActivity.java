package com.example.ocv2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.util.HashMap;

public class CameraActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2{
    private static final String TAG="MainActivity";

    private Mat mRgba;
    private Mat mGray;
    private CameraBridgeViewBase mOpenCvCameraView;

    private facialExpressionRecognition facialExpressionRecognition;
    private BaseLoaderCallback mLoaderCallback =new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status){
                case LoaderCallbackInterface
                        .SUCCESS:{
                    Log.i(TAG,"OpenCv Is loaded");
                    mOpenCvCameraView.enableView();
                }
                default:
                {
                    super.onManagerConnected(status);

                }
                break;
            }
        }
    };

    private HashMap<String, Integer> emotionCountMap = new HashMap<>();

    private static final int FRAMES_INTERVAL = 15;

    private int frameCounter = 0;

    //private String maxEmotionDuringInterval = "";

    TextView txtEmotion1;
    TextView txtEmotion2;
    TextView txtEmotion3;
    ProgressBar pb1;
    ProgressBar pb2;
    ProgressBar pb3;



    private int maxCount = 0;
    private String firstMaxEmotion = "";
    private String secondMaxEmotion = "";
    private String thirdMaxEmotion = "";
    //counter to stop 3 max emotions
    private int count=0;

    public CameraActivity(){
        Log.i(TAG,"Instantiated new "+this.getClass());
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        int MY_PERMISSIONS_REQUEST_CAMERA=0;
        // if camera permission is not given it will ask for it on device
        if (ContextCompat.checkSelfPermission(CameraActivity.this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(CameraActivity.this, new String[] {Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
        }

        setContentView(R.layout.activity_camera);

        mOpenCvCameraView=(CameraBridgeViewBase) findViewById(R.id.frame_surface);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCvCameraView.setCameraIndex(1);

        try{
            //input size of model is 48
            int inputSize = 48;
            facialExpressionRecognition = new facialExpressionRecognition(getAssets(), CameraActivity.this, "model300.tflite", inputSize);
        }
        catch (IOException e){
            e.printStackTrace();
        }

        txtEmotion1 = findViewById(R.id.textView1);
        txtEmotion2 = findViewById(R.id.textView2);
        txtEmotion3 = findViewById(R.id.textView3);

        pb1 = (ProgressBar)findViewById(R.id.pb1);
        pb2 = (ProgressBar)findViewById(R.id.pb2);
        pb3 = (ProgressBar)findViewById(R.id.pb3);

        pb1.setVisibility(firstMaxEmotion.equals("") ? View.GONE : View.VISIBLE);
        pb2.setVisibility(secondMaxEmotion.equals("") ? View.GONE : View.VISIBLE);
        pb3.setVisibility(thirdMaxEmotion.equals("") ? View.GONE : View.VISIBLE);


    }
    @Override
    protected void onResume() {
        super.onResume();
        if (OpenCVLoader.initDebug()){
            //if load success
            Log.d(TAG,"Opencv initialization is done");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
        else{
            //if not loaded
            Log.d(TAG,"Opencv is not loaded. try again");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0,this,mLoaderCallback);
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (mOpenCvCameraView !=null){
            mOpenCvCameraView.disableView();
        }
    }
    public void onDestroy(){
        super.onDestroy();
        if(mOpenCvCameraView !=null){
            mOpenCvCameraView.disableView();
        }

    }
    public void onCameraViewStarted(int width ,int height){
        mRgba=new Mat(height,width, CvType.CV_8UC4);
        mGray =new Mat(height,width,CvType.CV_8UC1);
    }
    public void onCameraViewStopped(){
        mRgba.release();
    }
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame){
        frameCounter++;
        mRgba=inputFrame.rgba();
        mGray=inputFrame.gray();
        Core.flip(mRgba, mRgba, 0);
        mRgba=facialExpressionRecognition.recognizeImage(mRgba);
        String currEmotion = facialExpressionRecognition.curr_emotion_s;
        updateEmotionCount(currEmotion);

        if(frameCounter == FRAMES_INTERVAL){
            frameCounter = 0;
            firstMaxEmotion = getMaxEmotionDuringInterval();
            pb1.setProgress(maxCount * 100 / FRAMES_INTERVAL);
            secondMaxEmotion = getMaxEmotionDuringInterval();
            pb2.setProgress(maxCount * 100 / FRAMES_INTERVAL);
            thirdMaxEmotion = getMaxEmotionDuringInterval();
            pb3.setProgress(maxCount * 100 / FRAMES_INTERVAL);

//            pb1.setVisibility(firstMaxEmotion.equals("") ? View.VISIBLE : View.GONE);
//            pb2.setVisibility(secondMaxEmotion.equals("") ? View.VISIBLE : View.GONE);
//            pb3.setVisibility(thirdMaxEmotion.equals("") ? View.VISIBLE : View.GONE);


            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    if (firstMaxEmotion != null) {
                        pb1.setVisibility(firstMaxEmotion.equals("") ? View.GONE : View.VISIBLE);
                    }
                    if (secondMaxEmotion != null) {
                        pb2.setVisibility(secondMaxEmotion.equals("") ? View.GONE : View.VISIBLE);
                    }
                    if (thirdMaxEmotion != null) {
                        pb3.setVisibility(thirdMaxEmotion.equals("") ? View.GONE : View.VISIBLE);
                    }

//                    pb1.setVisibility(firstMaxEmotion.equals("") ? View.GONE : View.VISIBLE);
//                    pb2.setVisibility(secondMaxEmotion.equals("") ? View.GONE : View.VISIBLE);
//                    pb3.setVisibility(thirdMaxEmotion.equals("") ? View.GONE : View.VISIBLE);


                    txtEmotion1.setText(firstMaxEmotion);
                    txtEmotion2.setText(secondMaxEmotion);
                    txtEmotion3.setText(thirdMaxEmotion);
                }
            });
        }




        return mRgba;
    }
    public void updateEmotionCount(String emotion){
        emotionCountMap.put(emotion, emotionCountMap.getOrDefault(emotion, 0)+1);
    }
    private String getMaxEmotionDuringInterval(){
        count++;
        maxCount = 0;
        String maxEmotion = "";
        for (String emotion : emotionCountMap.keySet()) {
            int count = emotionCountMap.get(emotion);
            if (count > maxCount) {
                maxCount = count;
                maxEmotion = emotion;
            }
        }
        emotionCountMap.remove(maxEmotion);
        if(count == 3) {
            emotionCountMap.clear();
        }
        return maxEmotion;
    }
}