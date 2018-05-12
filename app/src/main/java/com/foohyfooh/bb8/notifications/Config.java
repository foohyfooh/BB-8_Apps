package com.foohyfooh.bb8.notifications;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;
import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Entity(tableName = Config.TABLE_NAME)
public class Config {

    public static final String TABLE_NAME = "Config";
    public static final String COLUMN_PACKAGE = "packageName";
    public static final String COLUMN_HEX     = "hex";
    public static final String COLUMN_PATTERN = "pattern";

    @NonNull @PrimaryKey @ColumnInfo(name = COLUMN_PACKAGE) private String packageName;
    @ColumnInfo(name = COLUMN_HEX) private String hexColour;
    @ColumnInfo(name = COLUMN_PATTERN) private @Pattern String pattern;

    public Config(String packageName, String hexColour, @Pattern String pattern) {
        this.packageName = packageName;
        this.hexColour = hexColour;
        this.pattern = pattern;
    }

    @Ignore
    public Config(String packageName) {
        this(packageName, "#ffffff", BB8CommandService.ACTION_BLINK);
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getHexColour() {
        return hexColour;
    }

    public void setHexColour(String hexColour) {
        this.hexColour = hexColour;
    }

    public @Pattern String getPattern() {
        return pattern;
    }

    public void setPattern(@Pattern String pattern) {
        this.pattern = pattern;
    }

    @StringDef({BB8CommandService.ACTION_BLINK, BB8CommandService.ACTION_FLASH})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Pattern{}

}
