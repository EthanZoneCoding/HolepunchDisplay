package com.ethanzone.hpdisplay;

import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import java.util.Objects;

public class HPDisplay extends AccessibilityService {

    private View display;
    private View pill;
    private Drawable icon = null;
    private Drawable iconBackup = null;
    private Drawable miniIcon = null;
    private Drawable miniIcon2 = null;
    private String title = "Nothing to display";
    private String description = "Check back later";
    private WindowManager windowManager;

    // For gesture detections
    private float prevX, prevY;
    private boolean isDown = false;

    @Override
    public void onInterrupt() {
    }

    private boolean getSetting(String setting) {
        // Get preferences
        SharedPreferences prefs = getSharedPreferences("com.ethanzone.hpdisplay", Context.MODE_PRIVATE);
        return prefs.getBoolean(setting, false);
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

    @SuppressLint("ClickableViewAccessibility")
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
        this.display.setVisibility(View.GONE);
        this.display.findViewById(R.id.button).setClickable(false); // Don't allow display to be clicked, only pill
        this.display.findViewById(R.id.icon2).setVisibility(View.GONE); // Don't show display right icon


        // Initialize the icons
        this.icon = getDrawable(R.drawable.checkmark);
        this.miniIcon = getDrawable(R.drawable.ic_edges);
        this.miniIcon2 = getDrawable(R.drawable.ic_edges);
        this.pill.findViewById(R.id.icon).setBackground(this.miniIcon);
        this.pill.findViewById(R.id.icon2).setBackground(this.miniIcon2);

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

        // Open display on pill click
        this.pill.findViewById(R.id.button).setOnTouchListener((view, motionEvent) -> {
            Log.v("event", "Click!");
            HPDisplay.this.expand();
            return true;
        });

        // Close display on display swipe up
        this.display.setOnTouchListener((view, event) -> {
            Log.v("event", event.getAction() + "");
            float x = event.getX();
            float y = event.getY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    HPDisplay.this.isDown = true;
                    break;
                case MotionEvent.ACTION_MOVE:

                    float dx = x - HPDisplay.this.prevX;
                    float dy = y - HPDisplay.this.prevY;

                    if (dy < -10) {
                        HPDisplay.this.contract();
                    }

                case MotionEvent.ACTION_UP:
                    HPDisplay.this.isDown = false;
                    break;
            }

            HPDisplay.this.prevX = x;
            HPDisplay.this.prevY = y;
            return true;
        });


    }

    private boolean open = false;

    private boolean expanding;

    public void expand() {
        if (!this.expanding && !this.open) {
            this.expanding = true;
            this.open = true;

            // Show the display
            this.display.setVisibility(View.VISIBLE);

            // Fill in data
            this.pill.findViewById(R.id.icon).setBackground(this.miniIcon);
            this.pill.findViewById(R.id.icon2).setBackground(this.miniIcon2);
            this.display.findViewById(R.id.icon).setBackground(this.icon);
            ((TextView) this.display.findViewById(R.id.label)).setText(this.title);
            ((TextView) this.display.findViewById(R.id.description)).setText(this.description);

            // Animate the window

            // Fade in the text
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

            // Expand the window
            this.display.animate()
                    .scaleX(3)
                    .scaleY(3)
                    .translationY(100)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .setDuration(800);


            // Prevent double-contracts
            Handler handler = new Handler();
            handler.postDelayed(() -> {
                HPDisplay.this.expanding = false;

            }, 800);
        }
    }

    private boolean contracting;

    public void contract() {

        if (!this.contracting && this.open) {
            this.contracting = true;

            // Animate the window

            // Fade out text
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

            // Shrink window
            this.display.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .translationY(0)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .setDuration(800);

            // Prevent double-contracts
            Handler handler = new Handler();
            handler.postDelayed(() -> {

                // Ensure the pill's button is on the bottom
                HPDisplay.this.pill.findViewById(R.id.button).setElevation(-10);

                // Hide the Display
                HPDisplay.this.display.setVisibility(View.GONE);


                HPDisplay.this.contracting = false;
                HPDisplay.this.open = false;
            }, 800);
        }
    }

    public void setMediaManager(boolean on){
        if (on) {
            AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

            // Backup the icon
            this.iconBackup = this.icon;

            // Set the icon to a play button, and show the right mini icon
            this.icon = getDrawable(R.drawable.pause);
            this.miniIcon2 = getDrawable(R.drawable.music);


        } else {
            // Restore the icon and hide the right icon
            this.icon = this.iconBackup;
            this.miniIcon2 = getDrawable(R.drawable.ic_edges);
        }
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

        Log.v("event", event.toString());

        // Handle incoming notifications
        if (event.getEventType() == (AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED)) {

            // Read the notification
            Notification notification = (Notification) event.getParcelableData();
            this.title = notification.extras.getString("android.title");
            this.description = notification.extras.getString("android.text");

            // Check if media is playing
            if (Objects.equals(notification.getChannelId(), "7")){

                // If so, show the display as a media manager
                Log.v("event", "Media playing!");
                this.setMediaManager(true);

            }

            // Non media notification
            else {
                this.setMediaManager(false);

                // Delete the notification from the bar if the setting is enabled
                if (getSetting("deletefrombar")) {
                    try {
                        notification.deleteIntent.send();
                    } catch (PendingIntent.CanceledException e) {
                        e.printStackTrace();
                    }
                }

                // Try to get the icon
                try {
                    this.icon = getPackageManager().getApplicationIcon((String) event.getPackageName());

                    // Update the mini icon, because it is a new notification
                    this.miniIcon = getPackageManager().getApplicationIcon((String) event.getPackageName());

                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }



            expand();

            // Close display after 3 seconds
            Handler handler = new Handler();
            handler.postDelayed(() -> contract(), 3000);


        } else if (event.getEventType() != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            // Close the display if the user does something else

            if (event.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                if (event.getPackageName() != "com.ethanzone.hpdisplay") {
                    // Ignore changes to the display itself
                    contract();
                }
            }
        }

    }
}