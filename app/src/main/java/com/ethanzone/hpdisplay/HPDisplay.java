package com.ethanzone.hpdisplay;

import android.accessibilityservice.AccessibilityService;
import android.app.Notification;
import android.app.Service;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

public class HPDisplay extends AccessibilityService {

    private View display;
    private View pill;
    private Drawable icon = null;
    private String title = "Nothing to display";
    private String description = "Check back later";
    private WindowManager windowManager;

    @Override
    public void onInterrupt() {
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    public void onServiceConnected() {
        create();
    }

    private WindowManager.LayoutParams smallParams = new WindowManager.LayoutParams(
            1000,
            150,
            0, -15,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                    | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                    | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_FULLSCREEN
            , PixelFormat.TRANSPARENT);

    private WindowManager.LayoutParams largeParams = new WindowManager.LayoutParams(
            1000,
            500,
            0, -190,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                    | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                    | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_FULLSCREEN
            , PixelFormat.TRANSPARENT);

    @RequiresApi(api = Build.VERSION_CODES.R)
    private void create() {

        this.smallParams.gravity = Gravity.CENTER | Gravity.TOP;
        this.largeParams.gravity = Gravity.CENTER | Gravity.TOP;

        // Create the overlay window and add the view
        this.windowManager = (WindowManager) this.getSystemService(Service.WINDOW_SERVICE);

        LayoutInflater li = LayoutInflater.from(this);
        this.display = li.inflate(R.layout.popup, null);
        this.pill = li.inflate(R.layout.popup, null);

        this.windowManager.addView(this.pill, this.smallParams);
        this.windowManager.addView(this.display, this.largeParams);

        this.pill.setVisibility(View.VISIBLE);
        this.pill.findViewById(R.id.label).setVisibility(View.GONE); // Don't show pill text
        this.pill.findViewById(R.id.description).setVisibility(View.GONE);
        this.setLastingIcon(getDrawable(R.drawable.ic_edges));

        this.display.setVisibility(View.GONE);

        this.icon = getDrawable(R.drawable.checkmark);

        // First time animation

        this.display.findViewById(R.id.label).animate()
                .alpha(0)
                .setInterpolator(new LinearInterpolator())
                .setDuration(0);

        this.display.findViewById(R.id.description).animate()
                .alpha(0)
                .setInterpolator(new LinearInterpolator())
                .setDuration(0);

        this.display.animate()
                .scaleX(1)
                .scaleY(1)
                .translationY(0)
                .setInterpolator(new LinearInterpolator())
                .setDuration(0);

        // Event handling
        this.pill.findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v("event", "Click!");
                HPDisplay.this.expand();
            }
        });

    }

    public void setLastingIcon(Drawable icon) {
        this.pill.findViewById(R.id.icon).setBackground(icon);
    }

    private boolean expanding;

    public void expand() {
        if (!this.expanding) {
            // Show the display
            this.display.setVisibility(View.VISIBLE);

            // Fill in data
            this.display.findViewById(R.id.icon).setBackground(this.icon);
            ((TextView) this.display.findViewById(R.id.label)).setText(this.title);
            ((TextView) this.display.findViewById(R.id.description)).setText(this.description);

            // Animate the window

            this.display.findViewById(R.id.label).animate()
                    .alpha(1)
                    .setInterpolator(new LinearInterpolator())
                    .setStartDelay(400)
                    .setDuration(300);

            this.display.findViewById(R.id.description).animate()
                    .alpha(1)
                    .setInterpolator(new LinearInterpolator())
                    .setStartDelay(400)
                    .setDuration(300);

            this.display.animate()
                    .scaleX(3)
                    .scaleY(3)
                    .translationY(100)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .setDuration(800);

            this.expanding = true;

            // Prevent double-contracts
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    HPDisplay.this.expanding = false;
                }
            }, 800);
        }
    }

    private boolean contracting;

    public void contract() {

        if (!this.contracting) {

            // Animate the window

            this.display.findViewById(R.id.label).animate()
                    .alpha(0)
                    .setInterpolator(new LinearInterpolator())
                    .setStartDelay(400)
                    .setDuration(300);

            this.display.findViewById(R.id.description).animate()
                    .alpha(0)
                    .setInterpolator(new LinearInterpolator())
                    .setStartDelay(400)
                    .setDuration(300);

            this.display.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .translationY(0)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .setDuration(800);

            this.contracting = true;

            // Prevent double-contracts
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    // Hide the Display
                    HPDisplay.this.display.setVisibility(View.GONE);

                    HPDisplay.this.contracting = false;
                }

            }, 800);
        }
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

        Log.v("event", event.toString());

        if (event.getEventType() == (AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED)) {
            Notification notification = (Notification) event.getParcelableData();
            this.title = notification.extras.getString("android.title");
            this.description = notification.extras.getString("android.text");

            try {
                this.icon = getPackageManager().getApplicationIcon((String) event.getPackageName());
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            setLastingIcon(this.icon);
            expand();
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    contract();
                }
            }, 3000);


        } else if (event.getEventType() != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            if (event.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                if (event.getPackageName() != "com.ethanzone.hpdisplay") { // Ignore self events
                    contract();
                }
            }
        }

    }
}