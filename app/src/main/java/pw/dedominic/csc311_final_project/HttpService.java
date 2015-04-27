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
	/** For communication back to calling activity */
	private Handler mHandler;

	/** login service */
	private getUserNameTask mGetUserNameTask;

	/** get node AsyncTask */
	private getNodeListTask mGetNodeListTask;

	/** AsyncTask that fetches a CSV from a server. */
	private getCSVTask mGetCSVTask;

	/** AsyncTask that fetches pending player messages */
	private getMessageTask mGetMessageTask;

	/** AsyncTask that uploads user's location */
	private uploadLocationTask mUploadLocationTask;

	/** AsyncTask to upload node capture event */
	private CaptureNodeTask mCaptureNodeTask;

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

	public void recreateGetUserNameTask()
	{
		mGetUserNameTask = new getUserNameTask();
	}

	/** fetches username based on MAC */
	public void getUserName(String MAC_ADDR)
	{
		URL url;
		try
		{
			url = new URL(Constants.SERVER_DOMAIN_NAME +
						  Constants.SERVER_GET_USER_NAME +
						  "?MAC="+MAC_ADDR);
		}
		catch (MalformedURLException e)
		{
			return;
		}

		mGetUserNameTask.execute(url);
	}

	/**
	 * Task that handles authentication on server.
	 * Checks if mac address is associated with a username.
	 * Returns the user's username.
	 * <p>
	 * Returns an empty string otherwise
	 * </p>
	 */
	private class getUserNameTask extends AsyncTask<URL, Void, Void>
	{
		protected Void doInBackground(URL... params)
		{
			HttpURLConnection connection;
			String username;
			BufferedReader reader;
			try
			{
				connection = (HttpURLConnection) params[0].openConnection();
				reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				username = reader.readLine();
				mHandler.obtainMessage(Constants.MESSAGE_USER_NAME, username).sendToTarget();
			}
			catch (IOException e)
			{
				return null;
			}

			return null;
		}
	}

	public void recreateNodeListTask()
	{
		mGetNodeListTask = new getNodeListTask();
	}

	public void getNodeList()
	{
		URL url;
		try
		{
			url = new URL(Constants.SERVER_DOMAIN_NAME +
						  Constants.SERVER_GET_NODE_LIST);
		}
		catch (MalformedURLException e)
		{
			return;
		}

		mGetNodeListTask.execute(url);
	}

	private class getNodeListTask extends AsyncTask<URL, Void, Void>
	{
		protected Void doInBackground(URL... params)
		{
			HttpURLConnection connection;
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
				mHandler.obtainMessage(Constants.MESSAGE_NODE_LIST, builder.toString()).sendToTarget();
			}
			catch (IOException e)
			{
				Log.e("Failed to get Node list", e.getMessage());
			}
			return null;
		}
	}

	/**
	 * AsyncTasks can only be called once, so to call it again, it must be recreated
	 */
	public void recreateCSVTask()
	{
		mGetCSVTask = new getCSVTask();
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
			HttpURLConnection connection;
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
				Log.e("FAILED TO GET DATA", e.getMessage());
			}
			return null;
		}
	}

	/** allows for task to be called again; call before executing task */
	public void recreateMessageTask()
	{
		mGetMessageTask = new getMessageTask();
	}

	/** calls task from outside of class scope */
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
			return;
		}

		mGetMessageTask.execute(url);
	}

	/**
	 * Gets messages from commander to player or team global.
	 */
	private class getMessageTask extends AsyncTask<URL, Void, Void>
	{
		protected Void doInBackground(URL... params)
		{
			HttpURLConnection connection;
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
				mHandler.obtainMessage(Constants.MESSAGE_NEW_MESSAGE, builder.toString()).sendToTarget();
			}
			catch (IOException e)
			{
			}

			return null;
		}
	}

	/** allows for task to be called again; call before executing task */
	public void recreateUploadLocationTask()
	{
		mUploadLocationTask = new uploadLocationTask();
	}

	/** calls task from outside of class scope */
	public void uploadLocation(String user_name, double latitude, double longitude, String MAC_ADDR)
	{
		URL url;
		try
		{
			url = new URL(Constants.SERVER_DOMAIN_NAME +
						  Constants.SERVER_UPLOAD_PLAYER_LOCATION +
						  "?user=" + user_name +
					      "&lat=" + Double.toString(latitude) +
						  "&lon=" + Double.toString(longitude)+
						  "&mac=" + MAC_ADDR);
		}
		catch (MalformedURLException e)
		{
			return;
		}
		mUploadLocationTask.execute(url);
	}

	/**
	 * Uploads information about user's location to server.
	 */
	private class uploadLocationTask extends AsyncTask<URL, Void, Void>
	{
		protected Void doInBackground(URL... params)
		{
			HttpURLConnection conn;
			try
			{
				conn = (HttpURLConnection) params[0].openConnection();
				conn.getInputStream();
			}
			catch (IOException e)
			{
			}

			return null;
		}
	}

	public void recreateNodeCaptureTask()
	{
		mCaptureNodeTask = new CaptureNodeTask();
	}

	public void captureNode(int nodeID, String teamname)
	{
		URL url;
		try
		{
			url = new URL(Constants.SERVER_DOMAIN_NAME +
						  Constants.SERVER_CAPTURE_NODE +
						   "?team="+teamname +
						   "&node="+Integer.toString(nodeID));
		}
		catch (MalformedURLException e)
		{
			return;
		}

		mCaptureNodeTask.execute(url);
	}

	public class CaptureNodeTask extends AsyncTask<URL, Void, Void>
	{
		protected Void doInBackground(URL... params)
		{
			HttpURLConnection connection;
			try
			{
				connection = (HttpURLConnection) params[0].openConnection();
				connection.getInputStream();
			}
			catch (IOException e)
			{
			}
			return null;
		}
	}
}
