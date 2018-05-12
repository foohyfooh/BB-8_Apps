package com.foohyfooh.bb8

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.foohyfooh.bb8.notifications.NotificationsActivity
import com.foohyfooh.bb8.voice_commands.VoiceCommandsActivity

import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        main_gotoNotifications.setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
        }

        main_gotoVoiceCommands.setOnClickListener {
            startActivity(Intent(this, VoiceCommandsActivity::class.java))
        }

    }

}
