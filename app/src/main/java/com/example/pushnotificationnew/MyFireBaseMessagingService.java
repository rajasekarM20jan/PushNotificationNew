package com.example.pushnotificationnew;


import static com.example.pushnotificationnew.MainActivity.NOTIFICATION_CHANNEL_ID;
import static com.example.pushnotificationnew.MainActivity.NOTIFICATION_ID;
import static com.example.pushnotificationnew.MainActivity.handlerThread;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

public class MyFireBaseMessagingService extends FirebaseMessagingService {

    public static String title,body;
    static NotificationManager notificationManager;
    private static final String TAG = "MyFirebaseMessagingService";
    private static int SECONDS;
    static Bitmap bannerBitmap;
    private CountDownTimer countDownTimer;
    public static boolean isDismissed;
    NotificationCompat.BigPictureStyle notificationStyle;

    Handler handler;

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        title = remoteMessage.getData().get("title");
        body = remoteMessage.getData().get("message");


        String imageUrl = remoteMessage.getData().get("imageUrl");
        try {
            JSONObject timerJson = new JSONObject(remoteMessage.getData().get("timer"));
            SECONDS= timerJson.getInt("secondsLeft");
        } catch (JSONException e) {
            SECONDS=0;
            e.getMessage();
        }
        isDismissed=false;
        showNotification(title, body, imageUrl, SECONDS);
    }

    public void showNotification(String title, String message, String imageUrl, int timerData) {
        if (!isDismissed) {
            // Create a notification builder.
            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            String channelId = NOTIFICATION_CHANNEL_ID;
            String channelName = "My Channel";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
                channel.enableLights(true);
                channel.setLightColor(Color.RED);
                channel.enableVibration(true);
                channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                notificationManager.createNotificationChannel(channel);
            }
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "my_channel_id");

            // Set the notification title and message.
            String timerText = String.format(Locale.getDefault(), "\t%02d:%02d", SECONDS / 60, SECONDS % 60);
            notificationBuilder.setContentTitle(title+"\t"+timerText);
            notificationBuilder.setOngoing(true);
            notificationBuilder.setAutoCancel(true);
            notificationBuilder.setOnlyAlertOnce(true);

            bannerBitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.banner_image);
            // Set the notification image.
            if (imageUrl != null) {

                notificationBuilder.setSmallIcon(R.drawable.ic_notification);
                notificationStyle = new NotificationCompat.BigPictureStyle()
                        .bigPicture(bannerBitmap) // Add your banner image here
                        .setSummaryText(message);
                Intent cancelIntent = new Intent(this, NotificationReceiver.class);
                cancelIntent.setAction(NotificationReceiver.ACTION_CANCEL_NOTIFICATION);
                PendingIntent pendingCancelIntent = PendingIntent.getBroadcast(this, NOTIFICATION_ID, cancelIntent, PendingIntent.FLAG_IMMUTABLE);
                notificationBuilder.addAction(R.drawable.ic_cancel, "Dismiss", pendingCancelIntent);

                Intent resultIntent = new Intent(this, MainActivity.class);
                PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_IMMUTABLE);
                notificationBuilder.setContentIntent(resultPendingIntent);
                notificationBuilder.setStyle(notificationStyle);
            }

            // Set the notification timer data.
            if (timerData != 0) {
                handlerThread = new HandlerThread("MyBackgroundThread");
                handlerThread.start();
                handler = new Handler(handlerThread.getLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (!isDismissed) {
                            countDownTimer = new CountDownTimer(timerData * 1000, 1000) {
                                @Override
                                public void onTick(long millisUntilFinished) {
                                    int secondsLeft = (int) (millisUntilFinished / 1000);
                                    if (secondsLeft > 0 && !isDismissed) {
                                        String timerText = String.format(Locale.getDefault(), "\t%02d:%02d", secondsLeft / 60, secondsLeft % 60);
                                        notificationBuilder.setContentTitle(title + timerText);

                                        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
                                    } else {
                                        countDownTimer.cancel();
                                        isDismissed = true;
                                        notificationManager.cancel(NOTIFICATION_ID);
                                    }
                                }

                                @Override
                                public void onFinish() {
                                    // Do something when the timer finishes.
                                }
                            }.start();
                        }
                    }
                });
            }
            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
        }
    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handlerThread.quit();
    }
}