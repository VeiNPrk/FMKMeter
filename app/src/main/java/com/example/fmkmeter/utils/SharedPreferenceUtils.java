package com.example.fmkmeter.utils;

import android.content.Context;

import androidx.preference.PreferenceManager;

public class SharedPreferenceUtils {
    private static final String SETTING_KEY_DELAYED_START = "setting_delayed_start";
    private static final String SETTING_KEY_AUTO_MEASURMENT = "setting_automatic_measurements";
    private static final String SETTING_KEY_DELAYED_START_TIME = "setting_delayed_start_time";
    private static final String SETTING_KEY_AUTO_MEASURMENT_TIME = "setting_auto_measurement_time";

    public static boolean getIsDelayedStart(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
                SETTING_KEY_DELAYED_START, false);
    }

    public static boolean getIsAutoMeasurment(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
                SETTING_KEY_AUTO_MEASURMENT, false);
    }

    public static int getDelayedStartTime(Context context) {
        return Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString(
                SETTING_KEY_DELAYED_START_TIME, "3"));
    }

    public static int getMeasurmentTime(Context context) {
        return Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString(
                SETTING_KEY_AUTO_MEASURMENT_TIME, "4"));
    }
}
