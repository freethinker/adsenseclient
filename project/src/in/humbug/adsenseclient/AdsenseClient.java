package in.humbug.adsenseclient;

import java.io.File;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class AdsenseClient extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        TextView tv = new TextView(this);
        tv.setText( stringFromJNI() );
        setContentView(tv); 
        File adsenseDataDir = new File("/sdcard/humbug_adsense_client/");
        adsenseDataDir.mkdirs();
    }
    
    public native String stringFromJNI();
    static {
    	System.loadLibrary("adsensejni");
    }
}