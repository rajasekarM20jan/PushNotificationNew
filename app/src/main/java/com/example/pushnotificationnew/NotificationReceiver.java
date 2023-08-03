package com.example.pushnotificationnew;

import static com.example.pushnotificationnew.MainActivity.handlerThread;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationManagerCompat;

public class NotificationReceiver extends BroadcastReceiver {
    public static final String ACTION_CANCEL_NOTIFICATION = "cancel_notification";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && ACTION_CANCEL_NOTIFICATION.equals(intent.getAction())) {
            // Cancel the notification when "Dismiss" action is clicked
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.cancel(MainActivity.NOTIFICATION_ID);
            MyFireBaseMessagingService.isDismissed=true;
            handlerThread.quit();
        }
    }
}
