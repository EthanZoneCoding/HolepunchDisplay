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

    public NotificationHandler(Context context) {
        this.context = context;
        this.utils = new Utils(context);
    }

    public UIState readNotification(AccessibilityEvent event) {

        // Check of the event is a notification
        if (event.getEventType() == (AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED)) {
            try {
                // Read the notification
                Notification notification = (Notification) event.getParcelableData();
                String title = notification.extras.getString("android.title");
                String description = notification.extras.getString("android.text");

                // Delete the notification from the bar if the setting is enabled and the notification is not a persistent notification
                if (this.utils.getSetting("deletefrombar") & !Objects.equals(notification.getChannelId(), "7")) {
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
                    icon = UIState.ICON_BLANK;
                }

                // Create the UIState object
                return new UIState(title, description, icon, icon, UIState.ICON_BLANK, UIState.SHAPE_OPEN);
            } catch (NullPointerException e) {
                return UIState.nullState();
            }
        }

        // Return a null UIState if the event is not a notification
        return UIState.nullState();
    }
}
