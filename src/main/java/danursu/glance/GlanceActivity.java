package danursu.glance;

import danursu.glance.GlanceActivity.NotificationReceiver;
import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.DigitalClock;
import android.widget.ImageView;

import java.util.Random;

public class GlanceActivity extends Activity {
	
	public static Bitmap[] notificationIcons = {null, null, null};
	private NotificationReceiver nReceiver;

	private int secondsUntilClose = 5;
	//private boolean finishedLoading = false;
	
	class NotificationReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
        	refreshNotifications();
        }
    }

    class ClockThread extends Thread {
        public void run() {

            Random random = new Random();

            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int height = size.y;

            while(isStillOn) {
                try {
                    Thread.sleep(10000);
                    try {
                        clock.setY(random.nextInt((height - 250) - 50) + 50);
                    } catch (Exception e1) {}
                } catch (InterruptedException e2) { }
            }

        }
    }


    class PauseThread extends Thread {
	    public void run() {
	    	
	        try {
				Thread.sleep(secondsUntilClose * 1000);				
			} catch (InterruptedException e) { }
	        
	        if(isStillOn) {
	        	boolean isAdmin = mDevicePolicyManager.isAdminActive(mComponentName);  
	            if (isAdmin) {  
	                mDevicePolicyManager.lockNow();  
	            }
	        }
	        
	        finish();
	    }
	}
	
	private DigitalClock clock;
	private ImageView notification1;
	private ImageView notification2;
	private ImageView notification3;
	
	private ComponentName mComponentName;
	private DevicePolicyManager mDevicePolicyManager;
	
	private boolean isStillOn;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		
		if(Build.VERSION.SDK_INT >= 19 && !sharedPref.getBoolean("alwayson", false)) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE);
        }
		
		setContentView(R.layout.activity_glance);
		
		isStillOn = true;
		
		mComponentName = new ComponentName(this, AdminReceiver.class);
		mDevicePolicyManager = (DevicePolicyManager)getSystemService(Context.DEVICE_POLICY_SERVICE);
		
		clock = (DigitalClock) findViewById(R.id.clock);
		notification1 = (ImageView) findViewById(R.id.notification1);
		notification2 = (ImageView) findViewById(R.id.notification2);
		notification3 = (ImageView) findViewById(R.id.notification3);
				
		String colourToSet = sharedPref.getString("colour", "White");
		
		if(colourToSet.equals("Red")) {
			clock.setTextColor(Color.RED);
		} else if(colourToSet.equals("Green")) {
			clock.setTextColor(Color.GREEN);
		} else if(colourToSet.equals("Blue")) {
			clock.setTextColor(Color.BLUE);
		}		
		
		Window window = this.getWindow();
        //window.addFlags(LayoutParams.FLAG_DISMISS_KEYGUARD);
        window.addFlags(LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        window.addFlags(LayoutParams.FLAG_TURN_SCREEN_ON);
                
        if(sharedPref.getBoolean("alwayson", false)) {
        	window.addFlags(LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
            window.addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);                        
        }
        
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        
        String brightnessToSet = sharedPref.getString("brightness", "Normal");
		
		if(brightnessToSet.equals("Dim")) {
			lp.screenBrightness=LayoutParams.BRIGHTNESS_OVERRIDE_OFF;
		} else if(brightnessToSet.equals("Normal")) {
			lp.screenBrightness=0.25F;
		} else if(brightnessToSet.equals("Bright")) {
			lp.screenBrightness=LayoutParams.BRIGHTNESS_OVERRIDE_FULL;
		}
		
        getWindow().setAttributes(lp);
               
        for(int i = 0; i < 3; i++) {
        	notificationIcons[i] = null;
        }
        
        nReceiver = new NotificationReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("danursu.glance.REFRESH_NOTIFICATIONS");
        registerReceiver(nReceiver,filter);
		
        sendBroadcast(new Intent("danursu.glance.GET_NOTIFICATIONS"));
        
        if(!sharedPref.getBoolean("alwayson", false)) {
        	(new PauseThread()).start();
        } else {
            (new ClockThread()).start();
        }
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		isStillOn = false;
        unregisterReceiver(nReceiver);
        try {
			Thread.sleep(500);
		} catch (InterruptedException e) { }
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.glance, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void refreshNotifications() {
		if(notificationIcons[0] != null) {
			notification1.setImageBitmap(notificationIcons[0]);
		}
		if(notificationIcons[1] != null) {
			notification2.setImageBitmap(notificationIcons[1]);
		}
		if(notificationIcons[2] != null) {
			notification3.setImageBitmap(notificationIcons[2]);
		}
	}
}
