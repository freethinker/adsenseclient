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
        Cursor managedCursor = null;
        managedCursor = managedQuery(CONTENT_URI, null, null, null, null);
        if (managedCursor != null) { 
//        	startService(new Intent(this, AdsenseDataService.class));
	        setContentView(R.layout.main);
	        mTimeFrameView = (ListView) findViewById(R.id.list);
	
	        ListAdapter mTimeFrameCursorAdapter = new SimpleCursorAdapter(this,
	        		R.layout.item,
	        		managedCursor,
	            	new String[] { "Impressions", "Clicks", "Earnings" },
	        		new int[] { R.id.impressions, R.id.clicks, R.id.earnings });
	
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