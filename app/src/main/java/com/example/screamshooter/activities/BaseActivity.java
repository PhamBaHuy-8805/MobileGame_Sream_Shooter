package com.example.screamshooter.activities;

import android.app.Activity;
import android.content.Context;

import com.example.screamshooter.utils.LocaleHelper;

public abstract class BaseActivity extends Activity {

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.wrap(newBase));
    }
}
