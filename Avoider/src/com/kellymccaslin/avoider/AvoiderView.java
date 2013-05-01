package com.kellymccaslin.avoider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Timer;

import com.kellymccaslin.avoider.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.SensorEvent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class AvoiderView extends SurfaceView implements SurfaceHolder.Callback
{
	//private AvoiderThread avoiderThread;
	private static Activity activity;
	private static Context cont;
	private static Resources resources;
	
	// Motion parameters
	private final float FACTOR_FRICTION = 0.5f; // imaginary friction on the screen
	private final float GRAVITY = 9.8f; // acceleration of gravity
	private float mAx; // acceleration along x axis
	private float mAy; // acceleration along y axis
	//private final float mDeltaT = 0.5f;
	
	// Game constants
	private static final int INITIAL_TARGETS = 6;
	private static final int INITIAL_BLOCKERS = 4;
	private static final int MAX_LIVES = 10;
	
	private static int numGreenHit;
	private static int numLives;
	public static double totalElapsedTime;
	private static boolean gameOver = true;
	private static boolean timerRunning = false;
	//private boolean[] hitStates;
	
	// Sound variables and constants
	private static final int TARGET_SOUND_ID = 0;
	private static final int BLOCKER_SOUND_ID = 1;
	private SoundPool soundPool;
	private Map<Integer,Integer> soundMap;
	
	// Game variables
	public static android.graphics.PointF ball = new android.graphics.PointF();
	private static ArrayList<Point> lives = new ArrayList<Point>();
	private static ArrayList<Point> targets = new ArrayList<Point>();
	private static ArrayList<Point> blockers = new ArrayList<Point>();
	private static Bitmap ballBitmap;
	private static Bitmap lifeBitmap;
	private static Bitmap targetBitmap;
	private static Bitmap blockerBitmap;
	
	private static int numTargets;
	private static int numBlockers;
	private static ArrayList<Double> targetVelocitiesX = new ArrayList<Double>();
	private static ArrayList<Double> targetVelocitiesY = new ArrayList<Double>();
	private static ArrayList<Double> blockerVelocitiesX = new ArrayList<Double>();
	private static ArrayList<Double> blockerVelocitiesY = new ArrayList<Double>();
	
	private static int ballInitialX;
	private static int ballInitialY;
	
	private int ballSpeed;
	private int targetSpeed;
	private int blockerSpeed;
	
	private static int lifeHeight;
	private static int lifeWidth;
	
	private static int screenWidth;
	private static int screenHeight;
	
	private static int ballDiameter;
	private static int ballRadius;
	private static int targetDiameter;
	private static int targetRadius;
	private static int blockerDiameter;
	private static int blockerRadius;
	
	private static Paint backgroundPaint = new Paint();
	
	public AvoiderView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		activity = (Activity) context;
		cont = context;
		resources = getResources();
		
		getHolder().addCallback(this);
								
		// Initialize sounds
		soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
		soundMap = new HashMap<Integer, Integer>();
		//soundMap.put(TARGET_SOUND_ID, soundPool.load(context, R.raw.target_hit, 1));
		//soundMap.put(BLOCKER_SOUND_ID, soundPool.load(context, R.raw.blocker_hit, 1));
		
	}
	
	// Called when view is first added - set original positions
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) 
	{
		
		super.onSizeChanged(w, h, oldw, oldh);
		
		screenWidth = w;
		screenHeight = h;
		
		ballDiameter = w / 12; 
		targetDiameter = w / 8;
		blockerDiameter = w / 13;
		ballRadius = ballDiameter / 2;
		targetRadius = targetDiameter / 2;
		blockerRadius = blockerDiameter / 2;
		
		targetSpeed = w * 3 / 2;
		blockerSpeed = targetSpeed;

		
		ballInitialX = w / 2;
		ballInitialY = h / 2;
		
		ballBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ball);
		ballBitmap = Bitmap.createScaledBitmap(ballBitmap, ballDiameter, ballDiameter, true);
		
		lifeWidth = w / 15;
		lifeHeight = lifeWidth;
		
		for(int i=0; i < MAX_LIVES; i++)
		{
			lives.add(new Point((i * 40) ,0));
		}
		
		lifeBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.life);
		lifeBitmap = Bitmap.createScaledBitmap(lifeBitmap, lifeWidth, lifeHeight, true);
		
		targetBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.target);
		targetBitmap = Bitmap.createScaledBitmap(targetBitmap, targetDiameter,
				targetDiameter, true);
		
		blockerBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.blocker);
		blockerBitmap = Bitmap.createScaledBitmap(blockerBitmap, blockerDiameter,
				blockerDiameter, true);
		
		backgroundPaint.setColor(Color.WHITE);
		

