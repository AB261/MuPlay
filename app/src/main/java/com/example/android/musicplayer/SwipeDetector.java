package com.example.android.musicplayer;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class SwipeDetector implements View.OnTouchListener {
    private static final String logTag = "SwipeDetector";
    private static final int MIN_DISTANCE = 100;
    private float downX, downY, upX, upY;
    private Action mSwipeDetected = Action.None;

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = event.getX();
                downY = event.getY();
                mSwipeDetected = Action.None;
                return false;
            case MotionEvent.ACTION_UP:
                upX = event.getX();
                upY = event.getY();

                float deltaX = downX - upX;
                float deltaY = downY - upY;

                if (Math.abs(deltaX) > MIN_DISTANCE) {
                    if (deltaX < 0) {
                        Log.i(logTag, "Swipe Left to Right");
                        mSwipeDetected = Action.LR;
                        return false;
                    }
                    if (deltaX > 0) {
                        Log.i(logTag, "Swipe Right to Left");
                        mSwipeDetected = Action.RL;
                        return false;
                    }
                } else if (Math.abs(deltaY) > MIN_DISTANCE) {
                    if (deltaY < 0) {
                        Log.i(logTag, "Swipe Top to Bottom");
                        mSwipeDetected = Action.TB;
                        return false;
                    }
                    if (deltaY > 0) {
                        Log.i(logTag, "Swipe Bottom to Top");
                        mSwipeDetected = Action.BT;
                        return false;
                    }
                }
                return false;

        }


        return false;

    }

    public boolean swipeDetected() {
        return mSwipeDetected != Action.None;
    }

    public Action getAction() {
        return mSwipeDetected;
    }

    public enum Action {
        LR,
        RL,
        BT,
        TB,
        None
    }


}
