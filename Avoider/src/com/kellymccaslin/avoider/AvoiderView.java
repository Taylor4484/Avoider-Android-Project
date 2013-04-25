package com.kellymccaslin.avoider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.hardware.SensorEvent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class AvoiderView extends SurfaceView implements SurfaceHolder.Callback
{
	private AvoiderThread avoiderThread;
	private Activity activity;
	
	// Motion parameters
	private final float FACTOR_FRICTION = 0.5f; // imaginary friction on the screen
	private final float GRAVITY = 9.8f; // acceleration of gravity
	private float mAx; // acceleration along x axis
	private float mAy; // acceleration along y axis
	//private final float mDeltaT = 0.5f;
	
	// Game constants
	private static final int INITIAL_TARGETS = 6;
	private static final int INITIAL_BLOCKERS = 2;
	private static final int MAX_LIVES = 10;
	
	private int numGreenHit;
	private static int numLives;
	public static double totalElapsedTime;
	private boolean gameOver; 
	//private boolean[] hitStates;
	
	// Sound variables and constants
	private static final int TARGET_SOUND_ID = 0;
	private static final int BLOCKER_SOUND_ID = 1;
	private SoundPool soundPool;
	private Map<Integer,Integer> soundMap;
	
	// Game variables
	private static Point ball = new Point(); 
	private static ArrayList<Point> lives = new ArrayList<Point>();
	private static ArrayList<Point> targets = new ArrayList<Point>();
	private static ArrayList<Point> blockers = new ArrayList<Point>();
	private static Bitmap ballBitmap;
	private static Bitmap lifeBitmap;
	private static Bitmap targetBitmap;
	private static Bitmap blockerBitmap;
	
	private static int numTargets;
	private static int numBlockers;
	private static int targetVelocityX;
	private static int targetVelocityY;
	private static int blockerVelocityX;
	private static int blockerVelocityY;
	
	private int ballInitialX;
	private int ballInitialY;
	
	private int ballSpeed;
	private int targetSpeed;
	private int blockerSpeed;
	
	private static int lifeHeight;
	private static int lifeWidth;
	
	private int screenWidth;
	private int screenHeight;
	
	private static int ballDiameter;
	private static int targetDiameter; // Same as ball? 
	private static int blockerDiameter; // Same as ball? 
	
	private static Paint backgroundPaint = new Paint();
	
	public AvoiderView(Context context, AttributeSet attrs)
	{
		super(context,attrs);
		activity = (Activity) context;
		
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
		ballSpeed = w * 3 / 2;
		targetDiameter = w / 8;
		blockerDiameter = w / 13;
		
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
		
		addTargetsAndBlockers();
		newGame();
	}
	
	public void stopGame() {
		targets.clear();
		blockers.clear();
		if (avoiderThread != null) 
			avoiderThread.setRunning(false);
	}
	
	public void releaseResources() {
		targets.clear();
		blockers.clear();
		soundPool.release();
		soundPool = null;
	}
	
	private void addTargetsAndBlockers() {
		
		int randomXLimit = screenWidth - targetDiameter;
		int randomYLimit = screenHeight - targetDiameter;
		
		for (int i = 0; i< INITIAL_TARGETS; i++) {
			Random generator = new Random();
			int x = generator.nextInt(randomXLimit);
			int y = generator.nextInt(randomYLimit);
			targets.add(new Point(x,y));
		}
		
		for (int i = 0; i< INITIAL_BLOCKERS; i++) {
			Random generator = new Random();
			int x = generator.nextInt(randomXLimit);
			int y = generator.nextInt(randomYLimit);
			blockers.add(new Point(x,y));
		}
	}
	
	
	private void newGame()
	{
		numLives = 3;
		
		ball.set(ballInitialX, ballInitialY);
		
		if (gameOver)
		{
			gameOver = false;
			avoiderThread = new AvoiderThread(getHolder());
			avoiderThread.start();
		}
				
	}
	
	
	//----------- To do while thread is running ---------------//
	public static void updatePositions(double elapsedTimeMS)
	{
		
		double interval = elapsedTimeMS / 1000.0; 
		numTargets = targets.size();
		numBlockers = blockers.size();
		
		// Update target positions
		
//		for (int i=0; i<numTargets; i++) {
//			Point currentTarget = targets.get(i);
//			currentTarget.x += interval * targetVelocityX;
//			currentTarget.y += interval * targetVelocityY;
//			targets.set(i, new Point(currentTarget.x, currentTarget.y));
//		}
		
		// Update blocker positions
		
//		for (int i=0; i<numBlockers; i++) {
//			Point currentTarget = blockers.get(i);
//			currentTarget.x += interval * blockerVelocityX;
//			currentTarget.y += interval * blockerVelocityY;
//		}
	}
	
	@Override
	public void onDraw(Canvas canvas) {
		drawGameElements(canvas);
	}
	
	// Draw background, ball, target and blockers
	public static void drawGameElements(Canvas canvas) 
	{		
		numTargets = targets.size();
		numBlockers = blockers.size();
		
		canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), backgroundPaint);
		
		//canvas.drawCircle(ball.x, ball.y, ballRadius, ballPaint);
		canvas.drawBitmap(ballBitmap, ball.x, ball.y, null);
		
		
		for (int i=0; i<numTargets; i++) {
			Point point = targets.get(i);
			canvas.drawBitmap(targetBitmap, point.x, point.y, null);
		}
		
		for (int i=0; i<numBlockers; i++) {
			Point point = blockers.get(i);
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
//			canvas.drawRect(point.x, point.y, point.x + lifeWidth, point.y + lifeHeight, 
//					lifePaint);
			canvas.drawBitmap(lifeBitmap, point.x, point.y, null);
		}
	}
	
	//-------------------------------------------------------//
	
	// To do if the user tilts the phone
	public void handleMovement(SensorEvent event) 
	{
		float x=event.values[0];
		float y=event.values[1];
		// TODO: Handle x,y,z changes - change ball.x and ball.y appropriately
	}


	@Override
	public void surfaceCreated(SurfaceHolder holder) 
	{
		setWillNotDraw(false);
		avoiderThread = new AvoiderThread(holder);
		avoiderThread.setRunning(true);
		avoiderThread.start();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) 
	{
		boolean retry = true;
		avoiderThread.setRunning(false);
		
		while (retry)
		{
			try
			{
				avoiderThread.join();
				retry = false;
			}
			catch (InterruptedException e) {}
		}
	}
	
	// Methods we won't be using
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {}

}
