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
		import android.util.AttributeSet;
		import android.util.Log;
		import android.view.View;

		import java.util.Vector;

/**
 * Simple map view that renders points only.
 * Does not render an image background.
 * <p>
 * The map is suppose to be roughly ~100 meters along it's longest side.
 * </p>
 */
public class MapView extends View
{
	// communicate with game
	private Handler mHandler;

	// player's point
	private PlayerPoint PLAYERS_POINT;

	// message point
	private PlayerPoint MESSAGE_POINT;

	// array of all map points
	private Vector<PlayerPoint> POINTS = new Vector<>();

	// checks if map ready to render points
	private boolean IS_READY = false;

	/**
	 * Sets map boundaries based on Constant Value
	 *
	 * @param x a longitude in decimal degrees
	 * @return difference of x minus an offset (0.001)
	 */
	private double map_left_boundary(double x)
	{
		return x - Constants.PLAYER_MAP_VIEW_OFFSET;
	}

	/**
	 * Sets map boundary based on Constant Value
	 *
	 * @param x a longitude in decimal degrees
	 * @return sum of x minus an offset (0.001)
	 */
	private double map_right_boundary(double x)
	{
		return x + Constants.PLAYER_MAP_VIEW_OFFSET;
	}

	/**
	 * Sets bottom map boundary based on latitude given
	 * <p>
	 * Note, this constant isn't working as expected,
	 * random numbers are thrown to make it sort of work
	 * </p>
	 * @param y latitude in decimal degrees
	 * @return sum of latitude + (aspect ratio * a constant(0.001))
	 */
	private double map_bottom_boundary(double y)
	{

		return y - .6 * Constants.PLAYER_MAP_VIEW_OFFSET;
	}

	// center's location geographically
	// in decimal degrees
	// initially -99, anything below 0 would work
	private double CENTER_LOCATION_LONGITUDE = -999;
	private double CENTER_LOCATION_LATITUDE = -999;

	public MapView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		PLAYERS_POINT = new PlayerPoint(-100,-100,0xFF000000);
		MESSAGE_POINT = new PlayerPoint(-100,-100,0xFFF7F7B9);
	}

	/**
	 * Sets the center point which all points nearby will draw around.
	 *
	 * @param lat latitude in decimal degrees
	 * @param lon longitude in decimal degrees
	 */
	public void setCenterPoint(double lat, double lon)
	{
		CENTER_LOCATION_LATITUDE = lat;
		CENTER_LOCATION_LONGITUDE = lon;
		double[] xy = decimalDegreesToPixels(lat, lon);
		PLAYERS_POINT.setXY((float)xy[0], (float)xy[1]);
	}

	public void setMESSAGE_POINT(double lat, double lon)
	{
		double[] xy = decimalDegreesToPixels(lat, lon);
		MESSAGE_POINT.setXY((float)xy[0], (float)xy[1]);
	}

	/** Draws map when invalidate() is called. */
	@Override
	protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);

		if (!IS_READY)
		{
			return;
		}

		PLAYERS_POINT.draw(canvas);
		MESSAGE_POINT.draw(canvas);

		for (PlayerPoint point : POINTS)
		{
			point.draw(canvas);
		}
	}

	/** Calls invalidate to force the view to redraw itself. */
	public void update_map()
	{
		invalidate();
		IS_READY = true;
	}

	/** Deletes previous points. */
	public void clear_map()
	{
		POINTS.clear();
	}

	/**
	 * Adds point to vector.
	 *
	 * @param x x coord
	 * @param y y coord
	 * @param c color as a hexadecimal ARGB number
	 */
	public void addPoint(float x, float y, int c)
	{
		POINTS.add(new PlayerPoint(x, y, c));
	}

	/**
	 * Add Point using Latitude and Longitude.
	 * Calls decimalDegreesToPixel then addPoint.
	 *
	 * @param lat latitude in decimal degrees
	 * @param lon longitude in decimal degrees
	 * @param c color as a hexadecimal ARGB number
	 */
	public boolean addGeoPoint(double lat, double lon, int c)
	{
		// index 0 = x, index y = 1
		double[] xy = decimalDegreesToPixels(lat, lon);
		addPoint((float)xy[0], (float)xy[1], c);

		return true;
	}

	/**
	 * True mercator projection.
	 * Converts given decimal degree values
	 * and converts them to pixels on device's screen
	 *
	 * @param lat latitude in decimal degrees
	 * @param lon longitude in decimal degrees
	 * @return double array with an (x,y) coordinate pair
	 */
	public double[] decimalDegreesToPixels(double lat, double lon)
	{
		double left_boundary = map_left_boundary(CENTER_LOCATION_LONGITUDE);
		double right_boundary = map_right_boundary(CENTER_LOCATION_LONGITUDE);
		double right_left_boundary_delta = right_boundary - left_boundary;

		double bottom_boundary = map_bottom_boundary(CENTER_LOCATION_LATITUDE);
		double bottom_boundary_rads = bottom_boundary * Math.PI / 180;

		double x = (lon - left_boundary) *
				(getWidth() / right_left_boundary_delta);

		double lat_rads = lat * Math.PI / 180;
		double map_width = ((getWidth() / right_left_boundary_delta) * 360)
				/ (2 * Math.PI);
		double y_offset = (map_width / 2 * Math.log(
				(1 + Math.sin(bottom_boundary_rads)) / (1 - Math.sin(bottom_boundary_rads))
		));
		double y = getWidth() - ((map_width / 2 * Math.log(
				(1 + Math.sin(lat_rads)) / (1 - Math.sin(lat_rads)))) - y_offset);

		double[] return_arr = {x,y};

		return return_arr;
	}

	/**
	 * Class the describes a point on the map.
	 * Requires x and y and a color
	 */
	private class PlayerPoint
	{
		private float x;
		private float y;
		private Paint color;

		public PlayerPoint(float _x, float _y, int player_color)
		{
			x = _x;
			y = _y;
			color = new Paint();
			color.setColor(player_color);
		}

		public void setXY(float _x, float _y)
		{
			x = _x;
			y = _y;
		}

		public void draw(Canvas canvas)
		{
			canvas.drawCircle(x, y, Constants.VIEW_BALL_RADIUS, color);
		}
	}
}

