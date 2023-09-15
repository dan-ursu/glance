package danursu.glance;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class NotificationService extends NotificationListenerService {

	private NotificationReceiver nReceiver;
		
	@Override
	public void onCreate() {
		nReceiver = new NotificationReceiver();
		IntentFilter filter = new IntentFilter();
        filter.addAction("danursu.glance.GET_NOTIFICATIONS");
        registerReceiver(nReceiver,filter);
	}
	
	@Override
	public void onNotificationPosted(StatusBarNotification sbn) {
		sendBroadcast(new Intent("danursu.glance.GET_NOTIFICATIONS"));
	}

	@Override
	public void onNotificationRemoved(StatusBarNotification sbn) {
		sendBroadcast(new Intent("danursu.glance.GET_NOTIFICATIONS"));	
	}
	
	@Override
	public void onDestroy() {
		unregisterReceiver(nReceiver);
	}
	
	class NotificationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
        	
        	int totalNotifications = 0;
        	
        	StatusBarNotification[] sbns = getActiveNotifications();
        	
        	for(int i = 0; i < sbns.length; i++) {
        		if(totalNotifications < 3) { //This will still execute on the third notification!
        			if(sbns[i] != null) {        				
                		StatusBarNotification sbn = sbns[i];
                		if(!sbn.isClearable()) {
                			continue;
                		}
                		int iconId = sbn.getNotification().icon;
                		try {
        					Resources res = getContext().getPackageManager().getResourcesForApplication(sbn.getPackageName());
        					Bitmap icon = BitmapFactory.decodeResource(res, iconId);
        					if(icon != null) {
        						icon = Bitmap.createScaledBitmap(icon, 48, 48, false);        					
        						GlanceActivity.notificationIcons[totalNotifications] = icon;            					
            					totalNotifications++;
        					}        					
        				} catch (NameNotFoundException e) { }
                	} else {
                		GlanceActivity.notificationIcons[totalNotifications] = null;
                	}
        		} else {
        			break;
        		}
        	}
        	
			sendBroadcast(new Intent("danursu.glance.REFRESH_NOTIFICATIONS"));
			
        }
    }
	
	private Context getContext() {
		return this;
	}
}