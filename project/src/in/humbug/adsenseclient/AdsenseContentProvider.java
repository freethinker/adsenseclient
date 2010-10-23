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


	public static String AUTHORITY = "in.humbug.adsenseclient.adsensecontentprovider";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/adsense");

	// UriMatcher stuff
	private static final int AFC = 0;
	private static final UriMatcher sURIMatcher = buildUriMatcher();


	private static UriMatcher buildUriMatcher() {
		UriMatcher matcher =  new UriMatcher(UriMatcher.NO_MATCH);
		matcher.addURI(AUTHORITY, "adsense/afc", AFC);
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
		Log.d(TAG, "query(" + uri + ")");

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
					StringBuilder cpi = new StringBuilder(100); /*Clicks Per Impression*/
					cpi.append("Clicks/Impressions: ");
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
					
					Log.d(TAG, "Row1 " + row[0] + " Row2 " + row[1] + " Row3 " + row[2]
					        + " Row4 " + row[3] + " Row5 " + row[4] + " Row6 " + row[5]
					        + " Row 7" + cpi);
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