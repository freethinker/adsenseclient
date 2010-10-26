package in.humbug.adsenseclient;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;


public class AdsenseDataService extends Service {

	private static final boolean D = true;
	String TAG = "AdsenseDataService";
	private NotificationManager mNM;
	private static final int MILLISECONDS = 1000;

	@Override
	public void onCreate() {
		mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

		// Display a notification about us starting.  We put an icon in the status bar.
		// showNotification();
		AdsenseClientState appState = ((AdsenseClientState)getApplicationContext());
	    appState.setServiceState(true);

	}

	public class LocalBinder extends Binder {
		AdsenseDataService getService() {
			return AdsenseDataService.this;
		}
	}


	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i("LocalService", "Received start id " + startId + ": " + intent);
		String email = getSp().getString("email", null);
		String password = getSp().getString("password", null);
		getReports(email, password);     
		return START_NOT_STICKY;
	}

	private void getReports(String email, String password) {
		
		System.loadLibrary("adsensejni");
		new AsyncTask<String, Void, Void>() {
			@Override
			protected Void doInBackground(String... args) {
				if(isInternetOn())
					genAdsenseReports(args[0], args[1]);
				return null;
			}
		}.execute(email, password);
	}

	public native int genAdsenseReports(String username, String password);

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onDestroy() {
		if(D) Log.e(TAG, "--- ON DESTROY ---");
		AdsenseClientState appState = ((AdsenseClientState)getApplicationContext());
	    appState.setServiceState(false);
		// Cancel the persistent notification.
		//mNM.cancel(R.string.adsense_data_service_started);

		// Tell the user we stopped.
		//Toast.makeText(this, R.string.adsense_data_service_stopped, Toast.LENGTH_SHORT).show();
	}


	/**
	 * Show a notification while this service is running.
	 */
	private void showNotification() {
		// In this sample, we'll use the same text for the ticker and the expanded notification
		CharSequence text = getText(R.string.adsense_data_service_started);

		// Set the icon, scrolling text and timestamp
		Notification notification = new Notification(R.drawable.stat_sample, text,
				System.currentTimeMillis());

		// The PendingIntent to launch our activity if the user selects this notification
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				new Intent(this, AdsenseClient.class), 0);

		// Set the info for the views that show in the notification panel.
		notification.setLatestEventInfo(this, getText(R.string.adsense_data_service_label),
				text, contentIntent);

		// Send the notification.
		// We use a layout id because it is a unique number.  We use it later to cancel.
		mNM.notify(R.string.adsense_data_service_started, notification);
	}

	public final boolean isInternetOn() {

		ConnectivityManager connec =  (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		// ARE WE CONNECTED TO THE NET
		if ( connec.getNetworkInfo(0).getState() == NetworkInfo.State.CONNECTED ||
				connec.getNetworkInfo(0).getState() == NetworkInfo.State.CONNECTING ||
				connec.getNetworkInfo(1).getState() == NetworkInfo.State.CONNECTING ||
				connec.getNetworkInfo(1).getState() == NetworkInfo.State.CONNECTED ) {
			return true;
		} else if ( connec.getNetworkInfo(0).getState() == NetworkInfo.State.DISCONNECTED ||  connec.getNetworkInfo(1).getState() == NetworkInfo.State.DISCONNECTED  ) {
			return false;
		}
		return false;
	}

	private SharedPreferences getSp() {
		return PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
	}
	
	private int getFetchFrequencyS() {
		int frequencyS =  Integer.parseInt(getSp().getString("fetch_frequency", "7200"));
		return (frequencyS * MILLISECONDS);
	}
}