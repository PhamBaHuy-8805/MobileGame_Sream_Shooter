package com.example.screamshooter.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.screamshooter.utils.Constants;

public class SettingsManager {

    public static final String LANGUAGE_VI = "vi";
    public static final String LANGUAGE_EN = "en";

    private static final String KEY_VOLUME = "key_volume";
    private static final String KEY_LANGUAGE = "key_language";
    private static final int DEFAULT_VOLUME = 70;

    private final SharedPreferences prefs;

    public SettingsManager(Context context) {
        Context appContext = context.getApplicationContext();
        Context target = appContext != null ? appContext : context;
        prefs = target.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
    }

    public int getVolume() {
        return prefs.getInt(KEY_VOLUME, DEFAULT_VOLUME);
    }

    public void setVolume(int volume) {
        prefs.edit().putInt(KEY_VOLUME, Math.max(0, Math.min(100, volume))).apply();
    }

    public String getLanguage() {
        String language = prefs.getString(KEY_LANGUAGE, LANGUAGE_VI);
        return LANGUAGE_EN.equals(language) ? LANGUAGE_EN : LANGUAGE_VI;
    }

    public void setLanguage(String language) {
        prefs.edit().putString(
                KEY_LANGUAGE,
                LANGUAGE_EN.equals(language) ? LANGUAGE_EN : LANGUAGE_VI
        ).apply();
    }
}
