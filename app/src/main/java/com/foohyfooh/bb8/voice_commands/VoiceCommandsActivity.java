package com.foohyfooh.bb8.voice_commands;

import android.content.Intent;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import com.foohyfooh.bb8.BB8CommandService;
import com.foohyfooh.bb8.R;
import com.foohyfooh.bb8.utils.ColourUtils;

import java.util.List;

public class VoiceCommandsActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_SPEECH_COMMAND = 3;
    private static final String TAG = "Voice: ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.voice_commands_activity_main);
        Button issueCommand = findViewById(R.id.voiceCommands_issueCommand);

        issueCommand.setOnClickListener(view -> {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            startActivityForResult(intent, REQUEST_CODE_SPEECH_COMMAND);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
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
        if(command.contains("random")) return ColourUtils.generateRandomColourArray();
        else if(command.contains("red")) return ColourUtils.RED;
        else if(command.contains("blue")) return ColourUtils.BLUE;
        else if(command.contains("green")) return ColourUtils.GREEN;
        else if(command.contains("purple")) return ColourUtils.PURPLE;
        else if(command.contains("pink")) return ColourUtils.PINK;
        else if (command.contains("yellow")) return ColourUtils.YELLOW;
        return ColourUtils.generateRandomColourArray();
    }

}
