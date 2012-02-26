/*
DrawingExample2Activity.java
Copyright (C) 2011 : Robert L Szkutak II

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License along
with this program; if not, write to the Free Software Foundation, Inc.,
51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/

package com.robertszkutak.drawingexample2;

import android.app.Activity;
import android.os.Bundle;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
 
public class DrawingExample2Activity extends Activity 
{
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);//Get rid of the application titlebar so we have more screen space
        
        setContentView(new Panel(this));//Use our class we create below as the view
    }
 
    //This class represents the screen of the Android device
    class Panel extends SurfaceView implements SurfaceHolder.Callback 
    {
    	Bitmap image;//The image we are drawing on the screen
    	int x, y = 0;//Hacky coordinate placement, used for rendering the image on an XY plane
    	
        private OurThread thread;//The thread executes our draw function everytime it is run
 
        public Panel(Context context) 
        {
            super(context);
            getHolder().addCallback(this);
            thread = new OurThread(getHolder(), this);
            
            image = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
        }
 
        //This interface allows us to setContentView() to our DrawingPanel class
        @Override
        public void onDraw(Canvas canvas) //canvas allows us to draw directly on the screen of the android device
        {
            canvas.drawColor(Color.BLACK);//Fills the entire canvas with black
            
            x = canvas.getWidth()/2-image.getWidth()/2;
            canvas.drawBitmap(image, x, y, null);//Draws a bitmap on specified X and Y coordinates
            
            //Moves the image down the screen -- there are better places to put this :-)
            y += 20;
            if(y + image.getHeight() > canvas.getHeight())
            	y = 0;
        }
 
        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}
 
        @Override
        public void surfaceCreated(SurfaceHolder holder) 
        {
            thread.setRunning(true);
            thread.start();
        }
 
        @Override
        public void surfaceDestroyed(SurfaceHolder holder) 
        {
            //We have to wait for the thread to finish so it doesn't touch the surface when we return to the application
            boolean retry = true;
            thread.setRunning(false);
            while (retry) 
            {
                try 
                {
                    thread.join();
                    retry = false;
                } catch (InterruptedException e) 
                {
                }
            }
        }
    }
 
    //This class is used to routinely draws our panel to the Android device again and again
    class OurThread extends Thread 
    {
        private SurfaceHolder surfaceHolder;
        private Panel panel;
        private boolean run = false;
 
        public OurThread(SurfaceHolder surfaceHolder, Panel panel) 
        {
            this.surfaceHolder = surfaceHolder;
            this.panel = panel;
        }
 
        public void setRunning(boolean run) 
        {
            this.run = run;
        }
        
        long timer, timeBuffer = 0;//Helps us see how much time has passed since last time
        
        @Override
        public void run() 
        {
            Canvas c;
            timer = System.currentTimeMillis();
            if(timer > timeBuffer + (1000 / 60))//Makes sure we dont run at more than 60FPS
            {
            	timeBuffer = timer;
            	while (run) 
            	{
            		c = null;
            		try {
	                    c = surfaceHolder.lockCanvas(null);//Have our canvas object point at the Android screen, lock the screen
	                    synchronized (surfaceHolder) 
	                    {
	                    	panel.onDraw(c);
	                    }
            		} finally 
            		{
            			if (c != null) 
	                    {
	                        surfaceHolder.unlockCanvasAndPost(c);//Unlock the screen and redraw our updated canvas to it
	                    }
            		}
            	}
            }
        }
    }
}