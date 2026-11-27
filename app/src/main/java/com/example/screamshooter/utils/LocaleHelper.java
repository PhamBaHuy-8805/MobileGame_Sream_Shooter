package com.example.screamshooter.utils;

import android.content.Context;
import android.content.res.Configuration;

import com.example.screamshooter.data.SettingsManager;

import java.util.Locale;

public final class LocaleHelper {

    private LocaleHelper() {
    }

    public static Context wrap(Context context) {
        String language = new SettingsManager(context).getLanguage();
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Configuration configuration =
                new Configuration(context.getResources().getConfiguration());
        configuration.setLocale(locale);

        return context.createConfigurationContext(configuration);
    }
}
