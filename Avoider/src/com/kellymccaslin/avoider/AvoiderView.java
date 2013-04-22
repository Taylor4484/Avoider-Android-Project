package com.kellymccaslin.avoider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
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
	private static final int INITIAL_GREEN = 6;
	private static final int INITIAL_RED = 2;
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
	private static Point ball; 
	private static ArrayList<Point> lives = new ArrayList<Point>();
	//private Point[] targets; Change to array lists?
	//private Point[] blockers;

	private int tartgetVelocityX;
	private int targetVelocityY;
	private int blockerVelocityX;
	private int blockerVelocityY;
	
	private int ballInitialX;
	private int ballInitialY;
	
	private int ballSpeed;
	private int targetSpeed;
	private int blockerSpeed;
	
	private static int lifeHeight;
	private static int lifeWidth;
	
	private int screenWidth;
	private int screenHeight;
	
	private static int ballRadius;
	private static int targetRadius; // Same as ball? 
	private static int blockerRadius; // Same as ball? 
	
	// Paint variables
	private static Paint ballPaint = new Paint();
	private static Paint lifePaint = new Paint();
	private static Paint targetPaint = new Paint();
	private static Paint blockerPaint = new Paint();
	private static Paint backgroundPaint = new Paint();
	private float strokeWidth;
	
	public AvoiderView(Context context, AttributeSet attrs)
	{
		super(context,attrs);
		activity = (Activity) context;
		
		getHolder().addCallback(this);
		
		addGamePieces();
		
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
		
		ballRadius = w / 25; 
		targetRadius = w / 25;
		blockerRadius = w / 25;
		
		ballInitialX = w / 2;
		ballInitialY = h / 2;
		
		lifeHeight = h / 25;
		lifeWidth = w / 10;
		
		for(int i=0; i < MAX_LIVES; i++)
		{
			lives.add(new Point(0,i * lifeWidth));
		}
		
		strokeWidth = w / 36;
		ballPaint.setStrokeWidth(strokeWidth);
		ballPaint.setColor(Color.BLACK);
		
		lifePaint.setStrokeWidth(strokeWidth);
		lifePaint.setColor(Color.BLUE);
		
		targetPaint.setStrokeWidth(strokeWidth);
		targetPaint.setColor(Color.GREEN);
		
		blockerPaint.setStrokeWidth(strokeWidth);
		blockerPaint.setColor(Color.RED);
		
		backgroundPaint.setColor(Color.BLUE);
		
		newGame();
	}
	
	public void stopGame() {
		if (avoiderThread != null) 
			avoiderThread.setRunning(false);
	}
	
	public void releaseResources() {
		soundPool.release();
		soundPool = null;
	}
	
	
	private void newGame()
	{
		numLives = 3;
		
		ball.set(ballInitialX, ballInitialY);
		
		if (gameOver)
		{
			gameOver = false;
			//avoiderThread = new AvoiderThread(getHolder());
			//avoiderThread.start();
		}
				
	}
	
	
	private void addGamePieces()
	{
		
		ball = new Point();
		
		// set target and blocker points
	}
	
	
	
	//----------- To do while thread is running ---------------//
	public static void updatePositions(double elapsedTimeMS)
	{
		
	}
	
	public static void drawGameElements(Canvas canvas) 
	{
		Log.v("Test","Draw game elements");
		
		canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), backgroundPaint);
		
		//canvas.drawCircle(ball.x, ball.y, ballRadius, ballPaint);
	}
	
	public static void addLives(Canvas canvas) 
	{
	
		Log.v("Test","Adding Lives");
		
		//canvas.drawRect(0, 0, lifeWidth, lifeHeight, lifePaint);
//		for(int i = 0; i < numLives; i++)
//		{
//			Point point = lives.get(i);
//			canvas.drawRect(point.x, point.y, lifeWidth, lifeHeight, lifePaint);
//		}
	}
	
	//-------------------------------------------------------//
	
	// To do if the user tilts the phone
	public void handleMovement(SensorEvent event) 
	{
		float x=event.values[0];
		float y=event.values[1];
		
		// TODO: Handle x,y,z changes
	}


	@Override
	public void surfaceCreated(SurfaceHolder holder) 
	{
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
