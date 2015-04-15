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
	// HTTP Client
	HttpURLConnection connection;

	// For communication with activity
	private Handler mHandler;

	/**
	 * AsyncTask that fetches a CSV from a server.
	 */
	private getCSVTask mGetCSVTask;

	/**
	 * Constructor that requires a handler to process data
	 * </p>
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
	public void recreateTask()
	{
		mGetCSVTask = new getCSVTask();
	}

	// allows for calling of task outside of task;
	public void getCSV()
	{
		mGetCSVTask.execute();
	}

	/**
	 * This task is responsible for talking with the server for user information.
	 * CSV columns delimited by a comma.
	 * Entries are delimited by a linefeed.
	 * </p>
	 * CSV column values: username, latitude, longitude, MAC_ADDRESS
	 */
	private class getCSVTask extends AsyncTask<Void, Void, Void>
	{
		protected Void doInBackground(Void... ignore)
		{
			URL url;
			try
			{
				url = new URL("https://dedominic.pw/csc-311/php/get_users.php");
			}
			catch (MalformedURLException e)
			{
				return null;
			}

			String data_string = "";
			BufferedReader reader;
			StringBuilder builder;
			try
			{
				connection = (HttpURLConnection) url.openConnection();
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


}
