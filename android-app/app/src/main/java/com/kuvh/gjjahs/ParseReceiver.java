package com.kuvh.gjjahs;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.kuvh.gjjahs.web.WebActivity;
import com.parse.ParsePushBroadcastReceiver;

import org.json.JSONException;
import org.json.JSONObject;

public class ParseReceiver extends ParsePushBroadcastReceiver {

    private static final String LOG_TAG = ParseReceiver.class.getSimpleName();

    protected void onPushReceive(Context context, Intent intent) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        if(pref.getBoolean("push_notifications", true)) {

            Bundle extras = intent.getExtras();
            String jsonData = extras.getString("com.parse.Data");
            String url = "";
            String alertMsg = "";

            if (jsonData != null) {
                try {
                    JSONObject data = new JSONObject(jsonData);
                    alertMsg = data.getString("alert");
                    url = data.getString("url");
                } catch (JSONException e) {
                    url = "";
                    Log.e(LOG_TAG, "Error parsing json data", e);
                }
            } else {
                Log.w(LOG_TAG, "cannot find notification data");
            }

            Intent launchActivity;
            if(url != "") {
                launchActivity = new Intent(context, WebActivity.class);
                launchActivity.putExtra("url",url);
            } else {
                launchActivity = new Intent(context, SplashActivity.class);
            }
            PendingIntent pi = PendingIntent.getActivity(context, 0, launchActivity, PendingIntent.FLAG_ONE_SHOT);

            Notification noti;
            if(pref.getBoolean("push_notifications_vibrate", true)) {
                noti = new NotificationCompat.Builder(context)
                        .setContentTitle(context.getString(R.string.app_name))
                        .setContentText(alertMsg)
                        .setContentIntent(pi)
                        .setAutoCancel(true)
                        .setDefaults(Notification.DEFAULT_VIBRATE)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setTicker(alertMsg)
                        .setSound(Uri.parse(pref.getString("push_notifications_ringtone", "content://settings/system/notification_sound")))
                        .build();
            } else {
                noti = new NotificationCompat.Builder(context)
                        .setContentTitle(context.getString(R.string.app_name))
                        .setContentText(alertMsg)
                        .setContentIntent(pi)
                        .setAutoCancel(true)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setTicker(alertMsg)
                        .setSound(Uri.parse(pref.getString("push_notifications_ringtone", "content://settings/system/notification_sound")))
                        .build();
            }

            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            nm.notify(0, noti);
        }
    }
}