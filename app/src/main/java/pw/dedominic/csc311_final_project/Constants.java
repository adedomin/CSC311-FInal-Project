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

/**
 * Constants.
 * </p>
 * Contains message types, algorithmically important values.
 */
public interface Constants
{
	// needed to calculate distance from player
	public final int APPROX_RAD_EARTH = 6371000; // meters

	public final int MESSAGE_NEW_CSV = 0;

	// time in seconds to query for list of users
	public final long HTTP_GET_CSV_DELAY = 15; // seconds

	// server/http related values
	public final String SERVER_DOMAIN_NAME = "dedominic.pw";
	public final String SERVER_GET_ALL_USERS_PHP = "/csc-311/php/get_users.php";

	// MapView Layout constants
	public final int VIEW_BALL_RADIUS = 10;
	public final double PLAYER_MAP_VIEW_OFFSET = .001;

	// login activity types
	public final String INTENT_USER_NAME_KEY = "USERNAME";
	public final String INTENT_PASSWORD_KEY = "PASSWORD";
}
