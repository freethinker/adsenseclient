package in.humbug.adsenseclient;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import android.preference.PreferenceManager;


public class AdsenseDataService extends Service {
	
	private NotificationManager mNM;

	@Override
	public void onCreate() {
		mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        // Display a notification about us starting.  We put an icon in the status bar.
        showNotification();
	}
	
	public class LocalBinder extends Binder {
        AdsenseDataService getService() {
            return AdsenseDataService.this;
        }
    }

	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i("LocalService", "Received start id " + startId + ": " + intent);
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		String email = settings.getString("email", null);
		String password = settings.getString("password", null);
        System.loadLibrary("adsensejni");
        new AsyncTask<String, Void, Void>() {
            @Override
            protected Void doInBackground(String... args) {
            	 genAdsenseReports(args[0], args[1]);
            	 return null;
            }
        }.execute(email, password);     
        return START_STICKY;
	}
	
	public native int genAdsenseReports(String username, String password);
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
    public void onDestroy() {
        // Cancel the persistent notification.
        mNM.cancel(R.string.adsense_data_service_started);

        // Tell the user we stopped.
        Toast.makeText(this, R.string.adsense_data_service_stopped, Toast.LENGTH_SHORT).show();
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

}
