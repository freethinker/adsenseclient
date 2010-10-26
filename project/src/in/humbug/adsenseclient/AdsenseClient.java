package in.humbug.adsenseclient;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.AdapterView.OnItemClickListener;

import com.admob.android.ads.AdManager;

public class AdsenseClient extends Activity {
	private static final boolean D = true;
	String TAG = "AdsenseClient";
	private ListView mTimeFrameView;
	int theme_type;
	public static String AUTHORITY = "in.humbug.adsenseclient.adsensecontentprovider";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/adsense/afc");
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "+++ ON CREATE +++");
        File adsenseDataDir = new File("/sdcard/humbug_adsense_client/");
        adsenseDataDir.mkdirs();
        AdManager.setTestDevices( new String[] {
        		  AdManager.TEST_EMULATOR,           // Android emulator
        		  "D3383E853BE5F647F48F591344D1136C",  // My G1 phone
        } );
		int verified = getSp().getInt("verified", 0);
		theme_type = Integer.parseInt(getSp().getString("theme_type", "1"));
		boolean automatic_updates = getSp().getBoolean("automatic_updates", true);
		AdsenseClientState appState = ((AdsenseClientState)getApplicationContext());
		boolean AdsenseDataServiceState = appState.getServiceState();
        Cursor managedCursor = null;
        managedCursor = managedQuery(CONTENT_URI, null, null, null, null);
        if (managedCursor != null && verified != 0 && theme_type == 2) { 
        	
	        setContentView(R.layout.main);
	        mTimeFrameView = (ListView) findViewById(R.id.list);
	
	        ListAdapter mTimeFrameCursorAdapter = new SimpleCursorAdapter(this,
	        		R.layout.item,
	        		managedCursor,
	            	new String[] { "Time", "EarningCur", "ClicksPerImps", "CTR+", "eCPM+", "PerClick" },
	        		new int[] { R.id.timeframe, R.id.earnings, R.id.clicksperimps, R.id.ctr, R.id.ecpm, R.id.perclick });
	
	        mTimeFrameView.setAdapter(mTimeFrameCursorAdapter);
	        

        	if ( automatic_updates == true && AdsenseDataServiceState == false)
        		startService(new Intent(this, AdsenseDataService.class));
        	else if ( automatic_updates == false && AdsenseDataServiceState == true)
        		stopService(new Intent(this, AdsenseDataService.class));
        	
        } else if (managedCursor != null && verified != 0 && theme_type == 1) { 
         	
	        setContentView(R.layout.main);
	        mTimeFrameView = (ListView) findViewById(R.id.list);
	
	        ListAdapter mTimeFrameCursorAdapter = new SimpleCursorAdapter(this,
	        		R.layout.item_clean,
	        		managedCursor,
	            	new String[] { "Time", "EarningCur" },
	        		new int[] { R.id.colname, R.id.data });
	
	        mTimeFrameView.setAdapter(mTimeFrameCursorAdapter);
	        mTimeFrameView.setOnItemClickListener(mListClickListener);


        	if ( automatic_updates == true && AdsenseDataServiceState == false)
        		startService(new Intent(this, AdsenseDataService.class));
        	else if ( automatic_updates == false && AdsenseDataServiceState == true)
        		stopService(new Intent(this, AdsenseDataService.class));

        } else {
        	setContentView(R.layout.splash);
        	Button addAccount = (Button) findViewById(R.id.button_add_account);
            addAccount.setOnClickListener(new OnClickListener() {
              public void onClick(View v) {
                startActivity(new Intent(AdsenseClient.this, AccountSetupBasics.class));
              }
            });

        }
    }
    
    @Override
    public void onStart() {
        super.onStart();
        if(D) Log.e(TAG, "++ ON START ++");
    }

    
    @Override
    public synchronized void onPause() {
        super.onPause();
        if(D) Log.e(TAG, "- ON PAUSE -");
    }

    @Override
    public void onStop() {
        super.onStop();
        if(D) Log.e(TAG, "-- ON STOP --");
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        if(D) Log.e(TAG, "+ ON RESUME +");
        if ( theme_type != Integer.parseInt(getSp().getString("theme_type", "1")) ) {
        	reload();
        }
    }

    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if(D) Log.e(TAG, "--- ON DESTROY ---");
    }

    public void reload() {
		onStop();
		onCreate(getIntent().getExtras());
	}
    
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
		int verified = getSp().getInt("verified", 0);
		Cursor managedCursor = null;
        managedCursor = managedQuery(CONTENT_URI, null, null, null, null);
		if (managedCursor != null && verified != 0) {
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.menu, menu);
		}
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        case R.id.preferences:
        	startActivity(new Intent(AdsenseClient.this, Preferences.class));
            return true;
        case R.id.refresh:
        	refreshAdsenseData();
        	return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    private void refreshAdsenseData() {
    	SharedPreferences settings = getSp();
    	Editor e = settings.edit();
		e.putInt("refresh", 1);
		e.commit();
		startActivity(new Intent(AdsenseClient.this, AccountSetupCheckSettings.class));
		return;
	}



	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.ECLAIR
                && keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            // Take care of calling this method on earlier versions of
            // the platform where it doesn't exist.
            onBackPressed();
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
    	Log.d(TAG, "Closing App");
        finish();
        return;
    }
    
    private OnItemClickListener mListClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        	AdsenseClientState appState = ((AdsenseClientState)getApplicationContext());
    		appState.setItemClickPosition(position);	
        	startActivity(new Intent(AdsenseClient.this, CleanThemeListItem.class));
        }
    };


    
    private SharedPreferences getSp() {
		return PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
	}

}