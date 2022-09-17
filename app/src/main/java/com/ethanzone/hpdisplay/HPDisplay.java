package com.ethanzone.hpdisplay;

import static com.ethanzone.hpdisplay.UIState.largeParams;
import static com.ethanzone.hpdisplay.UIState.smallParams;

import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.Service;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;

import androidx.annotation.RequiresApi;


public class HPDisplay extends AccessibilityService {

    public View display;
    public View pill;
    public PendingIntent clickAction;

    public final static int DEFAULT_DELAY = 3000;
    public int closeDelay = DEFAULT_DELAY;
    private final Handler closeRunnable = new Handler();

    public NotificationHandler notificationHandler = null;
    public MediaManager mediaManager = null;

    public HPDisplay() {
    }


    @Override
    public void onInterrupt() {
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    public void onServiceConnected() {
        create();
    }


    @SuppressLint({"ClickableViewAccessibility", "InflateParams", "UseCompatLoadingForDrawables"})
    @RequiresApi(api = Build.VERSION_CODES.R)
    private void create() {

        // Create the overlay window and add the view
        WindowManager windowManager = (WindowManager) this.getSystemService(Service.WINDOW_SERVICE);

        LayoutInflater li = LayoutInflater.from(this);
        this.display = li.inflate(R.layout.popup, null);
        this.pill = li.inflate(R.layout.popup, null);


        smallParams.gravity = Gravity.CENTER | Gravity.TOP;
        largeParams.gravity = Gravity.CENTER | Gravity.TOP;
        windowManager.addView(this.pill, smallParams);
        windowManager.addView(this.display, largeParams);

        this.pill.findViewById(R.id.label).setVisibility(View.GONE); // Don't show pill text
        this.pill.findViewById(R.id.description).setVisibility(View.GONE);
        this.display.setVisibility(View.GONE);
        this.display.findViewById(R.id.button).setClickable(false); // Don't allow display to be clicked, only pill (for now)
        this.display.findViewById(R.id.icon2).setVisibility(View.GONE); // Don't show display right icon


        // Initialize the different components
        MediaManager mediaManager = new MediaManager(this);
        Gestures gestures = new Gestures(this, this.display, this.pill, mediaManager);

        gestures.setListeners();

        // Initialize the UI
        UIState init = new UIState(UIState.DEFAULT_TITLE, UIState.DEFAULT_DESCRIPTION,
                getDrawable(R.drawable.checkmark), UIState.ICON_BLANK, UIState.ICON_BLANK, UIState.SHAPE_NOCHANGE);

        init.apply(this);

        // Initialize more components
        this.notificationHandler = new NotificationHandler(this);
        this.mediaManager = new MediaManager(this);

    }

    public void waitToClose() {

        closeDelay = DEFAULT_DELAY;

        closeRunnable.removeCallbacksAndMessages(null);
        closeRunnable.postDelayed(() -> {
            UIState uiState = UIState.getCurrentState(this);
            uiState.shape = UIState.SHAPE_CLOSED;
            uiState.apply(this);
        }, closeDelay);

    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

        Log.v("event", event.toString());

        UIState state = notificationHandler.readNotification(event);
        state.apply(this);

        this.mediaManager.updateMedia(UIState.getCurrentState(this));

    }
}