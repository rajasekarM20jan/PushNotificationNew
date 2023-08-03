package com.example.pushnotificationnew;


import androidx.appcompat.app.AppCompatActivity;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.HandlerThread;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class MainActivity extends AppCompatActivity {

    public static NotificationManager notificationManager;
    public static final String NOTIFICATION_CHANNEL_ID = "my_channel_id";
    public static int NOTIFICATION_ID = 1;
    public static boolean notificationDisplayed = false;
    Context context;
    Button sendNotification;
    public static HandlerThread handlerThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context=this;
        handlerThread = new HandlerThread("MyBackgroundThread");
        if(handlerThread.isAlive()){
            handlerThread.quit();
        }
        MyFireBaseMessagingService.isDismissed=true;
        FirebaseMessaging.getInstance().subscribeToTopic("testNotification");
    }




    @Override
    public void onBackPressed() {
        finishAffinity();
    }
}
