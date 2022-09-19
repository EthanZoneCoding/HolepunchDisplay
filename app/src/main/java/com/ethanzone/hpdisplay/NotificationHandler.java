package com.ethanzone.hpdisplay;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.BatteryManager;
import android.os.Handler;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import java.util.Objects;

public class NotificationHandler {

    private final Utils utils;
    private final Context context;
    public UIState backedUpState;
    public UIState uiState;

    public NotificationHandler(Context context) {
        this.context = context;
        this.utils = new Utils(context);
        this.backedUpState = uiState;
        this.uiState = ((HPDisplay) this.context).uiState;
        context.registerReceiver(this.batReceiver, new IntentFilter(Intent.ACTION_POWER_CONNECTED));
    }

    public void readNotification(AccessibilityEvent event) {

        // Check of the event is a notification
        if (event.getEventType() == (AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED)) {
            try {
                // Read the notification
                Notification notification = (Notification) event.getParcelableData();
                if (!Objects.equals(notification.getChannelId(), "7")) {
                    String title = notification.extras.getString("android.title");
                    String description = notification.extras.getString("android.text");

                    // Delete the notification from the bar if the setting is enabled and the notification is not a persistent notification
                    if (this.utils.getSetting("deletefrombar")){
                        try {
                            notification.deleteIntent.send();
                        } catch (PendingIntent.CanceledException e) {
                            e.printStackTrace();
                        }
                    }

                    ((HPDisplay) context).clickAction = notification.contentIntent;

                    // Get the icon of the notification's app
                    Drawable icon;
                    try {
                        icon = this.context.getPackageManager().getApplicationIcon((String) event.getPackageName());
                    } catch (PackageManager.NameNotFoundException e) {
                        icon = ((HPDisplay) context).uiState.ICON_NOCHANGE(UIState.ID_BIG);
                    }

                    // Backup the current state
                    this.backedUpState = uiState;

                    // Create the UIState object
                    uiState.title = title;
                    uiState.description = description;
                    uiState.icon = icon;
                    uiState.miniIcon = icon;
                    uiState.miniIconRight = ((HPDisplay) context).uiState.ICON_NOCHANGE(UIState.ID_SMALL_R);
                    uiState.shape = UIState.SHAPE_OPEN;

                    // Apply
                    uiState.apply();
                    Log.d("NotificationHandler", "Notification read");

                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

    // Charging
    @SuppressLint("UseCompatLoadingForDrawables")
    private final BroadcastReceiver batReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context ctx, Intent intent) {
            try {
                Log.d("Battery", "Battery level changed");

                // TODO: Make a charging animation

            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    };
}
