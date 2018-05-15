package com.foohyfooh.bb8;

import android.app.Application;
import android.os.Build;

import com.foohyfooh.bb8.utils.NotificationHelper;

public class BB8Application extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationHelper notificationHelper = new NotificationHelper(this);
            notificationHelper.setupNotificationChannels();
        }
    }

}
