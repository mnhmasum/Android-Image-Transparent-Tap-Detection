package com.artifactsoftwarelab.transparenttap.activities;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.artifactsoftwarelab.transparenttap.Utility.MenuButtonBound;
import com.artifactsoftwarelab.transparenttap.Utility.TapedPoint;


public class TransparentDetector {
    private static String TAG = "TransparentDetector";

    final Handler handler = new Handler(Looper.getMainLooper());

    private Bitmap bitmap;
    private TapedPoint tapedPoint;

    private long tapStartTime = 0L;
    private boolean isLongTouchAlive = false;
    private boolean detectMultipleView = false;

    private List<View> views = new ArrayList<>();
    private HashMap<String, View> viewHashMap = new HashMap<>();
    private HashMap<String, MenuButtonBound> menuButtonBoundHashMap = new HashMap<>();
    private long longPressDuration = 1000;

    public enum ClickType {
        DOWN,
        UP,
        LONG_PRESS
    }

    public void detectTwoLayerTransparency(boolean detectMultipleView) {
        this.detectMultipleView = detectMultipleView;
    }

    public void addView(View view) {
        views.add(view);
    }

    public void build() {
        for (View view : views) {
            view.setDrawingCacheEnabled(true);
            view.buildDrawingCache(true);
            collectViewByTagAndSaveToHash(view);
            collectViewBoundAreaAndSaveToHash(view);
            setOnTouchListener(view);
        }
    }

    public void collectViewByTagAndSaveToHash(View view) {
        viewHashMap.put(view.getTag().toString(), view);
    }

