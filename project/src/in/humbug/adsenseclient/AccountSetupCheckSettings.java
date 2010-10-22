package in.humbug.adsenseclient;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ProgressBar;

public class AccountSetupCheckSettings extends Activity {
	
	String TAG = "AdsenseValidateAccount";
	private ProgressBar mProgressBar;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.account_setup_check_settings);
		mProgressBar = (ProgressBar)findViewById(R.id.progress);
		mProgressBar.setIndeterminate(true);
		new Thread() {
			public void run() {
				try{
					System.loadLibrary("adsensejni");
					delAccountDir("/sdcard/humbug_adsense_client/");
					File adsenseDataDir = new File("/sdcard/humbug_adsense_client/");
					adsenseDataDir.mkdirs();
					SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(AccountSetupCheckSettings.this);
					String email = settings.getString("email", null);
					String password = settings.getString("password", null);
					if ( email != null && password != null ) {
						int status = genAdsenseReports(email, password);
						if (status == 0) {
							startActivity(new Intent(AccountSetupCheckSettings.this, AdsenseClient.class));
						} else {
							Log.d(TAG, "+++ INVALID PASSWORD +++");
							startActivity(new Intent(AccountSetupCheckSettings.this, AccountSetupBasics.class));
						}
					} else {
						Log.d(TAG, "+++ NULL EMAIL PASSWORD +++");
						startActivity(new Intent(AccountSetupCheckSettings.this, AccountSetupBasics.class));
					}
				} catch (Exception e) {  
					startActivity(new Intent(AccountSetupCheckSettings.this, AccountSetupBasics.class));
				}
			}
		}.start();

	}
	
	public native int genAdsenseReports(String username, String password);
	public native int delAccountDir(String path);
}