//		targetVelocityX = 1;
//		targetVelocityY = 1; 
//		blockerVelocityX = 1;
//		blockerVelocityY = 1;
		newGame();
	}
	
	public static void stopGame() {
		targets.clear();
		blockers.clear();
		if (timerRunning) {
			Avoider.tmr.cancel();
			Avoider.tmr.purge();
			timerRunning = false;
		}
	}
	
	public void releaseResources() {
		targets.clear();
		blockers.clear();
		soundPool.release();
		soundPool = null;
	}
	
	public static void newGame()
	{
		numLives = 3;
		
		ball.set(ballInitialX, ballInitialY);
		
		if (gameOver)
		{
			gameOver = false;
		}
		addTargetsAndBlockers();
		if (!timerRunning) {
			Avoider.tmr = new Timer();
			Avoider.tmr.schedule(Avoider.tsk,10,10);
			timerRunning = true;
		}
	}
	
	private static void addTargetsAndBlockers() {
		
		int randomXLimit = screenWidth - targetDiameter;
		int randomYLimit = screenHeight - targetDiameter;
		
		for (int i = 0; i< INITIAL_TARGETS; i++) {
			Random generator = new Random();
			int x = generator.nextInt(randomXLimit);
			int y = generator.nextInt(randomYLimit);
			targets.add(new Point(x,y));
		}
		
		// Set initial velocities
		for (int i = 0; i< INITIAL_BLOCKERS; i++) {
			Random generator = new Random();
			int x = generator.nextInt(randomXLimit);
			int y = generator.nextInt(randomYLimit);
			blockers.add(new Point(x,y));
		}
		
		for (int i=0; i<INITIAL_TARGETS; i++) {
			Random generator = new Random();
			double theta1 = generator.nextDouble() * 360.0;
			double theta2 = generator.nextDouble() * 360.0;
			targetVelocitiesX.add(i, Math.sin(theta1));
			targetVelocitiesY.add(i, Math.sin(theta2));
		}

		for (int i=0; i<INITIAL_BLOCKERS; i++) {
			Random generator = new Random();
			double theta1 = generator.nextDouble() * 360.0;
			double theta2 = generator.nextDouble() * 360.0;
			
			blockerVelocitiesX.add(i, Math.sin(theta1));
			blockerVelocitiesY.add(i, Math.sin(theta2));
		}

	}
	
	
	//----------- To do while timer is running ---------------//
	public static void updatePositions()
	{
		// Don't try to update positions if we haven't started the game yet
		if (gameOver == true) {
			return;
		}
		numTargets = targets.size();
		numBlockers = blockers.size();
		
		checkForCollisions();
		
		// Update target positions
		Iterator<Point> iter = targets.iterator();
		Iterator<Double> velX = targetVelocitiesX.iterator();
		Iterator<Double> velY = targetVelocitiesY.iterator();
		int i=0;
		while (iter.hasNext()) {
			// Get center point of target
			Point target = iter.next();
			double xVelocity = velX.next();
			double yVelocity = velY.next();
			int changeX;
			int changeY;
			
			if (target.x + targetDiameter > screenWidth || target.x <= 0) {
				xVelocity *= -1;
				targetVelocitiesX.set(i, xVelocity);
			}
			if (target.y + targetDiameter > screenHeight || target.y <= 0) {
				yVelocity *= -1;
				targetVelocitiesY.set(i, yVelocity);
			}
			
			if (xVelocity < 0) {
				changeX = (int) Math.floor(xVelocity);
			}
			else {
				changeX = (int) Math.ceil(xVelocity);
			}
			if (yVelocity < 0) {
				changeY = (int) Math.floor(yVelocity);
			}
			else {
				changeY = (int) Math.ceil(yVelocity);
			}
	
			target.x += changeX;
			target.y += changeY;
	
			targets.set(i, new Point(target.x, target.y));
			i++;
		}
		
		
		// Update blocker positions
		iter = blockers.iterator();
		velX = blockerVelocitiesX.iterator();
		velY = blockerVelocitiesY.iterator();
		i=0;
		while (iter.hasNext()) {
			Point blocker = iter.next();
			double xVelocity = velX.next();
			double yVelocity = velY.next();
			int changeX;
			int changeY;
			
			if (blocker.x + blockerDiameter > screenWidth || blocker.x <= 0) {
				xVelocity *= -1;
				blockerVelocitiesX.set(i, xVelocity);
			}
			if (blocker.y + blockerDiameter > screenHeight || blocker.y <= 0) {
				yVelocity *= -1;
				blockerVelocitiesY.set(i, yVelocity);
			}
			
			if (xVelocity < 0) {
				changeX = (int) Math.floor(xVelocity);
			}
			else {
				changeX = (int) Math.ceil(xVelocity);
			}
			if (yVelocity < 0) {
				changeY = (int) Math.floor(yVelocity);
			}
			else {
				changeY = (int) Math.ceil(yVelocity);
			}
	
			blocker.x += changeX;
			blocker.y += changeY;

	
			blockers.set(i, new Point(blocker.x, blocker.y));
			i++;
		}
	}
	
	
	private static void checkForCollisions() {
		
		// Get center point of ball
		int ballX = (int) ball.x + ballRadius;
		int ballY = (int) ball.y + ballRadius;
		
		Iterator<Point> iter = targets.iterator();
		Iterator<Double> velX = targetVelocitiesX.iterator();
		Iterator<Double> velY = targetVelocitiesY.iterator();
		int i=0;
		while (iter.hasNext()) {
			// Get center point of target
			Point target = iter.next();
			double xVelocity = velX.next();
			double yVelocity = velY.next();
			int centerX = target.x + targetRadius;
			int centerY = target.y + targetRadius;

			double distance = Math.sqrt(Math.pow(ball.x - centerX,2) + Math.pow(ball.y - centerY,2));
			
			if (distance <= ballRadius + targetRadius) {
				// COLLISION!
				numGreenHit++; 
				// Remove target and its velocities
				iter.remove();
				velX.remove();
				velY.remove();
//				targetVelocitiesX.remove(i);
//				targetVelocitiesY.remove(i);
				if (numGreenHit == 5) {
					numLives++;
					numGreenHit = 0;
				}
				if (numLives == 10) {
					// GAME OVER - You won
					stopGame();
					showGameOverDialog(R.string.win);
				}
			}
			i++;
		}
		
		iter = blockers.iterator();
		velX = blockerVelocitiesX.iterator();
		velY = blockerVelocitiesY.iterator();
		i=0;
		while (iter.hasNext()) {
			// Get center point of target
			Point blocker = iter.next();
			double xVelocity = velX.next();
			double yVelocity = velY.next();
			int centerX = blocker.x + blockerRadius;
			int centerY = blocker.y + blockerRadius;

			double distance = Math.sqrt(Math.pow(ball.x - centerX,2) + Math.pow(ball.y - centerY,2));
			
			if (distance <= ballRadius + blockerRadius) {
				// COLLISION!
				numLives--;
				iter.remove();
				velX.remove();
				velY.remove();
//				blockerVelocitiesX.remove(i);
//				blockerVelocitiesY.remove(i);
				if (numLives == 0) {
					//GAME OVER - You lose
					stopGame();
					showGameOverDialog(R.string.lose);
				}
			}
			i++;
		}
	}
	
	
	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);

