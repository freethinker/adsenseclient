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
        "Impressions",
        "Clicks",
        "Earnings"
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
					Log.d(TAG, "Row1 " + row[0] + " Row2 " + row[1] + " Row3 " + row[2]);
					cursor.addRow(new Object[]{
							i,
							row[0],
							row[1],
							row[2],	                    
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