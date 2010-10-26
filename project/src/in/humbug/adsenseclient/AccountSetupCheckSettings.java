package in.humbug.adsenseclient;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.ProgressBar;
import android.widget.TextView;

public class AccountSetupCheckSettings extends Activity {

	private static final boolean D = true;
	String TAG = "AdsenseValidateAccount";
	private ProgressBar mProgressBar;
	private TextView mTextView;
	private Handler mHandler = new Handler();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "+++ OnCreate +++");
		super.onCreate(savedInstanceState);
		int verified = getSp().getInt("verified", 0);
		int refresh = getSp().getInt("refresh", 0);
		if ( verified == 1 && refresh == 0) {
			startActivity(new Intent(this, AccountSetupBasics.class));
			return;
		}
		setContentView(R.layout.account_setup_check_settings);
		if (refresh == 1) {
			mTextView = (TextView)findViewById(R.id.message);
			mTextView.setText("Reloading Adsense Data");
		}
		mProgressBar = (ProgressBar)findViewById(R.id.progress);
		mProgressBar.setIndeterminate(true);
		if(isInternetOn()) {
			new Thread() {
				int status;
				public void run() {
					try{
						System.loadLibrary("adsensejni");
						String email = getSp().getString("email", null);
						String password = getSp().getString("password", null);
						int refresh = getSp().getInt("refresh", 0);
						if ( refresh == 0) {
							delAccountDir("/sdcard/humbug_adsense_client/");
							File adsenseDataDir = new File("/sdcard/humbug_adsense_client/");
							adsenseDataDir.mkdirs();
						}
						if ( email != null && password != null ) {
							status = genAdsenseReports(email, password);
							if (status == 0) {
								startActivity(new Intent(AccountSetupCheckSettings.this, AdsenseClient.class));
								Editor e = getSp().edit();
								e.putInt("verified", 1);
								e.putInt("refresh", 0);
								e.commit();
							} else if (status == 1) {
							} else if (status == 2) {
								showErrorDialogInvalidPassword();
							} else if (status == 3) {
								showErrorDialogReportsError();
							} else {
								Log.d(TAG, "Internet Issue");
								showErrorDialogInternetError();
							}
						} else {
							Log.d(TAG, "+++ NULL EMAIL PASSWORD +++");
							startActivity(new Intent(AccountSetupCheckSettings.this, AccountSetupBasics.class));
						}
					} catch (Exception e) {  
						Log.d(TAG, "+++ Exception that was not caught +++" + status);
						if (status == 1) {
						} else if (status == 2) {
							showErrorDialogInvalidPassword();
						} else if (status == 3) {
							showErrorDialogReportsError();
						} else {
							Log.d(TAG, "Internet Issue");
							showErrorDialogInternetError();
						}
					}
				}
			}.start();
		} else {
			showErrorDialogInternetError();
		}
	}

	@Override
    public synchronized void onResume() {
        super.onResume();
        if(D) Log.e(TAG, "+ ON RESUME +");
        int verified = getSp().getInt("verified", 0);
		int refresh = getSp().getInt("refresh", 0);
		if ( verified == 1 && refresh == 0) {
			//startActivity(new Intent(this, AccountSetupBasics.class));
			finish();
			return;
		}
    }
	
	public native int genAdsenseReports(String username, String password);
	public native int delAccountDir(String path);

	private void showErrorDialogInvalidPassword() {
		mHandler.post(new Runnable() {
			public void run() {
				mProgressBar.setIndeterminate(false);
				new AlertDialog.Builder(AccountSetupCheckSettings.this)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(getString(R.string.account_setup_failed_dlg_title))
				.setMessage("Invalid Password")
				.setCancelable(true)
				.setPositiveButton(
						getString(R.string.account_setup_failed_dlg_edit_details_action),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								startActivity(new Intent(AccountSetupCheckSettings.this, AccountSetupBasics.class));
							}
						})
						.show();
			}
		});
	}

	private void showErrorDialogReportsError() {
		mHandler.post(new Runnable() {
			public void run() {
				mProgressBar.setIndeterminate(false);
				new AlertDialog.Builder(AccountSetupCheckSettings.this)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(getString(R.string.account_setup_failed_dlg_title))
				.setMessage("Error in fetching reports")
				.setCancelable(true)
				.setPositiveButton(
						getString(R.string.account_setup_failed_retry),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								reload();
							}
						})
						.show();
			}
		});
	}

	private void showErrorDialogInternetError() {
		mHandler.post(new Runnable() {
			public void run() {
				mProgressBar.setIndeterminate(false);
				new AlertDialog.Builder(AccountSetupCheckSettings.this)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(getString(R.string.account_setup_failed_dlg_title))
				.setMessage("Internet seems to be down?")
				.setCancelable(true)
				.setPositiveButton(
						getString(R.string.account_setup_failed_retry),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								reload();
							}
						})
						.show();
			}
		});
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
	
	public void reload() {
		onStop();
		onCreate(getIntent().getExtras());
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
    	Log.d(TAG, "Resetting refresh");
    	SharedPreferences settings = getSp();
    	Editor e = settings.edit();
		e.putInt("refresh", 0);
		e.commit();
		finish();
        return;
    }

    private SharedPreferences getSp() {
		return PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
	}
}
