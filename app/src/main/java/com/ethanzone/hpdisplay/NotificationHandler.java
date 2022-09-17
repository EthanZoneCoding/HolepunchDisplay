package com.ethanzone.hpdisplay;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.view.accessibility.AccessibilityEvent;

import java.util.Objects;

public class NotificationHandler {

    private final Utils utils;
    private final Context context;
    public UIState backedUpState;

    public NotificationHandler(Context context) {
        this.context = context;
        this.utils = new Utils(context);
        this.backedUpState = UIState.getCurrentState(context);
    }

    public UIState readNotification(AccessibilityEvent event) {

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

                    ((HPDisplay) this.context).clickAction = notification.contentIntent;

                    // Get the icon of the notification's app
                    Drawable icon;
                    try {
                        icon = this.context.getPackageManager().getApplicationIcon((String) event.getPackageName());
                    } catch (PackageManager.NameNotFoundException e) {
                        icon = UIState.ICON_NOCHANGE(this.context);
                    }

                    // Backup the current state
                    this.backedUpState = UIState.getCurrentState(this.context);
                    // Create the UIState object
                    return new UIState(title, description, icon, icon, UIState.ICON_NOCHANGE(this.context), UIState.SHAPE_OPEN);
                }
            } catch (NullPointerException e) {
                return UIState.nullState();
            }
        }

        // Return a null UIState if the event is not a notification
        return UIState.nullState();
    }
}
