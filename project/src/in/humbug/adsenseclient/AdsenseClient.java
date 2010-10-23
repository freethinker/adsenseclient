package in.humbug.adsenseclient;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.admob.android.ads.AdManager;

public class AdsenseClient extends Activity {
	String TAG = "AdsenseClient";
	private ListView mTimeFrameView;
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
        		  AdManager.TEST_EMULATOR,             // Android emulator
        		//"E83D20734F72FB3108F104ABC0FFC738",  // My T-Mobile G1 test phone
        } );
        Cursor managedCursor = null;
        managedCursor = managedQuery(CONTENT_URI, null, null, null, null);
        if (managedCursor != null) { 
//        	startService(new Intent(this, AdsenseDataService.class));
	        setContentView(R.layout.main);
	        mTimeFrameView = (ListView) findViewById(R.id.list);
	
	        ListAdapter mTimeFrameCursorAdapter = new SimpleCursorAdapter(this,
	        		R.layout.item,
	        		managedCursor,
	            	new String[] { "Time", "EarningCur", "ClicksPerImps", "CTR+", "eCPM+", "PerClick" },
	        		new int[] { R.id.timeframe, R.id.earnings, R.id.clicksperimps, R.id.ctr, R.id.ecpm, R.id.perclick });
	
	        mTimeFrameView.setAdapter(mTimeFrameCursorAdapter);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        case R.id.preferences:
            preferences();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }



	private void preferences() {
		
	}
}