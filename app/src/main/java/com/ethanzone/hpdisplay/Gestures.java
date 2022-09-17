package com.ethanzone.hpdisplay;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import java.util.Objects;

public class Gestures {

    private final Context context;
    private final View display;
    private final View pill;
    private final MediaManager mediaManager;

    public Gestures(Context context, View display, View pill, MediaManager mediaManager) {
        this.context = context;
        this.display = display;
        this.pill = pill;
        this.mediaManager = mediaManager;
    }

    private float prevY;

    @SuppressLint({"ClickableViewAccessibility", "UseCompatLoadingForDrawables"})
    public void setListeners() {

        AudioManager audioManager = mediaManager.audioManager;

        // Open display on pill click
        this.pill.findViewById(R.id.button).setOnTouchListener((view, motionEvent) -> {
            Log.v("event", "Click!");
            UIState uiState = UIState.getCurrentState(this.context);
            uiState.shape = UIState.SHAPE_OPEN;
            uiState.apply(this.context);
            return true;
        });

        // Manage display touches and gestures
        this.display.setOnTouchListener((View view, MotionEvent event) -> {

            // Reset the auto-close timer
            ((HPDisplay) this.context).waitToClose();

            float x = event.getX();
            float y = event.getY();

            Button icon = this.display.findViewById(R.id.icon);

            float distance = (float) Math.sqrt(Math.pow(x - icon.getX(), 2) + Math.pow(y - icon.getY(), 2));

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:

                    // Detect if icon is clicked
                    if (distance < 240f) {
                        Log.v("event", "icon click");

                        UIState uiState = UIState.getCurrentState(this.context);
                        if (audioManager.isMusicActive() && Objects.equals(uiState.title, UIState.DEFAULT_TITLE)) {

                            uiState.icon = context.getDrawable(R.drawable.playbutton);
                            uiState.apply(this.context);

                            mediaManager.pause();

                        }
                        // TODO: Find a condition where the music is paused, but not just not playing
                        /*
                        else if (Objects.equals(uiState.shape, UIState.SHAPE_OPEN)) {

                            uiState.icon = context.getDrawable(R.drawable.pause);
                            uiState.apply(this.context);

                            mediaManager.play();

                        }
                        */
                        else {

                            try {
                                // Open the notification
                                ((HPDisplay) this.context).clickAction.send();
                                // Close the display
                                uiState.shape = UIState.SHAPE_CLOSED;
                                uiState.apply(this.context);

                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            // Clear the display
                            new UIState(UIState.DEFAULT_TITLE, UIState.DEFAULT_DESCRIPTION,
                                    context.getDrawable(R.drawable.checkmark),
                                    UIState.ICON_BLANK,  UIState.ICON_BLANK, UIState.SHAPE_NOCHANGE)
                                    .apply(this.context);

                        }

                        this.prevY = 0;
                        return true;
                    }
                    break;

                case MotionEvent.ACTION_MOVE:

                    float dy = y - this.prevY;

                    if (dy < -10) {
                        Log.v("event", "swipe up");

                        UIState uiState = UIState.getCurrentState(this.context);
                        uiState.shape = UIState.SHAPE_CLOSED;
                        uiState.apply(this.context);

                        return true;
                    }

                case MotionEvent.ACTION_UP:
                    break;
            }

            this.prevY = y;

            return true;
        });

    }

}
