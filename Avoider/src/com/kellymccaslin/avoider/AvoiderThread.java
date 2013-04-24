package com.kellymccaslin.avoider;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

public class AvoiderThread extends Thread {

	private SurfaceHolder surfaceHolder;
	private boolean threadIsRunning = true;
	
	public AvoiderThread(SurfaceHolder holder)
	{
		surfaceHolder = holder;
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
					AvoiderView.updatePositions(elapsedTimeMS);
					AvoiderView.drawGameElements(canvas);
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
