package com.kellymccaslin.avoider;

import android.content.Context;
import android.graphics.Canvas;
import android.view.SurfaceHolder;

public class AvoiderThread extends Thread {

	private SurfaceHolder surfaceHolder;
	private boolean threadIsRunning = true;
	private Canvas canvas;
	private Context context;
	private AvoiderView avoiderView;
	
	public AvoiderThread(SurfaceHolder holder, Context ctxt, AvoiderView aView)
	{
		surfaceHolder = holder;
		context = ctxt;
		avoiderView = aView;
		setName("AvoiderThread");
	}
	
	public void setRunning(boolean running) 
	{
		threadIsRunning = running;
	}
	
	@Override
	public void run()
	{
		Canvas canvas = null;
		long previousFrameTime = System.currentTimeMillis();
		
		while (threadIsRunning)
		{
			try
			{
				canvas = surfaceHolder.lockCanvas(null);
				
				synchronized(surfaceHolder)
				{
					long currentTime = System.currentTimeMillis();
					double elapsedTimeMS = currentTime - previousFrameTime;
					AvoiderView.totalElapsedTime += elapsedTimeMS / 1000.0;
					//AvoiderView.updatePositions(elapsedTimeMS);
					//AvoiderView.drawGameElements(canvas);
					previousFrameTime = currentTime;
				}
			}
			finally
			{
				if (canvas != null)
					surfaceHolder.unlockCanvasAndPost(canvas);
			}
		}
		
		
	}
	
}
