package com.foohyfooh.bb8.notifications;

import android.app.Notification;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.foohyfooh.bb8.R;
import com.foohyfooh.bb8.utils.NotificationHelper;

public class NotificationsActivity extends AppCompatActivity {


    private static final int NOTIFICATION_ID = 1;

    private AppInfoAdapter adapter;
    private NotificationHelper notificationHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notifications_activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(view -> finish());

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        RecyclerView recyclerView = findViewById(R.id.notifications_appList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AppInfoAdapter(this);
        recyclerView.setAdapter(adapter);

        notificationHelper = new NotificationHelper(this);
        findViewById(R.id.notifications_sendNotification).setOnClickListener(view -> {
            Notification notification = notificationHelper.makeNotification(NotificationHelper.CHANNEL_DEFAULT, "Sample Notification");
            notificationHelper.postNotification(NOTIFICATION_ID, notification);
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged(); //Notify of changes since the ConfigureActivity may have disabled an app
    }
}
