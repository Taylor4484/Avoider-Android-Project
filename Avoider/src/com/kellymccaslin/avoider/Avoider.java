package com.kellymccaslin.avoider;

import android.os.Bundle;
import android.widget.RelativeLayout;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class Avoider extends Activity implements SensorEventListener {

	private AvoiderView avoiderView;
	private SensorManager sensorManager;
	private Sensor accelerometer;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity);
		
		avoiderView = (AvoiderView) findViewById(R.id.avoiderView);
		
		// Add sensor listener 
		// set the screen always portait
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
		accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		
		// TODO: Set volume control stream
	}
	
	
	@Override
	public void onPause() 
	{
		super.onPause();
		avoiderView.stopGame();
	}
	
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		avoiderView.releaseResources();
	}
	
	
	// For accelerometer 
	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// Empty
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		
		if(event.sensor.getType()==Sensor.TYPE_ACCELEROMETER){
			
			avoiderView.handleMovement(event);
		}
	}

}
