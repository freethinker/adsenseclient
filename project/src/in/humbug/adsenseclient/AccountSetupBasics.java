package in.humbug.adsenseclient;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class AccountSetupBasics extends Activity implements OnClickListener {
	
	private EditText mEmailView;
    private EditText mPasswordView;
    private Button mNextButton;
    String TAG = "AdsenseSetupBasics";

	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.account_setup_basics);
		mEmailView = (EditText)findViewById(R.id.account_email);
        mPasswordView = (EditText)findViewById(R.id.account_password);
        mNextButton = (Button)findViewById(R.id.next);
		String email = getSp().getString("email", null);
		String password = getSp().getString("password", null);
		if (email != null)
			mEmailView.setText(email);
		if (password != null)
			mPasswordView.setText(password);

        mNextButton.setOnClickListener(this);
	}
	
	public void onClick(View v) {
        switch (v.getId()) {
            case R.id.next:
                onNext();
                break;
        }
    }
	
	private void onNext() {
        String email = mEmailView.getText().toString().trim();
        String password = mPasswordView.getText().toString().trim();
        SharedPreferences preferences = getSp();
        Editor e = preferences.edit();
        e.putString("email", email);
        e.putString("password", password);
        e.putInt("verified", 0);
        e.putInt("refresh", 0);
        e.commit();
        startActivity(new Intent(AccountSetupBasics.this, AccountSetupCheckSettings.class));
        return;
	}
	
	private SharedPreferences getSp() {
		return PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
	}
}