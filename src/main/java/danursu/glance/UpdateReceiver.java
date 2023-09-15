package danursu.glance;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class UpdateReceiver extends BroadcastReceiver {
	
	SharedPreferences sharedPref;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		if(!sharedPref.getBoolean("enabled", true)) {
			return;
		}
		Intent screenServiceIntent = new Intent(context, ScreenService.class);
        context.startService(screenServiceIntent);
	}

}
