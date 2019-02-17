package com.artifactsoftwarelab.transparenttap.activities;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;

import com.artifactsoftwarelab.transparenttap.Utility.MenuButtonBound;
import com.artifactsoftwarelab.transparenttap.Utility.TapedPoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class TransparentDetector {
    private static String TAG = "TransparentDetector";
    private Bitmap bitmap;
    private List<View> views = new ArrayList<>();

    private HashMap<String, View> viewHashMap = new HashMap<>();
    private HashMap<String, MenuButtonBound> menuButtonBoundHashMap = new HashMap<>();

    public TransparentDetector() {

    }

    public void addView(View view) {
        views.add(view);
    }

    public void build() {
        for (View view : views) {
            view.setDrawingCacheEnabled(true);
            view.buildDrawingCache(true);
            setViewByTag(view);
            getAllViewHeight(view);
            setOnTouchListener(view);

        }
    }

    public void setViewByTag(View view) {
        viewHashMap.put(view.getTag().toString(), view);
    }

    private void getPixelColor(MotionEvent motionEvent, View view, String buttonIdentifier, boolean isLongPress) {
        bitmap = view.getDrawingCache();

        int pixel = bitmap.getPixel((int) motionEvent.getX(), (int) motionEvent.getY());
        int red = Color.red(pixel);
        int blue = Color.blue(pixel);
        int green = Color.green(pixel);

        TapedPoint tapedPoint = new TapedPoint((int) motionEvent.getX(), (int) motionEvent.getY());

        Log.d(TAG, "Car Point: " + tapedPoint.getX1() + "," + tapedPoint.getY1());

        int x = (int) (view.getX() + tapedPoint.getX1());
        int y = (int) (view.getY() + tapedPoint.getY1());

        TapedPoint tapedPointActual = new TapedPoint(x, y);

        Log.d(TAG, "Car Point Actual: " + tapedPointActual.getX1() + "," + tapedPointActual.getY1());

        Log.d(TAG, "Red: " + red + " Blue: " + blue + " Green: " + green);


        if (red == 0 && blue == 0 && green == 0) {
            boolean notFound = true;
            for (String key : menuButtonBoundHashMap.keySet()) {
                if (menuButtonBoundHashMap.get(key).isInButtonArea(tapedPointActual)
                        && !key.equalsIgnoreCase(buttonIdentifier)) {
                    Log.d(TAG, "getPixelColor: " + key);
                    notFound = false;
                    chooseListener(getView(key), isLongPress);
                    break;
                }
            }

            if (notFound) {
                chooseListener(view, isLongPress);
            }


        } else {
            Log.d(TAG, "getPixelColor: " + buttonIdentifier);
            chooseListener(view, isLongPress);
        }

    }

    private void chooseListener(View view, boolean isLong) {
        if (isLong) {
            onDetectListener.onLongClick(view);
        } else {
            onDetectListener.onClickUp(view);
        }
    }

    private void getAllViewHeight(final View view) {
        ViewTreeObserver vto = view.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    view.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }

                int width = view.getMeasuredWidth();
                int height = view.getMeasuredHeight();
                int x = (int) view.getX();
                int y = (int) view.getY();

                MenuButtonBound menuButtonBound = new MenuButtonBound(x, y, x + width, y + height);
                menuButtonBoundHashMap.put(view.getTag().toString(), menuButtonBound);
                Log.d(TAG, "onGlobalLayout: " + view.getTag().toString() + ": " + width + ":" + height + "|" + x + "," + y);

            }/**/
        });

    }

    boolean isLongTouchAlive = false;

    public void setOnTouchListener(View view) {
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(final View view, final MotionEvent motionEvent) {

                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        if (isLongTouchAlive) {

                            LongPressData longPressData = new LongPressData();
                            longPressData.motionEvent = motionEvent;
                            longPressData.view = view;
                            longPressData.tag = view.getTag().toString();
                            longPressData.isLongPress = true;

                            Message message = new Message();
                            message.what = 1;
                            message.obj = longPressData;
                            responseHandler.sendMessage(message);

                            //getPixelColor(motionEvent, view, view.getTag().toString(), true);
                        }
                    }
                };

                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    isLongTouchAlive = false;
                    handler.removeCallbacks(r);
                    getPixelColor(motionEvent, view, view.getTag().toString(), false);
                } else if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    isLongTouchAlive = true;
                    onDetectListener.onClickDown(view);
                    handler.postDelayed(r, 2000);

                }

                return true;
            }
        });
    }

    final GestureDetector gestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
        public void onLongPress(MotionEvent e) {
            Log.e("", "Longpress detected");
        }
    });

    final Handler handler = new Handler(Looper.getMainLooper());


    final Handler responseHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {

            if (msg.what == 1) {

                LongPressData longPressData = (LongPressData) msg.obj;

                Log.d(TAG, "handleMessage: " + longPressData.view);
                Log.d(TAG, "handleMessage: " + longPressData.motionEvent);

                getPixelColor(longPressData.motionEvent, longPressData.view, longPressData.view.getTag().toString(), true);

            }
        }
    };

    class LongPressData {
        public MotionEvent motionEvent;
        public View view;
        public String tag;
        public boolean isLongPress;
    }


    private View getView(String tag) {
        return viewHashMap.get(tag);
    }

    public void handShakeListener(OnDetectListener onDetectListener) {
        this.onDetectListener = onDetectListener;
    }

    OnDetectListener onDetectListener;

    interface OnDetectListener {
        void onClickUp(View view);

        void onClickDown(View view);

        void onLongClick(View view);
    }


}
