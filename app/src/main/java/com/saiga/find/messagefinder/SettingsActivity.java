package com.saiga.find.messagefinder;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.util.Log;

import java.util.HashSet;

/** Activity to show currently configured user settings
 * shows user configured keywords and contact number
 * Lists app version and provides option to enable auto suggestion for contacts
 */
public class SettingsActivity extends AppCompatActivity {

    private final static String TAG = "SmsSettingsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // inflate our activity which contains fragment for entire layout
        setContentView(R.layout.settings_activity);
        //@deprecated -- only for debugging app crashes
//        try {
//            Class.forName("dalvik.system.CloseGuard")
//                    .getMethod("setEnabled", boolean.class)
//                    .invoke(null, true);
//        } catch (ReflectiveOperationException e) {
//            throw new RuntimeException(e);
//        }
    }


    /**
     * This fragment will be automatically inflated when we open settings screen on menu click
     */
    public static class AppPreferenceFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            // inflates the app preferences -- to display app version and user configurations
            addPreferencesFromResource(R.xml.app_preferences);
            Preference autoSuggestionPref = findPreference(getString(R.string.auto_suggestion_key));
            // register a onpreference change listener and manipulate state values
            // to determine whether the auto suggestion popup needs to be thrown to user
            bindPreferenceSummaryToValue(autoSuggestionPref);
            // preference which shows the current saved configuration
            // users can look into this configuration later - works after
            // reboot and closing the app
            Preference activeConfigPref = findPreference(getString(R.string.sms_config_key));
            // fetches contact number, keywords and attaches it to configuration
            lookupActiveConfiguration(activeConfigPref);
        }


        @Override
        public boolean onPreferenceChange(Preference preference, Object currentValue) {
            Boolean value = (Boolean)currentValue;
            Log.d(TAG,value.toString());
            if (preference instanceof SwitchPreference){
                // if auto suggestion is enabled/disabled , we add this state internally,
                // so that the dialog will be thrown to user based on the below settings
                    SharedPreferences sharedPreferences = getContext().getSharedPreferences("Settings", Context.MODE_PRIVATE);
                    SharedPreferences.Editor sharedPrefEditor = sharedPreferences.edit();
                    sharedPrefEditor.putBoolean("autoSuggDisabled",!value);
                    sharedPrefEditor.apply();
            }
            return true;
        }

        /**
         * @param preference registers onpreference change listener with this preference
         */
        private void bindPreferenceSummaryToValue(Preference preference) {
            preference.setOnPreferenceChangeListener(this);
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(preference.getContext());
            Boolean preferenceValue = preferences.getBoolean(preference.getKey(), false);
            Log.d(TAG,preferenceValue.toString());
            // to preserve the change on the first time when the user opens the settings screen
            onPreferenceChange(preference,preferenceValue);
        }

        /** fetches contact number, keywords and attaches it to configuration
         *
         * @param preference pass the preference whose summary will be modified
         */
        private void lookupActiveConfiguration(Preference preference) {
            // get the shared preferences file specific to the app and assign the respective fields
           SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(getContext());
            // if stored value is empty, assigning null
            String number = config.getString("contact",null);
            String personName = config.getString("contact_name",null);
            HashSet<String> keywordContents = (HashSet<String>)config.getStringSet("message",null);
            StringBuilder summary = new StringBuilder("Current Configuration:\nPhone number: ");
            // if any of the fields is null, print Not configured in the preference
            if (number == null || number.isEmpty()){
                summary.append(" Not Configured ");
            } else {
                summary.append(number);
            }
            summary.append("\nContact Name: ");
            if (personName == null || personName.isEmpty()){
                summary.append(" Not Configured ");
            } else {
                summary.append(personName);
            }

            summary.append("\nKeywords: ");
            if (keywordContents == null || keywordContents.isEmpty()){
                summary.append(" Not Configured ");
            } else {
                summary.append(keywordContents.toString());
            }
            preference.setSummary(summary);
        }

    }
}