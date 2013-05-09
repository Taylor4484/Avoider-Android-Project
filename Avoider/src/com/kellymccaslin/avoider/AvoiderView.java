package com.kellymccaslin.avoider;

// TODO: 1. Add functionality so that more targets and blockers pop up as time goes on. 
//       2. Add sound.
//       3. (If time allows) Add vibration when target or blocker is hit. 

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

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
import android.os.Handler;
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
	
	// Game constants
	private static final int INITIAL_TARGETS = 6;
	private static final int INITIAL_BLOCKERS = 4;
	private static final int MAX_LIVES = 10;
	
	private static int numGreenHit;
	private static int numLives;
	private static double initialTime;
	public static double totalElapsedTime;
	public static boolean gameOver = true;
	private static boolean timerRunning = false;
	private static boolean positionsAdded = false;
	
	// Sound variables and constants
	private static final int TARGET_SOUND_ID = 0;
	private static final int BLOCKER_SOUND_ID = 1;
	private static SoundPool soundPool;
	private static Map<Integer,Integer> soundMap;
	
	// Game variables
	public static android.graphics.PointF ball = new android.graphics.PointF();
	private static ArrayList<Point> lives = new ArrayList<Point>();
	private static ConcurrentMap<Integer,Point> targets = 
			new ConcurrentHashMap<Integer,Point>();
	private static ConcurrentMap<Integer,Point> blockers = 
			new ConcurrentHashMap<Integer,Point>();
	private static ConcurrentMap<Integer,ArrayList> targetVelocities = 
			new ConcurrentHashMap<Integer,ArrayList>();
	private static ConcurrentMap<Integer,ArrayList> blockerVelocities = 
			new ConcurrentHashMap<Integer,ArrayList>();
	private static List<Integer> ti = new ArrayList<Integer>();
	private static List<Integer> bi = new ArrayList<Integer>();
