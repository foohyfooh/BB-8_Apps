package com.foohyfooh.bb8.voice_commands;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.foohyfooh.bb8.R;
import com.orbotix.common.DiscoveryAgent;
import com.orbotix.common.Robot;
import com.orbotix.common.RobotChangedStateListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class VoiceCommandsActivity extends AppCompatActivity implements BB8CommandService.CommandListener  {

    private static final int REQUEST_CODE_LOCATION_PERMISSION = 1;
    private static final int REQUEST_CODE_BLUETOOTH = 2;
    private static final int REQUEST_CODE_SPEECH_COMMAND = 3;
    private static final String TAG = "Voice: ";

    private TextView connectionStatus;
    private boolean isServiceBound, bluetoothNotDenied = true;
    private BB8CommandService bb8CommandService;
    private ServiceConnection serviceConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.voice_commands_activity_main);
        connectionStatus = findViewById(R.id.voiceCommands_connectionStatus);
        Button issueCommand = findViewById(R.id.voiceCommands_issueCommand);

        issueCommand.setOnClickListener(view -> {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            startActivityForResult(intent, REQUEST_CODE_SPEECH_COMMAND);
        });

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int hasLocationPermission = checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
            if(hasLocationPermission != PackageManager.PERMISSION_GRANTED) {
                //Log.e(TAG, "Location permission has not already been granted");
                List<String> permissions = new ArrayList<>();
                permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
                requestPermissions(permissions.toArray(new String[permissions.size()]), REQUEST_CODE_LOCATION_PERMISSION);
            } else {
                //Log.d(TAG, "Location permission already granted");
                doBindService();
            }
        }else{
            doBindService();
        }
    }

    private void handleRequirements(){
        boolean settingNotFound = false, locationEnabled = false;
        try {
            locationEnabled = Settings.Secure.getInt(getContentResolver(),
                    Settings.Secure.LOCATION_MODE) != Settings.Secure.LOCATION_MODE_OFF;
        } catch (Settings.SettingNotFoundException e) {
            settingNotFound = true;
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !settingNotFound && !locationEnabled){
            new AlertDialog.Builder(this)
                    .setTitle(R.string.location_access_title)
                    .setMessage(R.string.location_access_message)
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(R.string.enable, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent enableLocationIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(enableLocationIntent);
                        }
                    })
                    .show();
        }else if(bluetoothNotDenied && BluetoothAdapter.getDefaultAdapter() != null
                && !BluetoothAdapter.getDefaultAdapter().isEnabled()){
            Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetoothIntent, REQUEST_CODE_BLUETOOTH);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        handleRequirements();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(bb8CommandService != null) bb8CommandService.start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_LOCATION_PERMISSION: {
                for(int i = 0; i < permissions.length; i++) {
                    if(grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        Log.d(TAG, "Permission Granted: " + permissions[i]);
                        doBindService();
                    } else if(grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        Log.d(TAG, "Permission Denied: " + permissions[i]);
                        Toast.makeText(VoiceCommandsActivity.this, "Cannot work without the permission", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
            default: {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case REQUEST_CODE_BLUETOOTH:
                if(resultCode == RESULT_OK){
                    if(bb8CommandService != null) bb8CommandService.start();
                }else{
                    bluetoothNotDenied = false;
                }
                break;
            case REQUEST_CODE_SPEECH_COMMAND:
                if(resultCode == RESULT_OK){
                    List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String command = results.get(0);
                    Log.d(TAG + " COMMAND", command);

                    Intent serviceIntent = new Intent(this, BB8CommandService.class);
                    if(command.contains("move"))
                        serviceIntent.setAction(BB8CommandService.ACTION_MOVE);
                    else if(command.contains("spin"))
                        serviceIntent.setAction(BB8CommandService.ACTION_SPIN);
                    else if(command.contains("stop"))
                        serviceIntent.setAction(BB8CommandService.ACTION_STOP);
                    else if(command.contains("blink") || command.contains("strobe"))
                        serviceIntent.setAction(BB8CommandService.ACTION_BLINK);
                    else if(command.contains("fade"))
                        serviceIntent.setAction(BB8CommandService.ACTION_FADE);
                    else return;

                    if(command.contains("forward"))
                        serviceIntent.putExtra(BB8CommandService.EXTRA_ANGLE, 0);
                    else if(command.contains("back"))
                        serviceIntent.putExtra(BB8CommandService.EXTRA_ANGLE, 180);
                    else if(command.contains("left"))
                        serviceIntent.putExtra(BB8CommandService.EXTRA_ANGLE, 90);
                    else if(command.contains("right"))
                        serviceIntent.putExtra(BB8CommandService.EXTRA_ANGLE, 270);

                    serviceIntent.putExtra(BB8CommandService.EXTRA_COLOUR, extraColourFromCommand(command));

                    startService(serviceIntent);
                }
                break;
        }
    }

    public static int[] extraColourFromCommand(String command){
        if(command.contains("random")){
            Random random = new Random();
            return new int[]{random.nextInt(256), random.nextInt(256), random.nextInt(256)};
        }else if(command.contains("red")) return new int[]{255, 0, 0};
        else if(command.contains("blue")) return new int[]{0, 0, 255};
        else if(command.contains("green")) return new int[]{0, 255, 0};
        else if(command.contains("purple")) return new int[]{128, 0, 128};
        else if(command.contains("pink")) return new int[]{255, 192, 203};
        else if (command.contains("yellow")) return new int[]{255, 255, 0};

        Random random = new Random();
        return new int[]{random.nextInt(256), random.nextInt(256), random.nextInt(256)};
    }

    void doBindService() {
        serviceConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName className, IBinder service) {
                bb8CommandService = ((BB8CommandService.BB8Binder)service).getService();
                bb8CommandService.addStateListener(VoiceCommandsActivity.this);
                //bb8CommandService.startDiscovery();
                bb8CommandService.start();
            }

            public void onServiceDisconnected(ComponentName className) {
                bb8CommandService = null;
            }
        };
        bindService(new Intent(this, BB8CommandService.class), serviceConnection, Context.BIND_AUTO_CREATE);
        isServiceBound = true;
    }

    void doUnbindService() {
        if (isServiceBound) {
            unbindService(serviceConnection);
            isServiceBound = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(bb8CommandService != null) bb8CommandService.stop();
        doUnbindService();
    }

    @Override
    public void handleRobotsAvailable(List<Robot> list) {}

    @Override
    public void onDiscoveryDidStart(DiscoveryAgent discoveryAgent) {}

    @Override
    public void onDiscoveryDidStop(DiscoveryAgent discoveryAgent) {}

    @Override
    public void handleRobotChangedState(Robot robot, RobotChangedStateListener.RobotChangedStateNotificationType type) {
        if(type == RobotChangedStateListener.RobotChangedStateNotificationType.Online){
            connectionStatus.setText(String.format(getString(R.string.status_connected), robot.getName()));
        }else{
            connectionStatus.setText(getString(R.string.status_disconnected));
        }
    }
}
