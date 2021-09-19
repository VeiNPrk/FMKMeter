package com.example.fmkmeter.utils;

import android.content.Context;

import androidx.preference.PreferenceManager;

public class SharedPreferenceUtils {
    private static final String SETTING_KEY_DELAYED_START = "setting_delayed_start";
    private static final String SETTING_KEY_AUTO_MEASURMENT = "setting_automatic_measurements";
    private static final String SETTING_KEY_DELAYED_START_TIME = "setting_delayed_start_time";
    private static final String SETTING_KEY_AUTO_MEASURMENT_TIME = "setting_auto_measurement_time";
    private static final String KEY_TF_INTEGR="tf_integr";
    private static final String KEY_TF_VISIBLE_FIRST_INTEGR="tf_visible_first_integr";
    private static final String KEY_TF_USE_NEW_INTEGR="tf_use_new_integr";
    private static final String KEY_CNT_N_LAST="cnt_n_last";

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

    public static boolean getIsIntegrate(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
                KEY_TF_INTEGR, false);
    }

    public static boolean getIsVisibleFirstIntegrate(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
                KEY_TF_VISIBLE_FIRST_INTEGR, false);
    }

    public static boolean getIsUseNewIntegrate(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
                KEY_TF_USE_NEW_INTEGR, false);
    }

    public static int getCntNLast(Context context){
        return Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString(
                KEY_CNT_N_LAST, "2000"));
    }

}
