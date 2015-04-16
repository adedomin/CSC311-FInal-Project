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
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Collections;
import java.util.Vector;


public class MainActivity extends FragmentActivity implements AdapterView.OnItemClickListener
{
	// activity has multiple views
	FragmentManager mFragmentManager;

	// list of players in a list view
	private ArrayAdapter<String> PLAYER_LIST;
	private ListView mListView;

	// map view of nearby points
	private MapView mMapView;

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

	public void onItemClick(AdapterView<?> adapterView, View view, int arg2, long arg3)
	{
		String view_string = ((TextView) view).getText().toString();

		String MAC_ADDRESS = view_string.substring(view_string.length() - 17);

		Toast.makeText(this, MAC_ADDRESS, Toast.LENGTH_LONG).show();
	}

	public void onActivityResult(int req_code, int result_code, Intent data)
	{
		if (result_code == Activity.RESULT_OK)
		{
			USER_NAME = data.getStringExtra(Constants.INTENT_USER_NAME_KEY);
			Toast.makeText(this, USER_NAME, Toast.LENGTH_LONG).show();
			initializeViews();
		}
	}

	public void initializeViews()
	{
		// list view init
		PLAYER_LIST = new ArrayAdapter<String>(this, R.layout.activity_text);
		mListView = (ListView) findViewById(R.id.listView);
		mListView.setAdapter(PLAYER_LIST);
		mListView.setOnItemClickListener(this);

		// map view init
		mMapView = (MapView) findViewById(R.id.mapView);
		mMapView.update_map();

		// get data now
		mHttpGetHandler.handleMessage(Message.obtain());
	}

	/**
	 * Haversine formula to get distances by lat/lon
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings)
		{
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	/**
	 * a temporary storage that is used to sort by distance from player
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
		 * @param another entry that is being compared against
		 * @return -1 for less than, 0 for equals, 1 for greater than
		 */
		@Override
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
		@Override
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
				case Constants.MESSAGE_NEW_CSV:
					processCSV((String)msg.obj);
					break;
			}
		}
	}

	/**
	 * basically a timer that calls for a list of users from a server
	 */
	class HttpGetHandler extends Handler
	{
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
			mHttpService.getCSV();
			sleep(1000 * Constants.HTTP_GET_CSV_DELAY);
		}

		/**
		 * allows for timed calling of events in its separate thread
		 */
		public void sleep(long milliseconds)
		{
			removeMessages(0);
			sendMessageDelayed(obtainMessage(0), milliseconds);
		}
	}
}
