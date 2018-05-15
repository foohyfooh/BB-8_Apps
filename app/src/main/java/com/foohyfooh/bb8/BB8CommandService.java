package com.foohyfooh.bb8;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

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
import com.orbotix.macro.cmd.LoopEnd;
import com.orbotix.macro.cmd.LoopStart;
import com.orbotix.macro.cmd.RGB;
import com.orbotix.macro.cmd.Roll;

import java.util.ArrayList;
import java.util.List;

public class BB8CommandService extends Service implements RobotChangedStateListener,
        DiscoveryAgentEventListener, DiscoveryStateChangedListener {

    private static final String TAG = "BB8CommandService";

    public static final String ACTION_BLINK = "com.foohyfooh.bb8.action.BLINK";
    public static final String ACTION_FLASH = "com.foohyfooh.bb8.action.FLASH";
    public static final String ACTION_MOVE = "com.foohyfooh.bb8.MOVE";
    public static final String ACTION_SPIN = "com.foohyfooh.bb8.SPIN";
    public static final String ACTION_STOP = "com.foohyfooh.bb8.STOP";
    public static final String ACTION_FADE = "com.foohyfooh.bb8.FADE";
    public static final String EXTRA_DIRECTION = "direction";
    public static final String EXTRA_COLOUR = "colour";
    public static final String EXTRA_ANGLE = "angle";

    private IBinder binder = new BB8Binder();
    private ConvenienceRobot bb8;
    private List<CommandListener> commandListeners = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        DiscoveryAgentLE.getInstance().addRobotStateListener(this);
        DiscoveryAgentLE.getInstance().addDiscoveryListener(this);
        DiscoveryAgentLE.getInstance().addDiscoveryChangedStateListener(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        int angle = intent.getIntExtra(EXTRA_ANGLE, 0);
        int[] colours = intent.getIntArrayExtra(EXTRA_COLOUR);
        switch (action){
            case ACTION_BLINK:
                blinkColour(colours[0], colours[1], colours[2]);
                break;
            case ACTION_FLASH:
                blinkColour(colours[0], colours[1], colours[2]);
                break;
            case ACTION_MOVE: //Move for 2 seconds
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

    public void changeColour(float red, float green, float blue){
        if(bb8 != null){
            bb8.setLed(red, green, blue);
        }
    }

    public void blinkColour(int red, int green, int blue){
        if(bb8 != null){
            MacroObject blinkMacro = new MacroObject();
            blinkMacro.addCommand(new LoopStart(15));
            blinkMacro.addCommand(new RGB(red, green, blue, 1000));
            blinkMacro.addCommand(new RGB(0, 0, 0, 1000));
            blinkMacro.addCommand(new LoopEnd());
            bb8.playMacro(blinkMacro);
        }
    }

    public void start() {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startDiscovery();
        }
    }

    public void stop() {
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
            case Connecting:
                Log.d(TAG, "State Changed to Connecting");
                break;
            case Connected:
                Log.d(TAG, "State Changed to Connected");
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
