package com.example.fmkmeter;

import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

public class SettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.fragment_settings, rootKey);
        EditTextPreference countingPreference = findPreference("cnt_n_last");
        if (countingPreference != null) {
            countingPreference.setOnBindEditTextListener(
                    new EditTextPreference.OnBindEditTextListener() {
                        @Override
                        public void onBindEditText(@NonNull EditText editText) {
                            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                        }
                    });
            countingPreference.setSummaryProvider(new Preference.SummaryProvider<EditTextPreference>() {
                @Override
                public CharSequence provideSummary(EditTextPreference preference) {
                    String text = preference.getText();
                    if (TextUtils.isEmpty(text)){
                        return "Не выбрано";
                    }
                    return "Кол-во: " + text;
                }
            });
        }
        EditTextPreference timeDimensionPreference = findPreference("setting_auto_measurement_time");
        if (timeDimensionPreference != null) {
            timeDimensionPreference.setOnBindEditTextListener(
                    new EditTextPreference.OnBindEditTextListener() {
                        @Override
                        public void onBindEditText(@NonNull EditText editText) {
                            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                        }
                    });
            timeDimensionPreference.setSummaryProvider(new Preference.SummaryProvider<EditTextPreference>() {
                @Override
                public CharSequence provideSummary(EditTextPreference preference) {
                    String text = preference.getText();
                    if (TextUtils.isEmpty(text)){
                        return "Не выбрано";
                    }
                    return "Измерение будет длиться " + text + " сек.";
                }
            });
        }
        EditTextPreference delayedStartTimePreference = findPreference("setting_delayed_start_time");
        if (delayedStartTimePreference != null) {
            delayedStartTimePreference.setOnBindEditTextListener(
                    new EditTextPreference.OnBindEditTextListener() {
                        @Override
                        public void onBindEditText(@NonNull EditText editText) {
                            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                        }
                    });
            delayedStartTimePreference.setSummaryProvider(new Preference.SummaryProvider<EditTextPreference>() {
                @Override
                public CharSequence provideSummary(EditTextPreference preference) {
                    String text = preference.getText();
                    if (TextUtils.isEmpty(text)){
                        return "Не выбрано";
                    }
                    return "Измеренияе начнётся через " + text + " сек. после нажатия кнопки";
                }
            });
        }
        final SwitchPreference tfIntegr = (SwitchPreference) findPreference("tf_integr");
        final SwitchPreference visibleFirstIntegr = (SwitchPreference) findPreference("tf_visible_first_integr");
        final SwitchPreference useNewIntegr = (SwitchPreference) findPreference("tf_use_new_integr");
        visibleFirstIntegr.setEnabled(tfIntegr.isChecked());
        useNewIntegr.setEnabled(tfIntegr.isChecked());
        tfIntegr.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue){
                boolean isEnabled = ((Boolean) newValue).booleanValue();
                visibleFirstIntegr.setEnabled(isEnabled);
                useNewIntegr.setEnabled(isEnabled);
                return true;
            }
        });
    }
}
