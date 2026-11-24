package com.example.screamshooter.ui.setting;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import com.example.screamshooter.R;

public class SettingsActivity extends AppCompatActivity {

    private SeekBar seekBarVolume;
    private TextView tvVolumeValue;
    private RadioGroup radioGroupLanguage;

    private AudioManager audioManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);

        seekBarVolume = findViewById(R.id.seekBarVolume);

        tvVolumeValue = findViewById(R.id.tvVolumeValue);

        radioGroupLanguage =
                findViewById(R.id.radioGroupLanguage);

        audioManager = (AudioManager)
                getSystemService(Context.AUDIO_SERVICE);

        findViewById(R.id.btnBack)
                .setOnClickListener(v -> finish());

        initVolumeSettings();

        initLanguageSettings();
    }

    private void initVolumeSettings() {

        if (audioManager == null) {

            seekBarVolume.setEnabled(false);

            return;
        }

        int maxVolume = audioManager.getStreamMaxVolume(
                AudioManager.STREAM_MUSIC
        );

        int currentVolume = audioManager.getStreamVolume(
                AudioManager.STREAM_MUSIC
        );

        seekBarVolume.setMax(maxVolume);

        seekBarVolume.setProgress(currentVolume);

        updateVolumeText(currentVolume, maxVolume);

        seekBarVolume.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onProgressChanged(
                            SeekBar seekBar,
                            int progress,
                            boolean fromUser
                    ) {

                        updateVolumeText(
                                progress,
                                seekBar.getMax()
                        );

                        if (fromUser) {

                            audioManager.setStreamVolume(
                                    AudioManager.STREAM_MUSIC,
                                    progress,
                                    0
                            );
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(
                            SeekBar seekBar
                    ) {
                    }

                    @Override
                    public void onStopTrackingTouch(
                            SeekBar seekBar
                    ) {
                    }
                }
        );
    }

    private void updateVolumeText(
            int currentVolume,
            int maxVolume
    ) {

        int percent = 0;

        if (maxVolume > 0) {

            percent = Math.round(
                    currentVolume * 100f / maxVolume
            );
        }

        tvVolumeValue.setText(
                getString(
                        R.string.volume_percent,
                        percent
                )
        );
    }

    private void initLanguageSettings() {

        String currentLanguage =
                AppCompatDelegate
                        .getApplicationLocales()
                        .toLanguageTags();

        if (currentLanguage.startsWith("en")) {

            radioGroupLanguage.check(
                    R.id.radioEnglish
            );

        } else {

            radioGroupLanguage.check(
                    R.id.radioVietnamese
            );
        }

        radioGroupLanguage.setOnCheckedChangeListener(
                (group, checkedId) -> {

                    String selectedLanguage;

                    if (checkedId == R.id.radioEnglish) {

                        selectedLanguage = "en";

                    } else {

                        selectedLanguage = "vi";
                    }

                    changeLanguage(selectedLanguage);
                }
        );
    }

    private void changeLanguage(String languageCode) {

        LocaleListCompat selectedLocales =
                LocaleListCompat.forLanguageTags(
                        languageCode
                );

        if (!selectedLocales.equals(
                AppCompatDelegate.getApplicationLocales()
        )) {

            AppCompatDelegate.setApplicationLocales(
                    selectedLocales
            );
        }
    }
}