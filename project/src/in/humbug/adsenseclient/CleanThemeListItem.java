package in.humbug.adsenseclient;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;


public class CleanThemeListItem extends Activity {
	
	private static final boolean D = true;
	String TAG = "AdsenseClient";
	private ListView mTimeFrameDataView;
	private TextView mTimeFrameView;
	public static String AUTHORITY = "in.humbug.adsenseclient.adsensecontentprovider";
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_clean);
        AdsenseClientState appState = ((AdsenseClientState)getApplicationContext());
		int position = appState.getItemClickPosition();
    	
    	String ExtraUri = "today";
    	String TimeFrame = "Today";
    	switch(position) {
    		case 0: ExtraUri = "today"; TimeFrame="Today"; break;
    		case 1: ExtraUri = "yesterday"; TimeFrame="Yesterday"; break;
    		case 2: ExtraUri = "last7days"; TimeFrame="Last 7 Days"; break;
    		case 3: ExtraUri = "thismonth"; TimeFrame="This Month"; break;
    		case 4: ExtraUri = "lastmonth"; TimeFrame="Last Month"; break;
    		case 5: ExtraUri = "alltime"; TimeFrame="All Time"; break;
    	}
    	mTimeFrameView = (TextView) findViewById(R.id.timeframe);
    	mTimeFrameView.setText(TimeFrame);
    	Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/adsense/afc/" + ExtraUri);
        mTimeFrameDataView = (ListView) findViewById(R.id.list);
        Cursor managedCursor = null;
        managedCursor = managedQuery(CONTENT_URI, null, null, null, null);
        if (managedCursor != null) {
        	ListAdapter mTimeFrameCursorAdapter = new SimpleCursorAdapter(this,
	        		R.layout.item_clean,
	        		managedCursor,
	            	new String[] { "Colname", "Data" },
	        		new int[] { R.id.colname, R.id.data });
	
	        mTimeFrameDataView.setAdapter(mTimeFrameCursorAdapter);
        }
	}
}
