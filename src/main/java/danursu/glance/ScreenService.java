package danursu.glance;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

public class ScreenService  extends Service {

	IntentFilter offFilter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
	IntentFilter onFilter = new IntentFilter(Intent.ACTION_SCREEN_ON);
	
	BroadcastReceiver mReceiverOff = new GlanceStarter();
	BroadcastReceiver mReceiverOn = new GlanceStopper();
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onCreate() {
		
	}

	@Override
	public void onStart(Intent intent, int startId) {     
        registerReceiver(mReceiverOff, offFilter);       
        registerReceiver(mReceiverOn, onFilter);        
	}

	@Override
	public void onDestroy() {
		//Toast.makeText(this, "Screen Service Destroyed", Toast.LENGTH_SHORT).show();	
		unregisterReceiver(mReceiverOff);
		unregisterReceiver(mReceiverOn);
		
	}

}
