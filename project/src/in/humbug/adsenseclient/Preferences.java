package in.humbug.adsenseclient;

import java.text.MessageFormat;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

//public class Preferences  extends PreferenceActivity implements OnSharedPreferenceChangeListener {
public class Preferences  extends PreferenceActivity {
	
	private OnSharedPreferenceChangeListener listener;
	
	String TAG = "AdsensePreferences";
	public static final String KEY_FETCH_FREQUENCY = "fetch_frequency";
	public static final String KEY_THEME_TYPE = "theme_type";
	private ListPreference mFetchFrequencyPreference;
	private ListPreference mThemeTypePreference;
	private boolean onSharedPreferenceChanged_busy = false;
	
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
	    listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
	    	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
	    		if (onSharedPreferenceChanged_busy) {
	    			return;
	    		}
	    		onSharedPreferenceChanged_busy = true;

	    		try {
	    			if (key.equals(KEY_FETCH_FREQUENCY)) {
	    				updateFrequency();
	    			}
	    			if (key.equals(KEY_THEME_TYPE)) {
	    				updateTheme();
	    			}
	    		} finally {
	    			onSharedPreferenceChanged_busy = false;
	    		}
	    	}
	    };

		prefs.registerOnSharedPreferenceChangeListener(listener);
	    addPreferencesFromResource(R.xml.preferences);
	    mFetchFrequencyPreference = (ListPreference) getPreferenceScreen().findPreference(
                KEY_FETCH_FREQUENCY);
		updateFrequency();
	    mThemeTypePreference = (ListPreference) getPreferenceScreen().findPreference(
                KEY_THEME_TYPE);
	    updateTheme();
	  }
	
	@Override
    public synchronized void onResume() {
        super.onResume();
        /*if ( theme_type != Integer.parseInt(getSp().getString("theme_type", "1")) ) {
        	reload();
        }*/
    }
	
	public void reload() {
		onStop();
		onCreate(getIntent().getExtras());
	}

	protected void updateFrequency() {
		String[] k = getResources().getStringArray(R.array.fetch_frequency_keys);
		String[] d = getResources().getStringArray(R.array.fetch_frequency_display);
		String displayFrequency = d[0];
		String frequency = mFetchFrequencyPreference.getValue();
		for (int i = 0; i < k.length; i++) {
			if (frequency.equals(k[i])) {
				displayFrequency = d[i];
				break;
			}
		}
		MessageFormat sf = new MessageFormat(getText(R.string.summary_preference_frequency)
				.toString());
		mFetchFrequencyPreference.setSummary(sf.format(new Object[] {
				displayFrequency
		}));
	}

	protected void updateTheme() {
		String[] k = getResources().getStringArray(R.array.theme_type_keys);
		String[] d = getResources().getStringArray(R.array.theme_type_display);
		String displayTheme = d[0];
		String theme = mThemeTypePreference.getValue();
		for (int i = 0; i < k.length; i++) {
			if (theme.equals(k[i])) {
				displayTheme = d[i];
				Log.d(TAG,"Loop Display Theme" + displayTheme);
				break;
			}
		}
		Log.d(TAG,"Display Theme" + displayTheme);
		MessageFormat sf = new MessageFormat(getText(R.string.summary_preference_theme_type)
				.toString());
		mThemeTypePreference.setSummary(sf.format(new Object[] {
				displayTheme
		}));
	}

/*
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (onSharedPreferenceChanged_busy) {
			return;
		}
		onSharedPreferenceChanged_busy = true;

		try {
			if (key.equals(KEY_FETCH_FREQUENCY)) {
				updateFrequency();
			}
			if (key.equals(KEY_THEME_TYPE)) {
				updateTheme();
			}
		} finally {
			onSharedPreferenceChanged_busy = false;
		}
	};*/
}
