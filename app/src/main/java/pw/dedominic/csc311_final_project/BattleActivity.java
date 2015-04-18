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

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

public class BattleActivity extends ActionBarActivity implements View.OnTouchListener
{
	private double first_x = 0;
	private double first_y = 0;

	private double last_x = 0;
	private double last_y = 0;

	private String MAC_ADDR;

	private BattleView mBattleView;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_battle);

		Intent intent = getIntent();

		MAC_ADDR = intent.getStringExtra("MAC_ADDR");
	}

	public boolean onTouch(View v, MotionEvent e)
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

				if (getPointDistance(first_x, first_y, last_x, last_y) > .5)
				{
					// do something
				}
		}
		return true;
	}

	public double getPointDistance(double x1, double y1, double x2, double y2)
	{
		return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
	}

	public double getSlopeOfLine(double x1, double y1, double x2, double y2)
	{
		return (y2 - y1)/(x2 - x1);
	}
}
