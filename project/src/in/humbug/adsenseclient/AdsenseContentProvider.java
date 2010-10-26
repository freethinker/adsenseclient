package in.humbug.adsenseclient;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Log;
import au.com.bytecode.opencsv.CSVReader;

public class AdsenseContentProvider extends ContentProvider {
	String TAG = "AdsenseContentProvider";
	public static final String SECRETS_FILE_NAME_CSV = "/sdcard/humbug_adsense_client/afc.csv";
	private static final File SECRETS_FILE_CSV = new File(SECRETS_FILE_NAME_CSV);
	
	private static final String[] COLUMNS = {
		"_id",
		"Time",
        "Impressions",
        "Clicks",
        "Earnings",
        "CTR",
        "eCPM",
        "PC",
        "EarningCur",		/* Earnings with a dollar sign in the front */
        "ClicksPerImps",
        "CTR+",
        "eCPM+",
        "PerClick"
    };

	private static final String[] COLUMNS_DAY = {
		"_id",
		"Colname",
		"Data"
    };

	public static String AUTHORITY = "in.humbug.adsenseclient.adsensecontentprovider";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/adsense");

	// UriMatcher stuff
	private static final int AFC = 0;
	/* DO NOT CHANGE THESE VALUES - HARDCODED ELSEWHERE */
	private static final int AFC_TODAY = 1;
	private static final int AFC_YESTERDAY = 2;
	private static final int AFC_LAST7DAYS = 3;
	private static final int AFC_THISMONTH = 4;
	private static final int AFC_LASTMONTH = 5;
	private static final int AFC_ALLTIME = 6;
	/* END HARDCODING */
	
	private static final UriMatcher sURIMatcher = buildUriMatcher();


	private static UriMatcher buildUriMatcher() {
		UriMatcher matcher =  new UriMatcher(UriMatcher.NO_MATCH);
		matcher.addURI(AUTHORITY, "adsense/afc", AFC);
		matcher.addURI(AUTHORITY, "adsense/afc/today", AFC_TODAY);
		matcher.addURI(AUTHORITY, "adsense/afc/yesterday", AFC_YESTERDAY);
		matcher.addURI(AUTHORITY, "adsense/afc/last7days", AFC_LAST7DAYS);
		matcher.addURI(AUTHORITY, "adsense/afc/thismonth", AFC_THISMONTH);
		matcher.addURI(AUTHORITY, "adsense/afc/lastmonth", AFC_LASTMONTH);
		matcher.addURI(AUTHORITY, "adsense/afc/alltime", AFC_ALLTIME);
		return matcher;
	}


	public AdsenseContentProvider() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public int delete(Uri arg0, String arg1, String[] arg2) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean onCreate() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		CSVReader reader = null;

		switch (sURIMatcher.match(uri)) {
		case AFC:			
			try {
				reader = new CSVReader(new FileReader(SECRETS_FILE_CSV));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				return null;
			}
			int i = 0;
			MatrixCursor cursor = new MatrixCursor(COLUMNS);
			for (;;) {
				String[] row;
				try {
					row = reader.readNext();
					if (null == row)
						break;
					if (row.length != 7) {
						Log.d(TAG, "Row length less than the usual value, have a stale file " + row.length);
						return null;
					}
					StringBuilder cpi = new StringBuilder(100); /*Clicks Per Impression*/
					cpi.append("Clicks/AdViews: ");
					cpi.append(row[2]);
					cpi.append("/");
					cpi.append(row[1]);
					
					StringBuilder earnings = new StringBuilder(100);
					earnings.append("$");
					earnings.append(row[3]);
					
					StringBuilder ctr = new StringBuilder(100); /* CTR */
					ctr.append("CTR: ");
					ctr.append(row[4]);
					
					StringBuilder ecpm = new StringBuilder(100); /* ECPM */
					ecpm.append("eCPM: $");
					ecpm.append(row[5]);
					
					StringBuilder perclick = new StringBuilder(100); /* ECPM */
					perclick.append("($");
					perclick.append(row[6]);
					perclick.append("/click)");
					
					cursor.addRow(new Object[]{
							i,
							row[0],
							row[1],
							row[2],
							row[3],
							row[4],
							row[5],
							row[6],
							earnings,
							cpi,
							ctr,
							ecpm,
							perclick
					});
				} catch (IOException e) {
					// TODO Auto-generated catch block
					return null;
				}
				i++;
			}   
			return cursor; 
		case AFC_TODAY:
		case AFC_YESTERDAY:
		case AFC_LAST7DAYS:
		case AFC_THISMONTH:
		case AFC_LASTMONTH:
		case AFC_ALLTIME:
			int position = sURIMatcher.match(uri);
			int j = 0;
			try {
				reader = new CSVReader(new FileReader(SECRETS_FILE_CSV));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				return null;
			}
			MatrixCursor daycursor = new MatrixCursor(COLUMNS_DAY);
			for (;;) {
				String[] row;
				try {
					row = reader.readNext();
					if (null == row)
						break;
					if (row.length != 7) {
						Log.d(TAG, "Row length less than the usual value, have a stale file " + row.length);
						return null;
					}
					
					if (j == (position-1)) {

						StringBuilder clicks = new StringBuilder(100); /*Clicks Per Impression*/
						clicks.append("Ad Clicks: ");
						
						StringBuilder clicksval = new StringBuilder(100);
						clicksval.append(row[2]);

						StringBuilder imp = new StringBuilder(100);
						imp.append("Ad Views: ");
						
						StringBuilder impval = new StringBuilder(100);
						impval.append(row[1]);

						StringBuilder ctr = new StringBuilder(100); /* CTR */
						ctr.append("CTR: ");

						StringBuilder ctrval = new StringBuilder(100);
						ctrval.append(row[4]);

						StringBuilder ecpm = new StringBuilder(100); /* ECPM */
						ecpm.append("eCPM: ");

						StringBuilder ecpmval = new StringBuilder(100); /* ECPM */
						ecpmval.append(row[5]);

						StringBuilder perclick = new StringBuilder(100); /* ECPM */
						perclick.append("Per Click: ");

						StringBuilder perclickval = new StringBuilder(100);
						perclickval.append("$");
						perclickval.append(row[6]);


						int k;
						for (k = 0; k <= 4; k++) {
							if (k == 0) daycursor.addRow(new Object[]{ k, clicks, clicksval });
							else if (k == 1) daycursor.addRow(new Object[]{ k, imp, impval });
							else if (k == 2) daycursor.addRow(new Object[]{ k, ctr, ctrval });
							else if (k == 3) daycursor.addRow(new Object[]{ k, ecpm, ecpmval });
							else daycursor.addRow(new Object[]{ k, perclick, perclickval });
						}
						return daycursor;
					}
					j++;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					return null;
				}
			}
			
		default:
			throw new IllegalArgumentException("Unknown Uri: " + uri);
		}
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

}