package net.vingroup.ecar.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import net.vingroup.ecar.MainActivity;
import net.vingroup.ecar.R;

/**
 * Created by redsu on 12/20/2017.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        Log.d(TAG, "Message data payload: " + remoteMessage.getData());
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        //This will give you the Text property in the curl request(Sample Message):
        Log.d(TAG, "Notification Message Body: " + remoteMessage.getNotification().getBody());
        //This is where you get your click_action
        Log.d(TAG, "Notification Click Action: " + remoteMessage.getNotification().getClickAction());
        //put code here to navigate based on click_action
        Log.e("FCM_MESSAGE", remoteMessage.getData().toString());
        remoteMessage.getNotification().getBody();
        showNotification(remoteMessage.getNotification().getBody());
    }


    private void showNotification(String messageBody) {
        SharedPreferences sharedPreferences= this.getSharedPreferences("VINECAR", Context.MODE_PRIVATE);
        String receiveValue = sharedPreferences.getString("_site", "");//receiveBundle.getString("_sitename");
        Log.d("NotificationDetect","respond: " + receiveValue);
        Bundle sendBundle = new Bundle();
        sendBundle.putString("_sitename",receiveValue);
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtras(sendBundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.main)
                .setContentTitle("Thông báo từ Vinpearl Car")
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0 , notificationBuilder.build());
    }

}