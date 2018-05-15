package com.foohyfooh.bb8

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.foohyfooh.bb8.notifications.NotificationListener
import com.foohyfooh.bb8.notifications.NotificationsActivity
import com.foohyfooh.bb8.voice_commands.VoiceCommandsActivity
import com.orbotix.common.DiscoveryAgent
import com.orbotix.common.Robot
import com.orbotix.common.RobotChangedStateListener

import kotlinx.android.synthetic.main.activity_main.*
import java.util.ArrayList

class MainActivity : AppCompatActivity(), DiscoveryListener {

    private val TAG = "MainActivity"
    private val REQUEST_CODE_LOCATION_PERMISSION = 1
    private val REQUEST_CODE_BLUETOOTH = 2

    private var serviceIsBound: Boolean = false
    private var bluetoothNotDenied = true
    private var bb8CommandService: BB8CommandService? = null;
    private var serviceConnection: ServiceConnection? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        main_gotoNotifications.setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
        }

        main_gotoVoiceCommands.setOnClickListener {
            startActivity(Intent(this, VoiceCommandsActivity::class.java))
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val hasLocationPermission = checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
            if (hasLocationPermission != PackageManager.PERMISSION_GRANTED) {
                //Log.e(TAG, "Location permission has not already been granted");
                val permissions = ArrayList<String>()
                permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)
                requestPermissions(permissions.toTypedArray(), REQUEST_CODE_LOCATION_PERMISSION)
            } else {
                //Log.d(TAG, "Location permission already granted");
                doBindService()
            }
        } else {
            doBindService()
        }
    }

    private fun doBindService() {
        serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(className: ComponentName, iBinder: IBinder) {
                bb8CommandService = (iBinder as BB8CommandService.BB8Binder).service
                bb8CommandService?.addDiscoveryListener(this@MainActivity)
                bb8CommandService?.start()
            }

            override fun onServiceDisconnected(className: ComponentName) {
                bb8CommandService = null
            }
        }
        bindService(Intent(this, BB8CommandService::class.java), serviceConnection, Context.BIND_AUTO_CREATE)
        serviceIsBound = true
    }

    private fun doUnbindService() {
        if (serviceIsBound) {
            unbindService(serviceConnection)
            serviceIsBound = false
        }
    }

    private fun handleRequirements() {
        val cn = ComponentName(this, NotificationListener::class.java)
        val flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        val notificationEnabled = flat != null && flat.contains(cn.flattenToString())

        var settingNotFound = false
        var locationEnabled = false
        try {
            locationEnabled = Settings.Secure.getInt(contentResolver,
                    Settings.Secure.LOCATION_MODE) != Settings.Secure.LOCATION_MODE_OFF
        } catch (e: Settings.SettingNotFoundException) {
            settingNotFound = true
        }

        if (!notificationEnabled) {
            AlertDialog.Builder(this)
                    .setTitle(R.string.notification_access_title)
                    .setMessage(R.string.notification_access_message)
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(R.string.enable) { dialogInterface, i -> startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")) }
                    .show()
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !settingNotFound && !locationEnabled) {
            AlertDialog.Builder(this)
                    .setTitle(R.string.location_access_title)
                    .setMessage(R.string.location_access_message)
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(R.string.enable) { dialogInterface, i ->
                        val enableLocationIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        startActivity(enableLocationIntent)
                    }
                    .show()
        } else if (bluetoothNotDenied && BluetoothAdapter.getDefaultAdapter() != null
                && !BluetoothAdapter.getDefaultAdapter().isEnabled) {
            val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBluetoothIntent, REQUEST_CODE_BLUETOOTH)
        }
    }

    override fun onResume() {
        super.onResume()
        handleRequirements()
    }

    override fun onStart() {
        super.onStart()
        bb8CommandService?.start()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_CODE_LOCATION_PERMISSION -> for (i in permissions.indices) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Permission Granted: " + permissions[i])
                    doBindService()
                } else if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    Log.d(TAG, "Permission Denied: " + permissions[i])
                    Toast.makeText(this@MainActivity, "Cannot work without the permission", Toast.LENGTH_SHORT).show()
                }
            }
            else -> {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CODE_BLUETOOTH -> {
                if (resultCode == Activity.RESULT_OK) {
                    bb8CommandService?.start()
                } else {
                    bluetoothNotDenied = false
                }
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        if (bb8CommandService != null) {
            bb8CommandService?.removeDiscoveryListener(this)
            bb8CommandService?.stop()
        }
        doUnbindService()
    }

    override fun onDiscoveryDidStart(discoveryAgent: DiscoveryAgent?) {

    }

    override fun handleRobotsAvailable(robots: MutableList<Robot>?) {

    }

    override fun handleRobotChangedState(robot: Robot?, type: RobotChangedStateListener.RobotChangedStateNotificationType?) {
        if (type == RobotChangedStateListener.RobotChangedStateNotificationType.Online) {
            main_connectionStatus.text = String.format(getString(R.string.status_connected), robot?.name)
        } else {
            main_connectionStatus.text = getString(R.string.status_disconnected)
        }
    }

    override fun onDiscoveryDidStop(discoveryAgent: DiscoveryAgent?) {

    }

}
