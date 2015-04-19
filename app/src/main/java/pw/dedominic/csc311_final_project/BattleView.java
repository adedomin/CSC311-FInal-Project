/*
 * Copyright (c) 2015. Anthony DeDominic
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pw.dedominic.csc311_final_project;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.jar.Attributes;

/**
 * Created by prussian on 4/18/15.
 */
public class BattleView extends View
{
	private boolean IS_READY = false;
	private boolean IS_READY2 = false;

	private Missile mMissile;

	private DrawTimer mDrawTimer = new DrawTimer();

	double first_x, first_y, last_x, last_y;

	public BattleView(Context context, AttributeSet attributeSet)
	{
		super(context, attributeSet);
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);

		if (!IS_READY)
		{
			return;
		}

		if (IS_READY2)
		{
			mMissile.draw(canvas);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent e)
	{
		switch (e.getAction())
		{
			case MotionEvent.ACTION_DOWN:
				first_x = e.getRawX();
				first_y = e.getRawY();
				break;
			case MotionEvent.ACTION_MOVE:
				break;
			case MotionEvent.ACTION_UP:
				last_x = e.getRawX();
				last_y = e.getRawY();

				double distance = getPointDistance(first_x, first_y, last_x, last_y);
				double radian = Math.atan2(first_y - last_y, first_x - last_x);
				setMissile(distance, radian);
				break;
		}
		return true;
	}

	public double getPointDistance(double x1, double y1, double x2, double y2)
	{
		return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
	}

	public void update()
	{
		if (!IS_READY)
		{
			IS_READY = true;
		}

		mDrawTimer.sleep(1000 / 60);
	}

	public void setMissile(double distance, double rads)
	{
		float x_vel = (float)Math.cos(rads) * (float)(distance * .1);
		float y_vel = (float)Math.sin(rads) * (float)(distance * .1);

		mMissile = new Missile(10, getHeight(), x_vel, y_vel, 0xFF000000);
		IS_READY2 = true;
	}

	private class DrawTimer extends Handler
	{
		@Override
		public void handleMessage(Message msg)
		{
			invalidate();
			update();
		}

		public void sleep(long milliseconds)
		{
			removeMessages(0);
			sendMessageDelayed(obtainMessage(0), milliseconds);
		}
	}

	private class Missile
	{
		private float x;
		private float y;

		private float x_vel;
		private float y_vel;

		Paint paint;

		public Missile(float _x, float _y, float velX, float velY, int color)
		{
			x = _x;
			y = _y;
			x_vel = velX;
			y_vel = velY;

			paint = new Paint();
			paint.setColor(color);
		}

		public void draw(Canvas canvas)
		{
			canvas.drawCircle(x, y, 50, paint);

			x += x_vel;
			y += y_vel;
			y_vel += .5; // gravity
		}
	}
}