//	private static ArrayList<Point> targets = new ArrayList<Point>();
//	private static ArrayList<Point> blockers = new ArrayList<Point>();
//	private static ArrayList<Point> removeBlockers = new ArrayList<Point>();
	private static Bitmap ballBitmap;
	private static Bitmap lifeBitmap;
	private static Bitmap targetBitmap;
	private static Bitmap blockerBitmap;
	private static Bitmap backgroundBitmap;
	
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
	
	private static Handler handA = new Handler();
	private static Handler handB = new Handler();
	private static Runnable runA;
	private static Runnable runB;
	
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
		soundMap.put(TARGET_SOUND_ID, soundPool.load(context, R.raw.target_hit, 1));
		soundMap.put(BLOCKER_SOUND_ID, soundPool.load(context, R.raw.blocker_hit, 1));
		
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
		
		backgroundBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ice);
		backgroundBitmap = Bitmap.createScaledBitmap(backgroundBitmap, screenWidth, screenHeight, true);
		
		newGame();
	}
	
	public static void stopGame() {
		targets.clear();
		blockers.clear();
		Avoider.handler.removeCallbacks(Avoider.runnable);
		handA.removeCallbacks(runA);
		handB.removeCallbacks(runB);
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
		
		addTargetsAndBlockers();
		
		if (gameOver)
		{
			gameOver = false;
		}
		
		Avoider.handler.postDelayed(Avoider.runnable, 10);
		
		runA = new Runnable() {
			@Override
			public void run() {
				addBlocker();
				handA.postDelayed(runA, 10000);
			}
		};
		runB = new Runnable() {
			@Override
			public void run() {
				addTarget();
				handB.postDelayed(runB, 2000);
			}
		};
		
		handA.postDelayed(runA, 10000);
		handB.postDelayed(runB, 2000);

		initialTime = System.currentTimeMillis();
	}
	
	private static void addTargetsAndBlockers() {
		
		int randomXLimit = screenWidth - targetDiameter;
		int randomYLimit = screenHeight - targetDiameter;
		
		numTargets = INITIAL_TARGETS;
		numBlockers = INITIAL_BLOCKERS;
		
		for (int i = 0; i< INITIAL_TARGETS; i++) {
			Random pointGen = new Random();
			int x = pointGen.nextInt(randomXLimit);
			int y = pointGen.nextInt(randomYLimit);
			Point pos = new Point(x,y);
			targets.put(i, pos);
			
			Random velGen = new Random();
			double theta1 = velGen.nextDouble() * 360.0;
			double theta2 = velGen.nextDouble() * 360.0;
			ArrayList<Double> vel = new ArrayList<Double>();
			vel.add(Math.sin(theta1));
			vel.add(Math.sin(theta2));
			targetVelocities.put(i, vel);
			
			ti.add(i);
		}
		
		// Set initial velocities
		for (int i = 0; i< INITIAL_BLOCKERS; i++) {
			Random pointGen = new Random();
			int x = pointGen.nextInt(randomXLimit);
			int y = pointGen.nextInt(randomYLimit);
			Point pos = new Point(x,y);
			blockers.put(i, pos);
			
			Random velGen = new Random();
			double theta1 = velGen.nextDouble() * 360.0;
			double theta2 = velGen.nextDouble() * 360.0;
			ArrayList<Double> vel = new ArrayList<Double>();
			vel.add(Math.sin(theta1));
			vel.add(Math.sin(theta2));
			blockerVelocities.put(i, vel);
			
			bi.add(i);
		}
		positionsAdded = true;
	}
	
	
	//----------- To do while timer is running ---------------//
	public static void updatePositions()
	{
		// Don't try to update positions if we haven't started the game yet
		if (gameOver == true) {
			return;
		}
		
		checkForCollisions();
		addThings();
		
		// Update target positions
		Iterator<Integer> iter = ti.iterator();
		while(iter.hasNext()) {
			// Get center point of target
			int i = iter.next();
			Point target = targets.get(i);
			if (target != null) {
				ArrayList<Double> vels = targetVelocities.get(i);
				double xVelocity = vels.get(0);
				double yVelocity = vels.get(1);
				int changeX;
				int changeY;
				
				if (target.x + targetDiameter > screenWidth || target.x <= 0) {
					xVelocity *= -1;
					vels.set(0, xVelocity);
				}
				if (target.y + targetDiameter > screenHeight || target.y <= 0) {
					yVelocity *= -1;
					vels.set(1, yVelocity);
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
				Log.v("Test", "Updated velocity: " + vels);
				targets.replace(i, target);
				targetVelocities.replace(i, vels);
			}
		}
		
		
		// Update blocker positions
		Iterator<Integer> iterB = bi.iterator();
		while(iterB.hasNext()) {
			int i = iterB.next();
			Point blocker = blockers.get(i);
			if (blocker != null) {
				ArrayList<Double> vels = blockerVelocities.get(i);
				double xVelocity = vels.get(0);
				double yVelocity = vels.get(1);
				int changeX;
				int changeY;
				
				if (blocker.x + blockerDiameter > screenWidth || blocker.x <= 0) {
					xVelocity *= -1;
					vels.set(0, xVelocity);
				}
				if (blocker.y + blockerDiameter > screenHeight || blocker.y <= 0) {
					yVelocity *= -1;
					vels.set(1, yVelocity);
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
				blockers.replace(i,blocker);
				blockerVelocities.replace(i, vels);
			}
		}
	}
	
	
	private static void checkForCollisions() {
		
		// Get center point of ball
		int ballX = (int) ball.x + ballRadius;
		int ballY = (int) ball.y + ballRadius;

		Iterator<Integer> colIter = ti.iterator();
		while(colIter.hasNext()) {
			// Get center point of target
			int i = colIter.next();
			Point target = targets.get(i);
			if (target != null) {
				ArrayList<Double> vels = targetVelocities.get(i);
				double xVelocity = vels.get(0);
				double yVelocity = vels.get(1);
				int centerX = target.x + targetRadius;
				int centerY = target.y + targetRadius;
	
				double distance = Math.sqrt(Math.pow(ball.x - centerX,2) + Math.pow(ball.y - centerY,2));
				
				if (distance <= ballRadius + targetRadius) {
					// COLLISION!
					soundPool.play(soundMap.get(TARGET_SOUND_ID), 1, 1, 1, 0, 1f);
					numGreenHit++; 
					// Remove target and its velocities
					targets.remove(i);
					targetVelocities.remove(i);
					colIter.remove();
					
					
					if (numGreenHit == 2) {
						numLives++;
						numGreenHit = 0;
					}
					if (numLives == 10) {
						// GAME OVER - You won
						stopGame();
						showGameOverDialog(R.string.win);
					}
				}
			}
		}
		
		Iterator<Integer> colIterB = bi.iterator();
		while(colIterB.hasNext()) {
			int i = colIterB.next();
			Point blocker = blockers.get(i);
			if (blocker != null) {
				ArrayList<Double> vels = blockerVelocities.get(i);
				int centerX = blocker.x + blockerRadius;
				int centerY = blocker.y + blockerRadius;
	
				double distance = Math.sqrt(Math.pow(ball.x - centerX,2) + Math.pow(ball.y - centerY,2));
				
				if (distance <= ballRadius + blockerRadius) {
					// COLLISION!
					soundPool.play(soundMap.get(BLOCKER_SOUND_ID), 1, 1, 1, 0, 1f);
					numLives--;
					blockers.remove(i);
					blockerVelocities.remove(i);
					colIterB.remove();
					
					if (numLives == 0) {
						//GAME OVER - You lose
						stopGame();
						showGameOverDialog(R.string.lose);
					}
				}
			}
		}
	}
	
	public static void addThings() {
		totalElapsedTime = System.currentTimeMillis() - initialTime;
		
		totalElapsedTime = totalElapsedTime / 1000; 
		
		if (totalElapsedTime % 2 == 0) {
			Log.v("Test", "Added blocker");
			addBlocker();
		}
		
		if (totalElapsedTime % 5 == 0) {
			addTarget();
		}
	}
	
	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		canvas.drawBitmap(backgroundBitmap, 0, 0, null);
		
		canvas.drawBitmap(ballBitmap, ball.x, ball.y, null);

		Iterator<Integer> targetIter = ti.iterator();
		while(targetIter.hasNext()) {
			int i = targetIter.next();
			if (targets.get(i) != null) {
				Point point = targets.get(i);
				canvas.drawBitmap(targetBitmap, point.x, point.y, null);
			}
		}
		
		Iterator<Integer> blockerIter = bi.iterator();
		while(blockerIter.hasNext()) {
			int i = blockerIter.next();
			if (blockers.get(i) != null) {
				Point point = blockers.get(i);
				canvas.drawBitmap(blockerBitmap, point.x, point.y, null);
			}
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
	
	private static void addTarget() {
		int randomXLimit = screenWidth - targetDiameter;
		int randomYLimit = screenHeight - targetDiameter;
		numTargets++;
		int i = numTargets;
		
		Random pointGen = new Random();
		int x = pointGen.nextInt(randomXLimit);
		int y = pointGen.nextInt(randomYLimit);
		Point pos = new Point(x,y);
		targets.put(i, pos);
		
		Random velGen = new Random();
		double theta1 = velGen.nextDouble() * 360.0;
		double theta2 = velGen.nextDouble() * 360.0;
		ArrayList<Double> vel = new ArrayList<Double>();
		vel.add(Math.sin(theta1));
		vel.add(Math.sin(theta2));
		targetVelocities.put(i, vel);
		
		ti.add(i);
	}
	
	private static void addBlocker() {
		int randomXLimit = screenWidth - targetDiameter;
		int randomYLimit = screenHeight - targetDiameter;
		numBlockers++;
		int i = numBlockers; 
		
		Random pointGen = new Random();
		int x = pointGen.nextInt(randomXLimit);
		int y = pointGen.nextInt(randomYLimit);
		Point pos = new Point(x,y);
		blockers.put(i, pos);
		
		Random velGen = new Random();
		double theta1 = velGen.nextDouble() * 360.0;
		double theta2 = velGen.nextDouble() * 360.0;
		ArrayList<Double> vel = new ArrayList<Double>();
		vel.add(Math.sin(theta1));
		vel.add(Math.sin(theta2));
		blockerVelocities.put(i, vel);
		
		bi.add(i);
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