    private void collectViewBoundAreaAndSaveToHash(final View view) {
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
                //Log.d(TAG, "onGlobalLayout: " + view.getTag().toString() + ": " + width + ":" + height + "|" + x + "," + y);

            }/**/
        });

    }

    private void getPixelColorFindViewAndMakeListener(TapedPoint tapedPoint, View view, ClickType clickType) {
        try {
            bitmap = view.getDrawingCache();
            int pixel = bitmap.getPixel(tapedPoint.getX1(), tapedPoint.getY1());
            int red = Color.red(pixel);
            int blue = Color.blue(pixel);
            int green = Color.green(pixel);
            Log.d(TAG, "Red: " + red + " Blue: " + blue + " Green: " + green);
            //Log.d(TAG, "Car Point: " + tapedPoint.getX1() + "," + tapedPoint.getY1());

            int x = (int) (view.getX() + tapedPoint.getX1());
            int y = (int) (view.getY() + tapedPoint.getY1());

            TapedPoint tapedPointActual = new TapedPoint(x, y);
            //Log.d(TAG, "Car Point Actual: " + tapedPointActual.getX1() + "," + tapedPointActual.getY1());
            detectActualViewAndSelectListener(view, clickType, red, blue, green, tapedPoint, tapedPointActual);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void detectActualViewAndSelectListener(View view, ClickType clickType, int red, int blue, int green, TapedPoint tapedPoint, TapedPoint tapedPointActual) {
        if (red == 0 && blue == 0 && green == 0) {

            for (String key : menuButtonBoundHashMap.keySet()) {
                if (menuButtonBoundHashMap.get(key).isInButtonArea(tapedPointActual) && !key.equalsIgnoreCase(view.getTag().toString())) {

                    if (!detectMultipleView) {
                        chooseListener(getView(key), clickType);
                        break;
                    } else {
                        int x = ((tapedPointActual.getX1() - (int) getView(key).getX()) + 1);
                        int y = ((tapedPointActual.getY1() - (int) getView(key).getY()) + 1);

                        bitmap = getView(key).getDrawingCache();
                        int pixel = bitmap.getPixel(x, y);
                        int red1 = Color.red(pixel);
                        int blue1 = Color.blue(pixel);
                        int green1 = Color.green(pixel);

                        Log.d(TAG, "down image: " + x + "," + y);
                        Log.d(TAG, ">>Red: " + red1 + " Blue: " + blue1 + " Green: " + green1);

                        if (red1 != 0 || blue1 != 0 || green1 != 0) {
                            chooseListener(getView(key), clickType);
                        }

                        break;
                    }
                }
            }

        } else {
            chooseListener(view, clickType);
        }
    }

    private void chooseListener(View view, ClickType clickType) {
        if (clickType == ClickType.LONG_PRESS) {
            onDetectListener.onLongClick(view);
        } else if (clickType == ClickType.UP) {
            onDetectListener.onClickUp(view);
        } else if (clickType == ClickType.DOWN) {
            onDetectListener.onClickDown(view);
        }
    }

    private View getView(String tag) {
        return viewHashMap.get(tag);
    }

    private Rect rect;
    public void setOnTouchListener(View view) {
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(final View view, final MotionEvent motionEvent) {

                Runnable r = getRunnable(view);

                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    handler.removeCallbacks(r);
                    isLongTouchAlive = false;
                    if (tapStartTime == 0l) {
                        onDetectListener.onLongClickUp(view);
                    } else {
                        tapedPoint = new TapedPoint((int) motionEvent.getX(), (int) motionEvent.getY());
                        getPixelColorFindViewAndMakeListener(tapedPoint, view, ClickType.UP);
                    }
                } else if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    view.setPressed(true);
                    Log.d(TAG, "onTouch: " + view.getTag().toString());
                    handler.removeCallbacks(r);
                    handler.postDelayed(r, longPressDuration);
                    tapStartTime = System.currentTimeMillis();

                    isLongTouchAlive = true;
                    tapedPoint = new TapedPoint((int) motionEvent.getX(), (int) motionEvent.getY());
                    getPixelColorFindViewAndMakeListener(tapedPoint, view, ClickType.DOWN);

                    rect = new Rect(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());

                } else if ((motionEvent.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_MOVE) {
                    detectOutSide(view, motionEvent);
                }

                return true;
            }

            private Runnable getRunnable(final View view) {
                return new Runnable() {
                    @Override
                    public void run() {
                        if (isLongTouchAlive) {
                            handler.removeCallbacks(this);
                            fireLongPress();
                        }
                    }

                    private void fireLongPress() {
                        final Handler inner = new Handler(Looper.getMainLooper());
                        inner.post(new Runnable() {
                            @Override
                            public void run() {
                                inner.removeCallbacks(this);
                                if (tapedPoint != null) {
                                    if ((System.currentTimeMillis() - tapStartTime) >= longPressDuration) {
                                        getPixelColorFindViewAndMakeListener(tapedPoint, view, ClickType.LONG_PRESS);
                                        tapStartTime = 0l;
                                    }
                                } else {
                                    throw new NullPointerException("Taped point is null");
                                }
                            }
                        });
                    }
                };
            }
        });

    }

    private void detectOutSide(View view, MotionEvent motionEvent) {
        tapedPoint = new TapedPoint((int) motionEvent.getX(), (int) motionEvent.getY());
        try {
            bitmap = view.getDrawingCache();
            int pixel = bitmap.getPixel(tapedPoint.getX1(), tapedPoint.getY1());
            int red = Color.red(pixel);
            int blue = Color.blue(pixel);
            int green = Color.green(pixel);
            Log.d(TAG, "Red====>: " + red + " Blue: " + blue + " Green: " + green);

            if (red == 0 && blue == 0 && green == 0) {
                cancelTouch(view);
            } else if (!rect.contains(view.getLeft() + (int) motionEvent.getX(), view.getTop() + (int) motionEvent.getY())) {
                cancelTouch(view);
            }
        } catch (Exception e) {
            cancelTouch(view);
        }
    }

    private void cancelTouch(View view) {
        view.setPressed(false);
        onDetectListener.onClickCancel(view);
    }

    public void handShakeListener(OnDetectListener onDetectListener) {
        this.onDetectListener = onDetectListener;
    }

    OnDetectListener onDetectListener;

    public interface OnDetectListener {
        void onClickUp(View view);
        void onClickDown(View view);
        void onLongClick(View view);
        void onLongClickUp(View view);
        void onClickCancel(View view);

    }

}
