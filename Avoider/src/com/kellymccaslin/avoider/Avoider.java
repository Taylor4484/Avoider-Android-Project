package com.kellymccaslin.avoider;

import java.util.Timer;
import java.util.TimerTask;

import android.os.Bundle;
import android.os.Handler;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.RelativeLayout;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class Avoider extends Activity {

	private AvoiderView avoiderView;
	private SensorManager sensorManager;
	private Sensor accelerometer;
	public static Timer tmr;
	public static TimerTask tsk = null;
	private Handler RedrawHandler = new Handler();
	
	//Move..maybe
	int mScrWidth, mScrHeight;
    android.graphics.PointF mBallPos, mBallSpd;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		getWindow().setFlags(0xFFFFFFFF, LayoutParams.FLAG_FULLSCREEN | LayoutParams.FLAG_KEEP_SCREEN_ON);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity);
		
		avoiderView = (AvoiderView) findViewById(R.id.avoiderView);
		
		// Add sensor listener 
		// set the screen always portait
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
		accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		
		// TODO: Set volume control stream
		
		 //get screen dimensions
        Display display = getWindowManager().getDefaultDisplay();  
        mScrWidth = display.getWidth(); 
        mScrHeight = display.getHeight();
    	mBallPos = new android.graphics.PointF();
    	mBallSpd = new android.graphics.PointF();
        
        //create variables for ball position and speed
        mBallPos.x = mScrWidth/2; 
        mBallPos.y = mScrHeight/2; 
        mBallSpd.x = 0;
        mBallSpd.y = 0; 
        
//        //create initial ball
//        mBallView = new BallView(this,mBallPos.x,mBallPos.y,5);
//                
//        mainView.addView(mBallView); //add ball to main screen
//        mBallView.invalidate(); //call onDraw in BallView
        		
        //listener for accelerometer, use anonymous class for simplicity
        ((SensorManager)getSystemService(Context.SENSOR_SERVICE)).registerListener(
    		new SensorEventListener() {    
    			@Override  
    			public void onSensorChanged(SensorEvent event) {  
    			    //set ball speed based on phone tilt (ignore Z axis)
    				mBallSpd.x = -event.values[0];
    				mBallSpd.y = event.values[1];
    				//timer event will redraw ball
    			}
        		@Override  
        		public void onAccuracyChanged(Sensor sensor, int accuracy) {} //ignore this event
        	},
        	((SensorManager)getSystemService(Context.SENSOR_SERVICE))
        	.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0), SensorManager.SENSOR_DELAY_NORMAL);
	}
	
	   //listener for menu button on phone
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("Exit"); //only one menu item
        return super.onCreateOptionsMenu(menu);
    }
    
    //listener for menu item clicked
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	// Handle item selection    
    	if (item.getTitle() == "Exit") //user clicked Exit
    		finish(); //will call onPause
   		return super.onOptionsItemSelected(item);    
    }
	
	
	@Override
	public void onPause() 
	{
		super.onPause();
//		tmr.cancel();
//		tmr = null;
		AvoiderView.stopGame();
	}
	
    @Override
    public void onResume() //app moved to foreground (also occurs at app startup)
    {

        //create timer to move ball to new position
//        tmr = new Timer(); 
        tsk = new TimerTask() {
			public void run() {
				//if debugging with external device, 
				//  a cat log viewer will be needed on the device
				android.util.Log.d(
				    "TiltBall","Timer Hit - " + mBallPos.x + ":" + mBallPos.y);
			    //move ball based on current speed
				mBallPos.x += mBallSpd.x;
				mBallPos.y += mBallSpd.y;
				//if ball goes off screen, reposition to opposite side of screen
				if (mBallPos.x > mScrWidth) mBallPos.x=0;
				if (mBallPos.y > mScrHeight) mBallPos.y=0;
				if (mBallPos.x < 0) mBallPos.x=mScrWidth;
				if (mBallPos.y < 0) mBallPos.y=mScrHeight;
				//update ball class instance
				AvoiderView.ball.x = mBallPos.x;
				AvoiderView.ball.y = mBallPos.y;
				
				AvoiderView.updatePositions();
				//redraw ball. Must run in background thread to prevent thread lock.
				RedrawHandler.post(new Runnable() {
				    public void run() {	
					   avoiderView.invalidate();
				  }});
			}}; // TimerTask
		
//        tmr.schedule(tsk,10,10); //start timer
        super.onResume();
    } // onResume
	
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		avoiderView.releaseResources();
		System.runFinalizersOnExit(true); //wait for threads to exit before clearing app
		android.os.Process.killProcess(android.os.Process.myPid());
	}
	
    //listener for config change. 
    //This is called when user tilts phone enough to trigger landscape view
    //we want our app to stay in portrait view, so bypass event 
    @Override 
    public void onConfigurationChanged(Configuration newConfig)
	{
       super.onConfigurationChanged(newConfig);
	}

}
