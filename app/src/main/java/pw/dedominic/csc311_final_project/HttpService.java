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

import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Does all the communication with the server using http
 */
public class HttpService
{
	/** HTTP Client */
	HttpURLConnection connection;

	/** For communication back to calling activity */
	private Handler mHandler;

	/** AsyncTask that fetches a CSV from a server. */
	private getCSVTask mGetCSVTask;

	/** AsyncTask that fetches pending player messages */
	private getMessageTask mGetMessageTask;

	/** AsyncTask that uploads user's location */
	private uploadLocationTask mUploadLocationTask;

	/**
	 * Constructor that requires a handler to process data
	 *
	 * @param handler handler from a calling activity
	 *                allows for communication back to activity
	 */
	public HttpService(Handler handler)
	{
		mHandler = handler;
	}

	/**
	 * AsyncTasks can only be called once, so to call it again, it must be recreated
	 */
	public void recreateCSVTask()
	{
		mGetCSVTask = new getCSVTask();
	}

	public void recreateMessageTask()
	{
		mGetMessageTask = new getMessageTask();
	}

	public void  recreateUploadLocationTask()
	{
		mUploadLocationTask = new uploadLocationTask();
	}

	/** allows for calling of task outside of task; */
	public void getCSV(String username)
	{
		URL url;
		try
		{
			url = new URL(Constants.SERVER_DOMAIN_NAME +
					      Constants.SERVER_GET_ALL_USERS_PHP +
						  "?user="+username);
		}
		catch (MalformedURLException e)
		{
			return;
		}
		mGetCSVTask.execute(url);
	}

	public void getMessages(String user_name, String team_name)
	{
		URL url;
		try
		{
			url = new URL(Constants.SERVER_DOMAIN_NAME +
						  Constants.SERVER_GET_PLAYER_MESSAGE +
						  "?user="+user_name+"&team="+team_name);
		}
		catch (MalformedURLException e)
		{
			Log.e("Test", "test");
			return;
		}

		mGetMessageTask.execute(url);
	}

	public void uploadLocation(String user_name, double latitude, double longitude)
	{
		URL url;
		try
		{
			url = new URL(Constants.SERVER_DOMAIN_NAME +
						  Constants.SERVER_UPLOAD_PLAYER_LOCATION +
						  "?user=\"" + user_name +
					      "\"&lati=\"" + Double.toString(latitude) +
						  "\"&long=\"" + Double.toString(longitude) + "\"");
		}
		catch (MalformedURLException e)
		{
			return;
		}
		mUploadLocationTask.execute(url);
	}

	/**
	 * This task is responsible for talking with the server for user information.
	 * CSV columns delimited by a comma.
	 * Entries are delimited by a linefeed.
	 * <p>
	 * CSV column values: username, latitude, longitude, MAC_ADDRESS
	 * </p>
	 */
	private class getCSVTask extends AsyncTask<URL, Void, Void>
	{
		protected Void doInBackground(URL... params)
		{
			String data_string;
			BufferedReader reader;
			StringBuilder builder;
			try
			{
				connection = (HttpURLConnection) params[0].openConnection();
				reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				builder = new StringBuilder();
				while ((data_string = reader.readLine()) != null)
				{
					data_string += "\n";
					builder.append(data_string);
				}
				mHandler.obtainMessage(Constants.MESSAGE_NEW_CSV, builder.toString()).sendToTarget();
			}
			catch (IOException e)
			{
			}
			return null;
		}
	}

	private class getMessageTask extends AsyncTask<URL, Void, Void>
	{
		protected Void doInBackground(URL... params)
		{
			String data_string;
			BufferedReader reader;
			StringBuilder builder;
			try
			{
				connection = (HttpURLConnection) params[0].openConnection();
				reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				builder = new StringBuilder();
				while ((data_string = reader.readLine()) != null)
				{
					data_string += "\n";
					builder.append(data_string);
				}
				Log.e("string", builder.toString());
				mHandler.obtainMessage(Constants.MESSAGE_NEW_MESSAGE, builder.toString()).sendToTarget();
			}
			catch (IOException e)
			{
			}

			return null;
		}
	}

	private class uploadLocationTask extends AsyncTask<URL, Void, Void>
	{
		protected Void doInBackground(URL... params)
		{
			try
			{
				connection = (HttpURLConnection) params[0].openConnection();
			}
			catch (IOException e)
			{
			}

			return null;
		}
	}
}
