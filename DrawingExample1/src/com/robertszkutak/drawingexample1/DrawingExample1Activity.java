/*
DrawingExample1Activity.java
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

package com.robertszkutak.drawingexample1;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

public class DrawingExample1Activity extends Activity 
{
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);//Get rid of the application titlebar so we have more screen space
        
        setContentView(new DrawingPanel(this));//Use our class we create below as the view
    }
    
    //This class represents the screen of the Android device
    class DrawingPanel extends View
    {
        public DrawingPanel(Context context) 
        {
            super(context);
        }
 
        //This interface allows us to setContentView() to our DrawingPanel class
        @Override
        public void onDraw(Canvas canvas)//canvas allows us to draw directly on the screen of the android device
        {
            Bitmap ship = BitmapFactory.decodeResource(getResources(), R.drawable.ship);//Load an image resource into a Bitmap
            int x = canvas.getWidth()/2-ship.getWidth()/2;//Center X
            int y = canvas.getHeight()/2-ship.getHeight()/2;//Center Y
            
            canvas.drawColor(Color.BLACK);//Fills the entire canvas with black
            canvas.drawBitmap(ship, x, y, null);//Draws a bitmap on specified X and Y coordinates
        }
    }
}