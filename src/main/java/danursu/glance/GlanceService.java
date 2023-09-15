package danursu.glance;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

public class GlanceService extends Service implements SensorEventListener
{
	
	private SensorManager mSensorManager;
	private Sensor mProximity;
	TelephonyManager telephonyManager;
	PhoneStateListener callStateListener;
		
	private boolean pendingGlance = false;
	private boolean inCall = false;
	
	private boolean customGlance;
    private boolean alwaysOn;
    
    private float maxDistance;
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	@Override
	public void onCreate() {
		//Toast.makeText(this, "Service Created", Toast.LENGTH_LONG).show();
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
	    mProximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
	    telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
	    
	    maxDistance = mProximity.getMaximumRange();
	    
	    //Toast.makeText(this, Float.toString(maxDistance), Toast.LENGTH_LONG).show();
	    
	    sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
	    
	    customGlance = sharedPref.getBoolean("customglance", false);
	    alwaysOn = sharedPref.getBoolean("alwayson", false);	    
	    
	    callStateListener = new PhoneStateListener() {  
	        public void onCallStateChanged(int state, String incomingNumber)   
	        {  
	              if(state == TelephonyManager.CALL_STATE_RINGING){  
	            	  inCall = true;
	              }  
	              
	              if(state == TelephonyManager.CALL_STATE_OFFHOOK){
	            	  inCall = true;
	              }  
	                                  
	              if(state == TelephonyManager.CALL_STATE_IDLE){  
	                  inCall = false;
	              }  
	        }
	    };
	    
	    if(!customGlance || !alwaysOn) {
	    	mSensorManager.registerListener(this, mProximity, SensorManager.SENSOR_DELAY_NORMAL);
		    telephonyManager.listen(callStateListener, PhoneStateListener.LISTEN_CALL_STATE);
	    }
	    
	    inCall = !(telephonyManager.getCallState() == TelephonyManager.CALL_STATE_IDLE);
	    
	    if(customGlance && alwaysOn && !inCall) {
	    	
	    	try {
				Thread.sleep(500);
			} catch (InterruptedException e) { }
	    	
	    	Intent i = new Intent(getBaseContext(), GlanceActivity.class);
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_TASK);
			startActivity(i);
	    }
	}

	@Override
	public void onStart(Intent intent, int startId) {	
	    
	}

	@Override
	public void onDestroy() {
		//Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();
		if(!customGlance || !alwaysOn) {
			mSensorManager.unregisterListener(this);
		    telephonyManager.listen(callStateListener, PhoneStateListener.LISTEN_NONE);
		}
		
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		
	}
	
	@Override
	public void onSensorChanged(SensorEvent event) {
		float distance = event.values[0];
		
		if(maxDistance > 0.0F) {
			if(distance <= maxDistance * 2F / 3F) {
				pendingGlance = true;
			}
			
			if(pendingGlance && distance > maxDistance * 2F / 3F) {
				pendingGlance = false;
				glance();
			}
		} else { // A precaution just in case maxDistance <= 0.0F for some reason
			if(distance <= 2.0F) {
				pendingGlance = true;
			}
			
			if(pendingGlance && distance > 2.0F) {
				pendingGlance = false;
				glance();
			}
		}
		
		//System.out.println(distance);
		/*
		if(maxDistance > 3.0F || maxDistance == 0) {
			if(distance <= 3.0F) {
				pendingGlance = true;
			}
			
			if(pendingGlance && distance > 3.0F) {
				pendingGlance = false;
				glance();
			}
		}
		
		if(maxDistance <= 3.0F && maxDistance > 1.0F) {
			if(distance <= maxDistance - 0.5F) {
				pendingGlance = true;
			}
			
			if(pendingGlance && distance > maxDistance - 0.5F) {
				pendingGlance = false;
				glance();
			}
		}
		
		if(maxDistance <= 1.0F && maxDistance != 0) {
			if(distance <= maxDistance / 2) {
				pendingGlance = true;
			}
			
			if(pendingGlance && distance > maxDistance / 2) {
				pendingGlance = false;
				glance();
			}
		} */
	}
	
	SharedPreferences sharedPref;
	
	private void glance() {
		
		if(inCall) {
			return;
		}
		
		if(customGlance) {
			Intent i = new Intent(getBaseContext(), GlanceActivity.class);
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_TASK);
			startActivity(i);
		} else {
			Intent i = new Intent(getBaseContext(), InvisibleActivity.class);
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_USER_ACTION);
			startActivity(i);
		}
	}
	
	
}