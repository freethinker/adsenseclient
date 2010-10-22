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


	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.account_setup_basics);
		mEmailView = (EditText)findViewById(R.id.account_email);
        mPasswordView = (EditText)findViewById(R.id.account_password);
        mNextButton = (Button)findViewById(R.id.next);

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
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        Editor e = preferences.edit();
        e.putString("email", email);
        e.putString("password", password);
        e.commit();
        startActivity(new Intent(AccountSetupBasics.this, AccountSetupCheckSettings.class));
        return;
	}
}
