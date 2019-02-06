package owenspangler.stapleshighschoolscheduler;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.MenuItem;

public class ScheduleInputActivity extends AppCompatPreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //setContentView(R.layout.activity_input_real_class);
        super.onCreate(savedInstanceState);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // load settings fragment
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MainPreferenceFragment()).commit();
    }

    public static class MainPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_schedule_input);

            // Class Name Text change listener. If more periods added, add a listener here.
            bindPreferenceSummaryToValue(findPreference("key_schedule_period_1_name"));
            bindPreferenceSummaryToValue(findPreference("key_schedule_period_2_name"));
            bindPreferenceSummaryToValue(findPreference("key_schedule_period_3_name"));
            bindPreferenceSummaryToValue(findPreference("key_schedule_period_4_name"));
            bindPreferenceSummaryToValue(findPreference("key_schedule_period_5_name"));
            bindPreferenceSummaryToValue(findPreference("key_schedule_period_6_name"));
            bindPreferenceSummaryToValue(findPreference("key_schedule_period_7_name"));
            bindPreferenceSummaryToValue(findPreference("key_schedule_period_8_name"));

            //Class Info Text Listener
            bindPreferenceSummaryToValue(findPreference("key_schedule_period_1_info"));
            bindPreferenceSummaryToValue(findPreference("key_schedule_period_2_info"));
            bindPreferenceSummaryToValue(findPreference("key_schedule_period_3_info"));
            bindPreferenceSummaryToValue(findPreference("key_schedule_period_4_info"));
            bindPreferenceSummaryToValue(findPreference("key_schedule_period_5_info"));
            bindPreferenceSummaryToValue(findPreference("key_schedule_period_6_info"));
            bindPreferenceSummaryToValue(findPreference("key_schedule_period_7_info"));
            bindPreferenceSummaryToValue(findPreference("key_schedule_period_8_info"));

            //Class Type Text Listener
            bindPreferenceSummaryToValue(findPreference("key_schedule_period_1_type"));
            bindPreferenceSummaryToValue(findPreference("key_schedule_period_2_type"));
            bindPreferenceSummaryToValue(findPreference("key_schedule_period_3_type"));
            bindPreferenceSummaryToValue(findPreference("key_schedule_period_4_type"));
            bindPreferenceSummaryToValue(findPreference("key_schedule_period_5_type"));
            bindPreferenceSummaryToValue(findPreference("key_schedule_period_6_type"));
            bindPreferenceSummaryToValue(findPreference("key_schedule_period_7_type"));
            bindPreferenceSummaryToValue(findPreference("key_schedule_period_8_type"));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private static void bindPreferenceSummaryToValue(Preference preference) {
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            String stringValue = newValue.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            } else if (preference instanceof EditTextPreference) { //Add Class Name Text Inputs Here so they update string
                if (preference.getKey().equals("key_schedule_period_1_name")) {
                    // update the changed gallery name to summary filed
                    preference.setSummary(stringValue);
                }else if (preference.getKey().equals("key_schedule_period_2_name")) {
                    // update the changed gallery name to summary filed
                    preference.setSummary(stringValue);
                }else if (preference.getKey().equals("key_schedule_period_3_name")) {
                    // update the changed gallery name to summary filed
                    preference.setSummary(stringValue);
                }else if (preference.getKey().equals("key_schedule_period_4_name")) {
                    // update the changed gallery name to summary filed
                    preference.setSummary(stringValue);
                }else if (preference.getKey().equals("key_schedule_period_5_name")) {
                    // update the changed gallery name to summary filed
                    preference.setSummary(stringValue);
                }else if (preference.getKey().equals("key_schedule_period_6_name")) {
                    // update the changed gallery name to summary filed
                    preference.setSummary(stringValue);
                }else if (preference.getKey().equals("key_schedule_period_7_name")) {
                    // update the changed gallery name to summary filed
                    preference.setSummary(stringValue);
                }else if (preference.getKey().equals("key_schedule_period_8_name")) {
                    // update the changed gallery name to summary filed
                    preference.setSummary(stringValue);
                }else if (preference.getKey().equals("key_schedule_period_1_info")) {
                    // update the changed gallery info to summary filed
                    preference.setSummary(stringValue);
                }else if (preference.getKey().equals("key_schedule_period_2_info")) {
                    // update the changed gallery info to summary filed
                    preference.setSummary(stringValue);
                }else if (preference.getKey().equals("key_schedule_period_3_info")) {
                    // update the changed gallery info to summary filed
                    preference.setSummary(stringValue);
                }else if (preference.getKey().equals("key_schedule_period_4_info")) {
                    // update the changed gallery info to summary filed
                    preference.setSummary(stringValue);
                }else if (preference.getKey().equals("key_schedule_period_5_info")) {
                    // update the changed gallery info to summary filed
                    preference.setSummary(stringValue);
                }else if (preference.getKey().equals("key_schedule_period_6_info")) {
                    // update the changed gallery info to summary filed
                    preference.setSummary(stringValue);
                }else if (preference.getKey().equals("key_schedule_period_7_info")) {
                    // update the changed gallery info to summary filed
                    preference.setSummary(stringValue);
                }else if (preference.getKey().equals("key_schedule_period_8_info")) {
                    // update the changed gallery info to summary filed
                    preference.setSummary(stringValue);
                }
            } else {
                preference.setSummary(stringValue);
            }
            return true;
        }
    };
}