package com.investokar.poppi.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.investokar.poppi.activity.HomeActivity;
import com.investokar.poppi.fragment.ChatFragment;
import com.investokar.poppi.activity.MainActivity;
import com.investokar.poppi.R;
import com.investokar.poppi.app.App;
import com.investokar.poppi.constants.Constants;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.Map;
import java.util.Random;

public class MyFcmListenerService extends FirebaseMessagingService implements Constants {

    private final int flag;

    public MyFcmListenerService () {
        flag = PendingIntent.FLAG_IMMUTABLE;
    }

    String fromUserPhotoUrl = "";
    RemoteViews contentView;
    Map<String, String> data;

    @Override
    public void onMessageReceived(RemoteMessage message) {
        String from = message.getFrom();
        data = message.getData();
        Log.e("Message", "Could not parse malformed JSON: \"" + data.toString() + "\"");
        if (data.containsKey("fromUserPhotoUrl")) {
            fromUserPhotoUrl = data.get("fromUserPhotoUrl");
            LoadImage loadImage = new LoadImage();
            loadImage.execute();
        } else {
            generateNotification(getApplicationContext(), data, null);
        }
    }

    @Override
    public void onNewToken(@NonNull String token) {
        Log.d(TAG, "Refreshed token: " + token);
        App.getInstance().setGcmToken(token);
    }

    @Override
    public void onDeletedMessages() {
        sendNotification("Deleted messages on server");
    }

    @Override
    public void onMessageSent(@NonNull String msgId) {
        sendNotification("Upstream message sent. Id=" + msgId);
    }


    public class LoadImage extends AsyncThread {

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected void onPostExecute(Object result) {
            generateNotification(getApplicationContext(), data, (Bitmap) result);
        }

        @Nullable
        @Override
        protected Object doInBackground(Object... params) {
            try {
                return Picasso.get().load(fromUserPhotoUrl).get();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private void sendNotification(String msg) {
        Log.e("Message", "Could not parse malformed JSON: \"" + msg + "\"");
    }

    @SuppressLint("SuspiciousIndentation")
    private void generateNotification(Context context, Map data, Bitmap bitmap) {
        String CHANNEL_ID = "my_channel_01"; // id for channel.
        CharSequence CHANNEL_NAME = context.getString(R.string.channel_name); // user visible name of channel.

        Random random = new Random();
        int randomNumber = random.nextInt();

        NotificationChannel mChannel;

        String msgId = "0";
        String fromUserId = "0";
        String fromUserState = "0";
        String fromUserUsername = "";
        String fromUserFullname = "";
        String msgMessage = "";
        String msgImageUrl = "";
        String msgCreateAt = "0";
        String msgDate = "";
        String msgTimeAgo = "";
        String msgRemoveAt = "0";

        String message = data.get("msg").toString();
        String type = data.get("type").toString();
        String actionId = data.get("id").toString();
        String accountId = data.get("accountId").toString();

        if (Integer.parseInt(type) == GCM_NOTIFY_MESSAGE || Integer.parseInt(type) == GCM_NOTIFY_LIKE) {

            msgId = data.get("msgId").toString();
            fromUserId = data.get("fromUserId").toString();
            fromUserState = data.get("fromUserState").toString();

            if (data.containsKey("fromUserUsername")) {
                fromUserUsername = data.get("fromUserUsername").toString();
            }

            if (data.containsKey("fromUserFullname")) {
                fromUserFullname = data.get("fromUserFullname").toString();
                if (fromUserFullname.split("\\w+").length>1) {
                    fromUserFullname = fromUserFullname.substring(0, fromUserFullname.lastIndexOf(' '));
                }
            }

            if (data.containsKey("fromUserPhotoUrl")) {
                fromUserPhotoUrl = data.get("fromUserPhotoUrl").toString();
            }

            if (data.containsKey("msgMessage")) {
                msgMessage = data.get("msgMessage").toString().replaceAll("<br>", "\n");
            }

            if (data.containsKey("msgImageUrl")) {
                msgImageUrl = data.get("msgImageUrl").toString();
            }

            msgCreateAt = data.get("msgCreateAt").toString();
            msgDate = data.get("msgDate").toString();
            msgTimeAgo = data.get("msgTimeAgo").toString();
            msgRemoveAt = data.get("msgRemoveAt").toString();
        }

        String title = context.getString(R.string.app_name);

        switch (Integer.parseInt(type)) {
            case GCM_NOTIFY_SYSTEM: {
                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(context, CHANNEL_ID)
                                .setSmallIcon(R.drawable.ic_notification)
                                .setContentTitle(title)
                                .setContentText(message);

                Intent resultIntent;
                if (App.getInstance().getId() != 0) {
                    resultIntent = new Intent(context, MainActivity.class);
                } else {
                    resultIntent = new Intent(context, HomeActivity.class);
                }

                resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                stackBuilder.addParentStack(MainActivity.class);
                stackBuilder.addNextIntent(resultIntent);
                PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, flag);
                mBuilder.setContentIntent(resultPendingIntent);

                NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    int importance = NotificationManager.IMPORTANCE_HIGH;
                    mChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance);
                    mNotificationManager.createNotificationChannel(mChannel);
                }

                mBuilder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
                mBuilder.setAutoCancel(true);
                mNotificationManager.notify(randomNumber, mBuilder.build());

                break;
            }

            case GCM_NOTIFY_CUSTOM: {
                if (App.getInstance().getId() != 0) {
                    NotificationCompat.Builder mBuilder =
                            new NotificationCompat.Builder(context, CHANNEL_ID)
                                    .setSmallIcon(R.drawable.ic_notification)
                                    .setContentTitle(title)
                                    .setContentText(message);

                    Intent resultIntent = new Intent(context, MainActivity.class);
                    resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                    stackBuilder.addParentStack(MainActivity.class);
                    stackBuilder.addNextIntent(resultIntent);
                    PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, flag);
                    mBuilder.setContentIntent(resultPendingIntent);

                    NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        int importance = NotificationManager.IMPORTANCE_HIGH;
                        mChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance);
                        mNotificationManager.createNotificationChannel(mChannel);
                    }

