package com.foohyfooh.bb8.notifications;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.foohyfooh.bb8.BB8CommandService;
import com.foohyfooh.bb8.R;
import com.foohyfooh.bb8.utils.ColourUtils;
import com.pes.androidmaterialcolorpickerdialog.ColorPicker;

import java.util.Arrays;

public class ConfigureActivity extends AppCompatActivity {

    public static final String EXTRA_PACKAGE_NAME = "Configure.packageName";
    private Config config;
    private String packageName;
    private ConfigDao dao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notifications_activity_configure);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(view -> finish());

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        dao = ConfigDatabase.getInstance(this).dao();
        packageName = getIntent().getStringExtra(EXTRA_PACKAGE_NAME);
        config = dao.get(packageName);

        final Button changeColour = findViewById(R.id.configure_colour);
        changeColour.setBackgroundColor(Color.parseColor(config.getHexColour()));

        int[] rgbColours = ColourUtils.extractColoursToArray(config.getHexColour());
        final ColorPicker colorPicker = new ColorPicker(this, rgbColours[0], rgbColours[1], rgbColours[2]);

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getId() == R.id.configure_colour){
                    colorPicker.show();
                    Button ok = colorPicker.findViewById(R.id.okColorButton);
                    ok.setOnClickListener(this);
                }else if(view.getId() == R.id.okColorButton){
                    String hexColour = "#" + ColourUtils.intToHex(colorPicker.getRed()) +
                            ColourUtils.intToHex(colorPicker.getGreen()) +
                            ColourUtils.intToHex(colorPicker.getBlue());
                    Log.d("AppInfoAdapter", "Hex Colour " + hexColour);
                    colorPicker.dismiss();
                    changeColour.setBackgroundColor(colorPicker.getColor());
                    config.setHexColour(hexColour);
                }
            }
        };
        changeColour.setOnClickListener(onClickListener);

        String[] patterns = {"Blink", "Fade"};
        Spinner pattern = findViewById(R.id.configure_pattern);
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Arrays.asList(patterns));
        pattern.setAdapter(adapter);

        if(config.getPattern().equals(BB8CommandService.ACTION_BLINK)) pattern.setSelection(0);
        else if(config.getPattern().equals(BB8CommandService.ACTION_FADE)) pattern.setSelection(1);

        pattern.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
                String item = adapter.getItem(pos);
                if(item == null) return;
                switch (item){
                    case "Blink":
                        config.setPattern(BB8CommandService.ACTION_BLINK);
                        break;
                    case "Fade":
                        config.setPattern(BB8CommandService.ACTION_FADE);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        Button disable = findViewById(R.id.configure_disable);
        disable.setOnClickListener(view -> {
            dao.delete(packageName);
            finish();
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        dao.update(config);
    }
}
