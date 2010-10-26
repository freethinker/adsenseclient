package in.humbug.adsenseclient;

import android.app.Application;

public class AdsenseClientState extends Application {
	
	private boolean AdsenseDataServiceState = false;
	private boolean AdsenseDataServiceStatePrev = false;
	private int ListItemClickPosition = 1;
	
	@Override
	public void onCreate(){
		super.onCreate();
	}
	
	public boolean getServiceState(){
		return AdsenseDataServiceState;
	}
	
	public boolean getPrevServiceState(){
		return AdsenseDataServiceStatePrev;
	}
	
	public void setServiceState(boolean state){
		AdsenseDataServiceStatePrev = AdsenseDataServiceState;
		AdsenseDataServiceState = state;
	}
	
	public void setItemClickPosition(int position) {
		ListItemClickPosition = position;
	}
	
	public int getItemClickPosition() {
		return ListItemClickPosition;
	}
}