                    mBuilder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
                    mBuilder.setAutoCancel(true);
                    mNotificationManager.notify(randomNumber, mBuilder.build());
                }
                break;
            }

            case GCM_NOTIFY_PERSONAL: {
                if (App.getInstance().getId() != 0 && Long.toString(App.getInstance().getId()).equals(accountId)) {
                    NotificationCompat.Builder mBuilder =
                            new NotificationCompat.Builder(context, CHANNEL_ID)
                                    .setSmallIcon(R.drawable.ic_notification)
                                    .setContentTitle(title)
                                    .setContentText(message);

                    Intent resultIntent = new Intent(context, MainActivity.class);
                    resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                    stackBuilder.addParentStack(MainActivity.class);
                    stackBuilder.addNextIntent(resultIntent);
                    PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, flag);
                    mBuilder.setContentIntent(resultPendingIntent);

                    NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        int importance = NotificationManager.IMPORTANCE_HIGH;
                        mChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance);
                        mNotificationManager.createNotificationChannel(mChannel);
                    }

                    mBuilder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
                    mBuilder.setAutoCancel(true);
                    mNotificationManager.notify(randomNumber, mBuilder.build());
                }

                break;
            }

            case GCM_NOTIFY_LIKE: {
                if (App.getInstance().getId() != 0 && Long.toString(App.getInstance().getId()).equals(accountId)) {
                    App.getInstance().setNotificationsCount(App.getInstance().getNotificationsCount() + 1);
                    contentView = new RemoteViews(context.getPackageName(), R.layout.custom_notification_view);
                    contentView.setTextViewText(R.id.title, fromUserFullname + " likes your profile.");
                    contentView.setTextViewText(R.id.body, "");
                    if (bitmap != null) {
                        contentView.setImageViewBitmap(R.id.small_picture, bitmap);
                    }

                    Intent resultIntent = new Intent(context, MainActivity.class);
                    resultIntent.putExtra("tab_id", 1);
                    resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    PendingIntent resultPendingIntent = PendingIntent.getActivity(context, randomNumber, resultIntent, flag);

                    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContent(contentView)
                            .setPriority(Notification.PRIORITY_MAX)
                            .setDefaults(Notification.DEFAULT_ALL)
                            .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE)
                            .setAutoCancel(true)
                            .setContentIntent(resultPendingIntent);

                    NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        mChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
                        mNotificationManager.createNotificationChannel(mChannel);
                    }

                    mNotificationManager.notify(randomNumber, mBuilder.build());
                }
                break;
            }

            case GCM_NOTIFY_MESSAGE: {
                if (App.getInstance().getId() != 0 && Long.parseLong(accountId) == App.getInstance().getId()) {
                    if (App.getInstance().getCurrentChatId() == Integer.parseInt(actionId)) {
                        Intent i = new Intent(ChatFragment.BROADCAST_ACTION);
                        i.putExtra(ChatFragment.PARAM_TASK, 0);
                        i.putExtra(ChatFragment.PARAM_STATUS, ChatFragment.STATUS_START);
                        i.putExtra("msgId", Integer.valueOf(msgId));
                        i.putExtra("fromUserId", Long.valueOf(fromUserId));
                        i.putExtra("fromUserState", Integer.valueOf(fromUserState));
                        i.putExtra("fromUserUsername", fromUserUsername);
                        i.putExtra("fromUserFullname", fromUserFullname);
                        i.putExtra("fromUserPhotoUrl", fromUserPhotoUrl);
                        i.putExtra("msgMessage", msgMessage);
                        i.putExtra("msgImageUrl", msgImageUrl);
                        i.putExtra("msgCreateAt", Integer.valueOf(msgCreateAt));
                        i.putExtra("msgDate", msgDate);
                        i.putExtra("msgTimeAgo", msgTimeAgo);
                        context.sendBroadcast(i);
                    } else {
                        if (App.getInstance().getMessagesCount() == 0) App.getInstance().setMessagesCount(App.getInstance().getMessagesCount() + 1);
                        contentView = new RemoteViews(context.getPackageName(), R.layout.custom_notification_view);
                        contentView.setTextViewText(R.id.title, fromUserFullname);
                        contentView.setTextViewText(R.id.body, msgMessage);
                        if (bitmap != null) {
                            contentView.setImageViewBitmap(R.id.small_picture, bitmap);
                        }

                        Intent resultIntent = new Intent(context, MainActivity.class);
                        resultIntent.putExtra("tab_id", 2);
                        resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        PendingIntent resultPendingIntent = PendingIntent.getActivity(context, randomNumber, resultIntent, flag);

                        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                                .setSmallIcon(R.mipmap.ic_launcher)
                                .setContent(contentView)
                                .setPriority(Notification.PRIORITY_MAX)
                                .setDefaults(Notification.DEFAULT_ALL)
                                .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE)
                                .setAutoCancel(true)
                                .setContentIntent(resultPendingIntent);

                        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            mChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
                            mNotificationManager.createNotificationChannel(mChannel);
                        }

                        mNotificationManager.notify(randomNumber, mBuilder.build());
                    }
                }

                break;
            }


            case GCM_NOTIFY_SEEN: {
                if (App.getInstance().getId() != 0 && Long.parseLong(accountId) == App.getInstance().getId()) {
                    Log.e("SEEN", "IN LISTENER");
                    if (App.getInstance().getCurrentChatId() == Integer.parseInt(actionId)) {
                        Intent i = new Intent(ChatFragment.BROADCAST_ACTION_SEEN);
                        i.putExtra(ChatFragment.PARAM_TASK, 0);
                        i.putExtra(ChatFragment.PARAM_STATUS, ChatFragment.STATUS_START);
                        context.sendBroadcast(i);
                    }
                    break;
                }
                break;
            }

            case GCM_NOTIFY_TYPING_START: {
                if (App.getInstance().getId() != 0 && Long.parseLong(accountId) == App.getInstance().getId()) {
                    if (App.getInstance().getCurrentChatId() == Integer.parseInt(actionId)) {
                        Intent i = new Intent(ChatFragment.BROADCAST_ACTION_TYPING_START);
                        i.putExtra(ChatFragment.PARAM_TASK, 0);
                        i.putExtra(ChatFragment.PARAM_STATUS, ChatFragment.STATUS_START);
                        context.sendBroadcast(i);
                    }
                    break;
                }
            }

            case GCM_NOTIFY_TYPING_END: {
                if (App.getInstance().getId() != 0 && Long.parseLong(accountId) == App.getInstance().getId()) {
                    if (App.getInstance().getCurrentChatId() == Integer.parseInt(actionId)) {
                        Intent i = new Intent(ChatFragment.BROADCAST_ACTION_TYPING_END);
                        i.putExtra(ChatFragment.PARAM_TASK, 0);
                        i.putExtra(ChatFragment.PARAM_STATUS, ChatFragment.STATUS_START);
                        context.sendBroadcast(i);
                    }
                    break;
                }
            }

            case GCM_NOTIFY_CHANGE_ACCOUNT_SETTINGS: {
                if (App.getInstance().getId() != 0 && Long.toString(App.getInstance().getId()).equals(accountId)) {
                    Log.e("CHANGE_ACCOUNT_SETTINGS", "GCM_NOTIFY_CHANGE_ACCOUNT_SETTINGS");
                    App.getInstance().loadSettings();
                }
                break;
            }
            default: {
                break;
            }
        }
    }
}