//		numTargets = targets.size();
//		numBlockers = blockers.size();
		
		canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), backgroundPaint);
		
		canvas.drawBitmap(ballBitmap, ball.x, ball.y, null);
		
		Iterator<Point> targetIter = targets.iterator();
		while(targetIter.hasNext()) {
			Point point = targetIter.next();
			canvas.drawBitmap(targetBitmap, point.x, point.y, null);
		}
		
		Iterator<Point> blockerIter = blockers.iterator();
		while(blockerIter.hasNext()) {
			Point point = blockerIter.next();
			canvas.drawBitmap(blockerBitmap, point.x, point.y, null);
		}
		
		addLives(canvas);
	}
	
	public static void addLives(Canvas canvas) 
	{
	
		// Add the current number of lives to the screen
		for(int i = 0; i < numLives; i++)
		{
			Point point = lives.get(i);
			canvas.drawBitmap(lifeBitmap, point.x, point.y, null);
		}
	}
	
	//-------------------------------------------------------//

	private static void showGameOverDialog(int messageId)
	   {
	      // create a dialog displaying the given String
	      final AlertDialog.Builder dialogBuilder = 
	         new AlertDialog.Builder(cont);
	      dialogBuilder.setCancelable(false);

	      // display number of shots fired and total time elapsed
	      dialogBuilder.setMessage(resources.getString(messageId));
	      dialogBuilder.setPositiveButton(R.string.reset_game,
	         new DialogInterface.OnClickListener()
	         {
	            // called when "Reset Game" Button is pressed
	            @Override
	            public void onClick(DialogInterface dialog, int which)
	            {
	            	newGame();
	            } // end method onClick
	         } // end anonymous inner class
	      ); // end call to setPositiveButton

	      activity.runOnUiThread(
	         new Runnable() {
	            public void run()
	            {
	               dialogBuilder.show(); // display the dialog
	            } // end method run
	         } // end Runnable
	      ); // end call to runOnUiThread
	   } // end method showGameOverDialog
	
	
	@Override
	public void surfaceCreated(SurfaceHolder holder) 
	{
		setWillNotDraw(false);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) 
	{
		boolean retry = true;
		
		while (retry)
		{
			try
			{
				retry = false;
			}
			finally {}
		}
	}


	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {}

}
