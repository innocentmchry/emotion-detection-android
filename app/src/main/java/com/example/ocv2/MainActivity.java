package com.example.ocv2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;

public class MainActivity extends AppCompatActivity {

    static {
        if(OpenCVLoader.initDebug()){
            Log.d("MainActivity", "Opencv is loaded man");
        }else{
            Log.d("MainActivity", "OpenCV failed to Load");
        }
    }
    private Button btnCamera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
//        if(OpenCVLoader.initDebug()){
//            Toast.makeText(MainActivity.this, "Open CV success", Toast.LENGTH_SHORT).show();
//        }
//        else{
//            Toast.makeText(MainActivity.this, "Failed to load Open CV", Toast.LENGTH_SHORT).show();
//        }
        btnCamera = findViewById(R.id.buttonCamera);
        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, CameraActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });
    }
}