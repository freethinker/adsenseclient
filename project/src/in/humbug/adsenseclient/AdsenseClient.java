package in.humbug.adsenseclient;

import java.io.File;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.TextView;

public class AdsenseClient extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String text = "NULL";
        String username="pratikmsinha@gmail.com";
        String password="z@dghzna!";
        File adsenseDataDir = new File("/sdcard/humbug_adsense_client/");
        adsenseDataDir.mkdirs();
        System.loadLibrary("adsensejni");
        int status = genAdsenseReports(username, password);
        if (status == 0) {
        	text = "Success";
        } else if ( status == 1) {
        	text = "Curl Error";
        } else if ( status == 2) {
        	text = "Login Error";
        } else if (status == 3) {
        	text = "Report Error";
        } else {
        	text = "Unknown Error";
        }
        setContentView(R.layout.main);
        TextView tv = new TextView(this);
        tv.setText( text );
        setContentView(tv); 

    }
    
    public native int genAdsenseReports(String username, String password);
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }
}