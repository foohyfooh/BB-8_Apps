package com.foohyfooh.bb8.notifications;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Build;

import com.foohyfooh.bb8.R;

public class NotificationHelper extends ContextWrapper {

    public static final String CHANNEL_DEFAULT = "default";

    private NotificationManager notificationManager;

    public NotificationHelper(Context context) {
        super(context);
    }

    public NotificationManager getNotificationManager() {
        if (notificationManager == null)
            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        return notificationManager;
    }

    public void setupNotificationChannels(){
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;

        NotificationChannel defaultChannel = new NotificationChannel( CHANNEL_DEFAULT,
                        getString(R.string.notificationChannel_default_name),
                        NotificationManager.IMPORTANCE_DEFAULT);
        defaultChannel.setDescription(getString(R.string.notificationChannel_default_description));

        NotificationManager notificationManager = getNotificationManager();
        notificationManager.createNotificationChannel(defaultChannel);
    }

    public Notification makeNotification(String id, String text){
        Notification.Builder builder = new Notification.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getText(R.string.app_name))
                .setContentText(text);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            builder.setChannelId(id);
        return builder
                .build();
    }

    public void postNotification(int id, Notification notification){
        getNotificationManager().notify(id, notification);
    }


}
