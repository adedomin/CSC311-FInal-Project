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
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.TextView;

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
				Intent intent = new Intent();

				intent.putExtra(Constants.INTENT_USER_NAME_KEY, mEditText.getText().toString());
				intent.putExtra(Constants.INTENT_PASSWORD_KEY, mEditText2.getText().toString());

				setResult(Activity.RESULT_OK, intent);
				finish();
				return false;
			}
		});
	}
}
