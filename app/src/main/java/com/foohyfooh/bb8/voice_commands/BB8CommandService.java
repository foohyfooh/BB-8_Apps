package com.foohyfooh.bb8.voice_commands;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import com.foohyfooh.bb8.utils.ColourUtils;
import com.orbotix.ConvenienceRobot;
import com.orbotix.common.DiscoveryAgent;
import com.orbotix.common.DiscoveryAgentEventListener;
import com.orbotix.common.DiscoveryException;
import com.orbotix.common.DiscoveryStateChangedListener;
import com.orbotix.common.Robot;
import com.orbotix.common.RobotChangedStateListener;
import com.orbotix.le.DiscoveryAgentLE;
import com.orbotix.le.RobotLE;
import com.orbotix.le.RobotRadioDescriptor;
import com.orbotix.macro.MacroObject;
import com.orbotix.macro.cmd.Delay;
import com.orbotix.macro.cmd.Fade;
import com.orbotix.macro.cmd.LoopEnd;
import com.orbotix.macro.cmd.LoopStart;
import com.orbotix.macro.cmd.RGB;
import com.orbotix.macro.cmd.Roll;

import java.util.ArrayList;
import java.util.List;

public class BB8CommandService extends Service implements RobotChangedStateListener,
        DiscoveryAgentEventListener, DiscoveryStateChangedListener {

    private static final String TAG = "BB8CommandService";
    public static final String ACTION_MOVE = "com.foohyfooh.bb8.voice_commands.MOVE";
    public static final String ACTION_SPIN = "com.foohyfooh.bb8.voice_commands.SPIN";
    public static final String ACTION_STOP = "com.foohyfooh.bb8.voice_commands.STOP";
    public static final String ACTION_FLASH = "com.foohyfooh.bb8.voice_commands.FLASH";
    public static final String ACTION_BLINK = "com.foohyfooh.bb8.voice_commands.BLINK";
    public static final String ACTION_FADE = "com.foohyfooh.bb8.voice_commands.FADE";
    public static final String EXTRA_COLOUR = "colour";
    public static final String EXTRA_DIRECTION = "direction";
    public static final String EXTRA_ANGLE = "angle";

    private IBinder binder = new BB8Binder();
    private ConvenienceRobot bb8;
    private List<CommandListener> commandListeners = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        if(DiscoveryAgentLE.getInstance() == null) return;
        DiscoveryAgentLE.getInstance().addRobotStateListener(this);
        DiscoveryAgentLE.getInstance().addDiscoveryListener(this);
        DiscoveryAgentLE.getInstance().addDiscoveryChangedStateListener(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(bb8 == null) return START_NOT_STICKY;
        String action = intent.getAction();
        int angle = intent.getIntExtra(EXTRA_ANGLE, 0);
        int[] colour = intent.getIntArrayExtra(EXTRA_COLOUR);
        switch (action){
            case ACTION_MOVE: //Move for 5 seconds
                MacroObject moveMacro = new MacroObject();
                moveMacro.addCommand(new Roll(5f, angle, 0));
                moveMacro.addCommand(new Delay(2000));
                moveMacro.addCommand(new Roll( 0.0f, 0, 0));
                bb8.playMacro(moveMacro);
                break;
            case ACTION_SPIN:
                break;
            case ACTION_STOP:
                bb8.stop();
                break;
            case ACTION_FLASH:
                break;
            case ACTION_BLINK:
                MacroObject blinkMacro = new MacroObject();
                blinkMacro.addCommand(new LoopStart(15));
                blinkMacro.addCommand(new RGB(colour[0], colour[1], colour[2], 1000));
                blinkMacro.addCommand(new RGB(0, 0, 0, 1000));
                blinkMacro.addCommand(new LoopEnd());
                bb8.playMacro(blinkMacro);
                break;
            case ACTION_FADE:
                break;
        }
        return START_NOT_STICKY;
    }

    public void startDiscovery() {
        DiscoveryAgentLE discoveryAgentLE = DiscoveryAgentLE.getInstance();
        if(discoveryAgentLE != null && !discoveryAgentLE.isDiscovering()) {
            try {
                RobotRadioDescriptor robotRadioDescriptor = new RobotRadioDescriptor();
                robotRadioDescriptor.setNamePrefixes(new String[]{"BB-"});
                discoveryAgentLE.setRadioDescriptor(robotRadioDescriptor);
                discoveryAgentLE.startDiscovery(this);
            } catch (DiscoveryException e) {
                Log.e(TAG, "DiscoveryException: " + e.getMessage());
            }
        }
    }

    private float[] extractColourFromIntent(Intent intent){
        String colour = intent.getStringExtra(EXTRA_COLOUR);
        int[] colours =  ColourUtils.extractColoursToArray(colour);
        return new float[]{colours[0] / 255f, colours[1] / 255f, colours[2] / 255f};
    }

    @SuppressLint("NewApi")
    protected void start() {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startDiscovery();
        }
    }


    protected void stop() {
        //If the DiscoveryAgent is in discovery mode, stop it.
        if(DiscoveryAgentLE.getInstance().isDiscovering()) {
            DiscoveryAgentLE.getInstance().stopDiscovery();
        }

        //If a robot is connected to the device, disconnect it
        if(bb8 != null) {
            bb8.disconnect();
            bb8 = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(DiscoveryAgentLE.getInstance() != null) return;
        DiscoveryAgentLE.getInstance().removeRobotStateListener(this);
        DiscoveryAgentLE.getInstance().removeDiscoveryListener(this);
        DiscoveryAgentLE.getInstance().removeDiscoveryChangedStateListener(this);

    }

    @Override
    public void handleRobotChangedState(Robot robot, RobotChangedStateListener.RobotChangedStateNotificationType type) {
        Log.d(TAG, "handleRobotChangedState: " + robot.getName() + " State: " + type.name());

        switch(type) {
            case Online:
                if (robot instanceof RobotLE) {
                    ((RobotLE) robot).setDeveloperMode(true);
                }
                bb8 = new ConvenienceRobot(robot);
                break;
        }
        for (CommandListener commandListener : commandListeners) {
            commandListener.handleRobotChangedState(robot, type);
        }
    }

    @Override
    public void handleRobotsAvailable(List<Robot> robots) {
        Log.d(TAG, "handleRobotsAvailable: robots: " + robots.size());
        for (Robot robot : robots) {
            Log.d(TAG, "handleRobotsAvailable: " + robot.getName());
        }
        for (CommandListener commandListener : commandListeners) {
            commandListener.handleRobotsAvailable(robots);
        }
    }

    @Override
    public void onDiscoveryDidStart(DiscoveryAgent discoveryAgent) {
        Log.d(TAG, "onDiscoveryDidStart");
        for (CommandListener commandListener : commandListeners) {
            commandListener.onDiscoveryDidStart(discoveryAgent);
        }
    }

    @Override
    public void onDiscoveryDidStop(DiscoveryAgent discoveryAgent) {
        Log.d(TAG, "onDiscoveryDidStop");
        for (CommandListener commandListener : commandListeners) {
            commandListener.onDiscoveryDidStop(discoveryAgent);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void addStateListener(CommandListener commandListener) {
        commandListeners.add(commandListener);
    }

    public class BB8Binder extends Binder {
        public BB8CommandService getService(){
            return BB8CommandService.this;
        }
    }

    public interface CommandListener extends DiscoveryStateChangedListener, DiscoveryAgentEventListener,
            RobotChangedStateListener {
    }
}
