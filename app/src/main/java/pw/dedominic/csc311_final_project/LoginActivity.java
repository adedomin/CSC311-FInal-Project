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
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Activity that gets user's username and password.
 * Password is meaningless currently, and likely will be till final.
 * Verification will also be added later
 */
public class LoginActivity extends Activity
{

	EditText mEditText;
	EditText mEditText2;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login2);

		mEditText = (EditText) findViewById(R.id.editText2);
		mEditText2 = (EditText) findViewById(R.id.editText);
		/**
		 * When user hits enter it sends what the edit text view contains
		 */
		mEditText2.setOnEditorActionListener(new TextView.OnEditorActionListener()
		{
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
			{
				CreateUserTask create_user = new CreateUserTask();
				create_user.execute();
				return false;
			}
		});
	}

	private void returnToMainActivity()
	{
		Intent intent = new Intent();

		intent.putExtra(Constants.INTENT_USER_NAME_KEY, mEditText.getText().toString());
		intent.putExtra(Constants.INTENT_TEAM_KEY, mEditText2.getText().toString());

		setResult(Activity.RESULT_OK, intent);
		finish();
	}

	private class CreateUserTask extends AsyncTask<Void, Void, Void>
	{
		@Override
		protected Void doInBackground(Void... params)
		{
			URL url;
			HttpURLConnection connection;
			try
			{
				url = new URL(Constants.SERVER_DOMAIN_NAME +
							  Constants.SERVER_CREATE_USER +
							  "?user="+mEditText.getText().toString() +
							  "&team="+mEditText2.getText().toString());
				connection = (HttpURLConnection) url.openConnection();
				connection.getInputStream();
			}
			catch (MalformedURLException e)
			{
			}
			catch (IOException e)
			{
			}

			returnToMainActivity();
			return null;
		}
	}
}
