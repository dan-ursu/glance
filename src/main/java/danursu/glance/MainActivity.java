package danursu.glance;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

public class MainActivity extends Activity {

	private boolean isMyServiceRunning(Class<?> serviceClass) {
	    ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	        if (serviceClass.getName().equals(service.service.getClassName())) {
	            return true;
	        }
	    }
	    return false;
	}
	

	private Switch enabledSwitch;
	private Switch customGlanceScreenSwitch;
	private Switch alwaysOnSwitch;
	
	private Button notificationsButton;
	private Button autolockButton;
	
	private TextView description;
	private TextView notificationsLabel;
	//private Intent screenServiceIntent = new Intent(this, ScreenService.class);
	
	public void startScreenService() {
		startService(new Intent(this, ScreenService.class));
	}
	
	public void stopScreenService() {
		stopService(new Intent(this, ScreenService.class));
	}
	
	/*
	AlertDialog.Builder dialogBuilder;

	private void aboutDialog() {

		dialogBuilder = new AlertDialog.Builder(this);

		dialogBuilder.setTitle("About");
		dialogBuilder.setMessage(aboutMessage);
		
		dialogBuilder.setPositiveButton("Close", new DialogInterface.OnClickListener() 
		{

		@Override
		public void onClick(DialogInterface dialog, int which) {
		//finish();
		}

		});
		
		AlertDialog dialogAbout = dialogBuilder.create();
		dialogAbout.show();
	}
	*/
	private Spinner colour;
	private Spinner brightness;
	
	private boolean enabled = true;
	private boolean customGlanceScreen = false;
	private boolean alwaysOn = false;
	
	private String[] colours = {"White", "Red", "Green", "Blue"};
    private String[] brightnessValues = {"Dim", "Normal", "Bright"};
    
    private ComponentName mComponentName;
    private DevicePolicyManager mDevicePolicyManager;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
				
		colour = (Spinner) findViewById(R.id.colours);
		brightness = (Spinner) findViewById(R.id.brightness);
				
		description = (TextView) findViewById(R.id.description);
		
		mComponentName = new ComponentName(this, AdminReceiver.class);
		mDevicePolicyManager = (DevicePolicyManager)getSystemService(Context.DEVICE_POLICY_SERVICE);
		
		notificationsLabel = (TextView) findViewById(R.id.notificationsLabel);
		notificationsButton = (Button) findViewById(R.id.notificationsButton);
				
		if(Build.VERSION.SDK_INT < 18) { //less than 4.3
			notificationsButton.setVisibility(View.INVISIBLE);
			notificationsLabel.append(" (Android 4.3+)");
		}
		
		notificationsButton.setOnClickListener(new OnClickListener() {
		    public void onClick(View v)
		    {
		    	startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
		    } 
		});		

		autolockButton = (Button) findViewById(R.id.autolockButton);
				
		autolockButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v)
		    {
				boolean isAdmin = mDevicePolicyManager.isAdminActive(mComponentName);  
	            if (!isAdmin) {
	            	Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
		            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mComponentName);
		            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, getString(R.string.adminDescription));
		            startActivityForResult(intent, 15 /* ADMIN_INTENT */);
	            } else {
	            	mDevicePolicyManager.removeActiveAdmin(mComponentName);
	            }				
		    }
		});
		
		refreshColours();
		refreshBrightnessValues();
		
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		final SharedPreferences.Editor editor = sharedPref.edit();
			
		if(!sharedPref.contains("enabled")) {
			enabled = true;
			editor.putBoolean("enabled", true);
			editor.commit();
		} else {
			enabled = sharedPref.getBoolean("enabled", true);
		}
		
		if(!isMyServiceRunning(ScreenService.class) && enabled) {
			startService(new Intent(this, ScreenService.class));
		}
				
		enabledSwitch = (Switch) findViewById(R.id.glanceSwitch);	
		customGlanceScreenSwitch = (Switch) findViewById(R.id.customGlanceScreen);
	    alwaysOnSwitch = (Switch) findViewById(R.id.alwaysOn);
		
		
	    enabledSwitch.setChecked(enabled);

	    enabledSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
	   
	     @Override
	     public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {	   
	    	 if(isChecked){
	    		 //Var.enabled = true;
	 			 editor.putBoolean("enabled", true);
	 			 editor.commit();
	 			 
	 			if(!isMyServiceRunning(ScreenService.class)) {
	 				startScreenService();
	 			}
	 			
	 			customGlanceScreenSwitch.setEnabled(true);
	 			alwaysOnSwitch.setEnabled(customGlanceScreenSwitch.isChecked());
	 			colour.setEnabled(customGlanceScreenSwitch.isChecked());
	 			brightness.setEnabled(customGlanceScreenSwitch.isChecked());
	    	 } else {
	    		 //Var.enabled = false;
	 			 editor.putBoolean("enabled", false);
	 			 editor.commit();
	 			 
	 			if(isMyServiceRunning(ScreenService.class)) {
	 				stopScreenService();
	 			}
	 			
	 			customGlanceScreenSwitch.setEnabled(false);
	 			alwaysOnSwitch.setEnabled(false);
	 			colour.setEnabled(false);
	 			brightness.setEnabled(false);
	    	 }
	    	 refreshDescription();
	     }
	    });
	    
	    
	    
	    if(!sharedPref.contains("customglance")) {
			customGlanceScreen = false;
			editor.putBoolean("customglance", false);
			editor.commit();
		} else {
			customGlanceScreen = sharedPref.getBoolean("customglance", false);
		}
	    
	    
	    customGlanceScreenSwitch.setChecked(customGlanceScreen);
	    
	    customGlanceScreenSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
	 	   
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {	   
			 if(isChecked){
				 alwaysOnSwitch.setEnabled(true);
				 colour.setEnabled(true);
				 brightness.setEnabled(true);
				 customGlanceScreen = true;
				 
				 editor.putBoolean("customglance", true);
	 			 editor.commit();
			 } else {
				 alwaysOnSwitch.setEnabled(false);
				 colour.setEnabled(false);
				 brightness.setEnabled(false);
				 customGlanceScreen = false;
				 
				 editor.putBoolean("customglance", false);
	 			 editor.commit();
			 }	 
			 refreshDescription();
		}
	    });
	    
	    
	    if(!sharedPref.contains("alwayson")) {
			alwaysOn = false;
			editor.putBoolean("alwayson", false);
			editor.commit();
		} else {
			alwaysOn = sharedPref.getBoolean("alwayson", false);
		}
	    
	    alwaysOnSwitch.setChecked(alwaysOn);
	    alwaysOnSwitch.setEnabled(customGlanceScreenSwitch.isChecked());
	    colour.setEnabled(customGlanceScreenSwitch.isChecked());
	    brightness.setEnabled(customGlanceScreenSwitch.isChecked());
	    
	    if(!enabled) {
	    	customGlanceScreenSwitch.setEnabled(false);
 			alwaysOnSwitch.setEnabled(false);
 			colour.setEnabled(false);
 			brightness.setEnabled(false);
	    }
	    
	    alwaysOnSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
		 	   
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {	   
			 if(isChecked){
				 alwaysOn = true;
				 
				 editor.putBoolean("alwayson", true);
	 			 editor.commit();
			 } else {
				 alwaysOn = false;
				 
				 editor.putBoolean("alwayson", false);
	 			 editor.commit();
			 }	
			 refreshDescription();
		}
	    });
	    
	    if(!sharedPref.contains("colour")) {
	    	editor.putString("colour", "White");
	    	editor.commit();
	    }
	    
	    if(!sharedPref.contains("brightness")) {
	    	editor.putString("brightness", "Normal");
	    	editor.commit();
	    }
				        
        ArrayAdapter<String> adapterColour= new ArrayAdapter<String>(this, R.layout.spinner_item, colours);
        adapterColour.setDropDownViewResource(R.layout.spinner_item);
        colour.setAdapter(adapterColour);
        
        colour.setOnItemSelectedListener(new OnItemSelectedListener() 
        {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				String multilingualColour = colour.getSelectedItem().toString();
				String englishColour = "White";
				
				if(multilingualColour.equals(colours[1])) {
					englishColour = "Red";
				} else if(multilingualColour.equals(colours[2])) {
					englishColour = "Green";
				} else if(multilingualColour.equals(colours[3])) {
					englishColour = "Blue";
				} else {
					englishColour = "White";
				}
				
				editor.putString("colour", englishColour);
				editor.commit();
				refreshDescription();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// Never seems to be needed...
			}
        });
        
        ArrayAdapter<String> adapterBrightness= new ArrayAdapter<String>(this, R.layout.spinner_item, brightnessValues);
        adapterBrightness.setDropDownViewResource(R.layout.spinner_item);
        brightness.setAdapter(adapterBrightness);
        
        brightness.setOnItemSelectedListener(new OnItemSelectedListener() 
        {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				String multilingualBrightness = brightness.getSelectedItem().toString();
				String englishBrightness = "Normal";
				
				if(multilingualBrightness.equals(brightnessValues[0])) {
					englishBrightness = "Dim";
				} else if(multilingualBrightness.equals(brightnessValues[2])) {
					englishBrightness = "Bright";
				} else {
					englishBrightness = "Normal";
				}
				
				editor.putString("brightness", englishBrightness);
				editor.commit();
				refreshDescription();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// Never seems to be needed...
			}
        });
        
        String savedColour = sharedPref.getString("colour", "White");
        String multilingualColour = colours[0];
        if(savedColour.equals("Red")) {
        	multilingualColour = colours[1];
        } else if(savedColour.equals("Green")) {
        	multilingualColour = colours[2];
        } else if(savedColour.equals("Blue")) {
        	multilingualColour = colours[3];
        } else {
        	multilingualColour = colours[0];
        }        
        ArrayAdapter myAdapterColour = (ArrayAdapter) colour.getAdapter();
        int spinnerPositionColour = myAdapterColour.getPosition(multilingualColour);
        colour.setSelection(spinnerPositionColour);
        
        String savedBrightness = sharedPref.getString("brightness", "Normal");
        String multilingualBrightness = brightnessValues[1];
        if(savedBrightness.equals("Dim")) {
        	multilingualBrightness = brightnessValues[0];
        } else if(savedBrightness.equals("Bright")) {
        	multilingualBrightness = brightnessValues[2];
        } else {
        	multilingualBrightness = brightnessValues[1];
        }
        ArrayAdapter myAdapterBrightness = (ArrayAdapter) brightness.getAdapter();
        int spinnerPosition = myAdapterBrightness.getPosition(multilingualBrightness);
        brightness.setSelection(spinnerPosition);
	    
	    refreshDescription();
	    
	}
	
	private void refreshColours() {
		colours[0] = getString(R.string.white);
		colours[1] = getString(R.string.red);
		colours[2] = getString(R.string.green);
		colours[3] = getString(R.string.blue);
	}
	
	private void refreshBrightnessValues() {
		brightnessValues[0] = getString(R.string.dim);
		brightnessValues[1] = getString(R.string.normal);
		brightnessValues[2] = getString(R.string.bright);
	}
		
	private void refreshDescription() {
		if(!enabledSwitch.isChecked()) {
	    	description.setText(getString(R.string.case_disabled));
	    } else if(!customGlanceScreenSwitch.isChecked()) {
	    	description.setText(getString(R.string.case1));
	    } else if(!alwaysOnSwitch.isChecked()) {
	    	description.setText(getString(R.string.case2));
	    } else {
	    	description.setText(getString(R.string.case3));
	    }
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		refreshColours();
		refreshBrightnessValues();
		refreshDescription();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_about) {
			//aboutDialog();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
