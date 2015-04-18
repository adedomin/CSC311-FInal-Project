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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Collections;
import java.util.Vector;


/**
 * The entry point of the app.
 */
public class MainActivity extends FragmentActivity implements AdapterView.OnItemClickListener
{
	/** list of players in a list view */
	private ArrayAdapter<String> PLAYER_LIST;
	private ListView mListView;

	private TextView mTextView;

	/** map view of nearby points */
	private MapView mMapView;

	/** Various Handlers for input output of network services */
	private HttpHandler mHttpHandler = new HttpHandler();
	private HttpGetHandler mHttpGetHandler = new HttpGetHandler();
	private HttpService mHttpService = new HttpService(mHttpHandler);

	// -999 means uninitialized
	private double PLAYER_LATITUDE = -999;
	private double PLAYER_LONGITUDE = -999;

	private String USER_NAME;
	private String PASSWORD;
	private String TEAM_NAME;

	// location services
	LocationManager mLocationManager;

	// locationListener
	LocationListener mLocationListener;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// ready GPS unit
		mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		mLocationListener = new LocationListener()
		{
			@Override
			public void onLocationChanged(Location location)
			{
				PLAYER_LATITUDE = location.getLatitude();
				PLAYER_LONGITUDE = location.getLongitude();
			}

			@Override
			public void onStatusChanged(String provider, int status, Bundle extras)
			{

			}

			@Override
			public void onProviderEnabled(String provider)
			{

			}

			@Override
			public void onProviderDisabled(String provider)
			{

			}
		};
		mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
		mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocationListener);

		Intent intent = new Intent(this, LoginActivity.class);
		startActivityForResult(intent, 0);
	}

	/**
	 * Pertains to List View's array adapter PLAYER_LIST.
	 * Clicking on a user's name will initiate a battle with said
	 * player if a bluetooth connection can be made
	 *
	 * @param adapterView parent view
	 * @param view the entry in the list view clicked
	 * @param arg2 not used, required
	 * @param arg3 not used, required
	 */
	public void onItemClick(AdapterView<?> adapterView, View view, int arg2, long arg3)
	{
		String view_string = ((TextView) view).getText().toString();

		String MAC_ADDRESS = view_string.substring(view_string.length() - 17);

		Toast.makeText(this, MAC_ADDRESS, Toast.LENGTH_LONG).show();
	}

	/**
	 * When Login activity finishes, this method is called
	 *
	 * @param req_code code the activity was called with.
	 * @param result_code if the result was a success or a failure
	 * @param data includes any values pertaining to the request.
	 */
	public void onActivityResult(int req_code, int result_code, Intent data)
	{
		if (result_code == Activity.RESULT_OK)
		{
			USER_NAME = data.getStringExtra(Constants.INTENT_USER_NAME_KEY);
			Toast.makeText(this, USER_NAME, Toast.LENGTH_LONG).show();
			initializeViews();
		}
	}

	/**
	 * This function is called after Login View finishes.
	 * This function builds the UI views that make up this activity.
	 */
	public void initializeViews()
	{
		// list view init
		PLAYER_LIST = new ArrayAdapter<String>(this, R.layout.activity_text);
		mListView = (ListView) findViewById(R.id.listView);
		mListView.setAdapter(PLAYER_LIST);
		mListView.setOnItemClickListener(this);

		mTextView = (TextView) findViewById(R.id.textView);

		// map view init
		mMapView = (MapView) findViewById(R.id.mapView);
		mMapView.update_map();

		// get data now
		mHttpGetHandler.handleMessage(Message.obtain());
	}

	/**
	 * Haversine formula to get distances of two points.
	 * Points are given in Latitude and Longitude.
	 *
	 * @param lat1 latitude of point one
	 * @param lon1 longitude of point one
	 * @param lat2 latitude of point two
	 * @param lon2 longitude of point two
	 */
	private double getDistance(double lat1, double lat2, double lon1, double lon2)
	{
		double[] lat_rads = new double[2];
		lat_rads[0] = lat1 * Math.PI / 180;
		lat_rads[1] = lat2 * Math.PI /180;

		double delta_lat = (lat2 - lat1) * Math.PI / 180;
		double delta_lon = (lon2 - lon1) * Math.PI / 180;

		double a = Math.pow(Math.sin(delta_lat / 2), 2) +
				   Math.cos(lat_rads[0]) * Math.cos(lat_rads[1]) *
				   Math.pow(Math.sin(delta_lon/2), 2);

		double b = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

		return ((double)Constants.APPROX_RAD_EARTH) * b;
	}

	/**
	 * Function that takes CSV from HttpService and converts it to, sorted, meaningful data
	 */
	private void processCSV(String raw_data)
	{
		// split by line
		String[] CSV = raw_data.split("\n");

		// resizeable array, sortable
		Vector<CSVData> Strings = new Vector<>();

		for (String string : CSV)
		{
			CSVData entry = new CSVData();
			String[] split_string = string.split(",");
			entry.username = split_string[0];
			entry.latitude = Double.parseDouble(split_string[1]);
			entry.longitude = Double.parseDouble(split_string[2]);
			entry.distance = getDistance(
					entry.latitude, PLAYER_LATITUDE,
					entry.longitude, PLAYER_LONGITUDE);
			entry.MAC_ADDR = split_string[3];

			Strings.add(entry);
		}

		// sorts by distance
		Collections.sort(Strings);

		PLAYER_LIST.clear(); // remove the old
		mMapView.clear_map();
		mMapView.setCenterPoint(PLAYER_LATITUDE,PLAYER_LONGITUDE);
		for (CSVData entry : Strings)
		{
			PLAYER_LIST.add(entry.username+"\n"+Double.toString(entry.distance)+"\n"+entry
					.MAC_ADDR);
			mMapView.addGeoPoint(entry.latitude, entry.longitude, 0xFF0000FF);
		}
		mMapView.update_map();
	}

	/**
	 * processes a string which contains a message to player or team from a desktop player.
	 *
	 * @param the_message A string with a message, latitude and longitude
	 */
	private void processMsg(String the_message)
	{
		String[] split_string = the_message.split("\n");
		String[] entry = split_string[0].split(",");

		mTextView.setText(entry[0]);

		mMapView.setMESSAGE_POINT(Double.parseDouble(entry[1]), Double.parseDouble(entry[2]));
		mMapView.update_map();
	}

	/**
	 * A temporary storage unit to allow for sorting of
	 * incoming CSV data.
	 * This class sorts by shortest distance from the player in meters.
	 */
	private class CSVData implements Comparable<CSVData>
	{
		public String username;
		public double distance;
		public String MAC_ADDR;

		// GeoLocation, for MapView
		public double latitude;
		public double longitude;

		/**
		 * Allows for comparison by distance
		 *
		 * @param another entry that is being compared against
		 * @return -1 for less than, 0 for equals, 1 for greater than
		 */
		public int compareTo(CSVData another)
		{
			return Double.compare(this.distance, another.distance);
		}
	}

	/**
	 * Handles HttpService Events
	 * e.g. when HttpService reads in a CSV from the server
	 */
	class HttpHandler extends Handler
	{
		/**
		 * HttpService will send data back to the main activity this way.
		 *
		 * @param msg a message that contains a string and an int describing
		 *            the type of data the string holds.
		 */
		@Override
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
				case Constants.MESSAGE_NEW_CSV:
					processCSV((String)msg.obj);
					break;
				case Constants.MESSAGE_NEW_MESSAGE:
					processMsg((String)msg.obj);
					break;
			}
		}
	}

	/**
	 * basically a timer that calls for a list of users from a server
	 */
	class HttpGetHandler extends Handler
	{
		/**
		 * Messages trigger handler to fetch server information.
		 *
		 * @param msg the message that was sent
		 */
		@Override
		public void handleMessage(Message msg)
		{
			if (PLAYER_LATITUDE == -999 || PLAYER_LATITUDE < -900)
			{
				PLAYER_LIST.add("Waiting for Location.");
				Location location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
				PLAYER_LATITUDE = location.getLatitude();
				PLAYER_LONGITUDE = location.getLongitude();
				sleep(500 * Constants.HTTP_GET_CSV_DELAY);
				return;
			}
			mHttpService.recreateTask();
			mHttpService.getCSV(USER_NAME);
			sleep(1000 * Constants.HTTP_GET_CSV_DELAY);
		}

		/**
		 * allows for timed calling of events in its separate thread
		 *
		 * @param milliseconds time in milliseconds to delay sending a message to self
		 */
		public void sleep(long milliseconds)
		{
			removeMessages(0);
			sendMessageDelayed(obtainMessage(0), milliseconds);
		}
	}
}
