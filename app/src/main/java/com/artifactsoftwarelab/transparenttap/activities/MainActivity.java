package com.artifactsoftwarelab.transparenttap.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.artifactsoftwarelab.transparenttap.R;

public class MainActivity extends AppCompatActivity {
    private static String TAG = "MainActivity";
    private ImageView imageViewCar, imageViewBuddy, imageViewRobot;
    private RelativeLayout relativeLayout;
    private TextView textView;
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = new TextView(this);
        button = (Button) findViewById(R.id.button);

        relativeLayout = findViewById(R.id.relative);
        relativeLayout.setDrawingCacheEnabled(true);
        relativeLayout.buildDrawingCache(true);

        imageViewRobot = findViewById(R.id.imageViewRobot);

        imageViewRobot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClickUp: robot");
            }
        });


        imageViewCar = findViewById(R.id.imageViewCar);

        imageViewCar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClickUp: car");
            }
        });

        imageViewBuddy = (ImageView) findViewById(R.id.imageViewBuddy);

        imageViewBuddy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClickUp: buddy");
            }
        });

        TransparentDetector mainActivityPresenter = new TransparentDetector();
        mainActivityPresenter.addView(imageViewCar);
        mainActivityPresenter.addView(imageViewBuddy);
        mainActivityPresenter.addView(imageViewRobot);
        mainActivityPresenter.build();

        mainActivityPresenter.handShakeListener(new TransparentDetector.OnDetectListener() {
            @Override
            public void onClickUp(View view) {
                view.performClick();
            }

            @Override
            public void onLongClick(View view) {
                Log.d(TAG, "onLongClick: True");
            }

            @Override
            public void onClickDown(View view) {
                Log.d(TAG, "onClickDown: True");
            }
        });

    }

}
