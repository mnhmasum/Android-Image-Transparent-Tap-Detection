package com.artifactsoftwarelab.transparenttap.activities;

import android.graphics.Color;
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

        textView = findViewById(R.id.textView);
        button = (Button) findViewById(R.id.button);

        relativeLayout = findViewById(R.id.relative);
        relativeLayout.setDrawingCacheEnabled(true);
        relativeLayout.buildDrawingCache(true);

        ImageView iv = new ImageView(this);
        iv.setBackgroundColor(Color.rgb(0, 0, 0));

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(30, 40);
        params.leftMargin = 446;
        params.topMargin = 352;
        relativeLayout.addView(iv, params);

        imageViewRobot = findViewById(R.id.imageViewRobot);

        imageViewRobot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: robot");
            }
        });


        imageViewCar = findViewById(R.id.imageViewCar);

        imageViewCar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: car");
            }
        });

        imageViewBuddy = (ImageView) findViewById(R.id.imageViewBuddy);
        imageViewBuddy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: buddy");
            }
        });

        TransparentDetector transparentDetector = new TransparentDetector();
        transparentDetector.addView(imageViewCar);
        transparentDetector.addView(imageViewBuddy);
        transparentDetector.addView(imageViewRobot);
        transparentDetector.build();

        transparentDetector.handShakeListener(new TransparentDetector.OnDetectListener() {
            @Override
            public void onClickUp(View view) {
                view.performClick();
                textView.setText("onClickUp: True " + view.getTag().toString() );
                Log.d(TAG, "onClickUp: True " + view.getTag().toString());
            }

            @Override
            public void onLongClick(View view) {
                textView.setText("onLongClick: True " + view.getTag().toString() );
                Log.d(TAG, "onLongClick: True " + view.getTag().toString());

            }

            @Override
            public void onClickDown(View view) {
                textView.setText("onClickDown: True " + view.getTag().toString() );
                Log.d(TAG, "onClickDown: True " + view.getTag().toString());
            }
        });

    }

}
