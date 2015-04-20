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

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * A bluetooth connection service. Connects players to enemy
 */
public class BluetoothService
{
	// app name and identifier id
	private static final String mAppName = "Final-Project";
	private static final UUID mUUID = UUID.fromString("79f29267-b8d4-486b-8ad3-403f652c3fb7");

	// bluetooth
	private BluetoothAdapter mBtAdapter = BluetoothAdapter.getDefaultAdapter();

	// handlers
	private Handler readHandle;

	// threads
	private ListenThread listener;
	private JoinThread joiner;
	private ConnectedThread connected;
	private boolean isConnect = false;

	/**
	 * Bluetooth service needs some way to communicate with an activity
	 *
	 * @param rHandle a handler in the host activity that accepts messages
	 */
	public BluetoothService(Handler rHandle)
	{
		readHandle = rHandle;
	}

	/**
	 * Messages activity indicating a change in the connection state
	 *
	 * @param connect if false, means disconnected, true if connection is established
	 */
	private void connectionChange(boolean connect)
	{
		if (connect == isConnect)
		{
			return;
		}

		int message = connect ? Constants.CONNECTED : Constants.DISCONNECTED;
		readHandle.obtainMessage(message).sendToTarget();
		isConnect = connect;
	}

	/** tells service to start listening for bluetooth connections */
	public void listen()
	{
		listener = new ListenThread();
		listener.start();
	}

	/**
	 * Connects to a target bluetooth device
	 *
	 * @param host the bluetooth device to connect to
	 */
	public void join(BluetoothDevice host)
	{
		joiner = new JoinThread(host);
		joiner.start();
	}

	/**
	 * Takes a bluetooth socket that connects two devices.
	 * Sets up a thread to manage the connection.
	 *
	 * @param socket a connected bluetooth socket
	 */
	public void connect(BluetoothSocket socket)
	{
		connected = new ConnectedThread(socket);
		connectionChange(true);
		connected.start();
	}

	/** Kills all connections and threads */
	public void killAll()
	{
		if (listener != null)
		{
			listener.cancel();
			listener = null;
		}

		if (joiner != null)
		{
			joiner.cancel();
			joiner = null;
		}

		if (connected != null)
		{
			connected.cancel();
			connected = null;
		}
	}

	/** takes data to be written to the socket's output stream */
	public void write(char type, float val)
	{
		if (connected == null)
		{
			return;
		}

		connected.write(type, val);
	}

	/** returns the MAC ADDRESS of the enemy connected */
	public String getConnectedAddress()
	{
		return connected.getConnectionAddress();
	}

	/**
	 * This thread will make the app listen and wait for a connection
	 * will remain in this thread until connection is established
	 */
	private class ListenThread extends Thread
	{
		private final BluetoothServerSocket srvSocket;

		public ListenThread()
		{
			BluetoothServerSocket tmp = null;
			try
			{
				tmp = mBtAdapter.listenUsingInsecureRfcommWithServiceRecord(mAppName, mUUID);
			}
			catch (IOException e)
			{
				connectionChange(false);
			}
			srvSocket = tmp;
		}

		public void run()
		{
			BluetoothSocket socket = null;
			while (true)
			{
				try
				{
					socket = srvSocket.accept();
				}
				catch (IOException e)
				{
					connectionChange(false);
				}
				if (socket != null)
				{
					connect(socket);
					cancel();
					break;
				}
			}
		}

		public void cancel()
		{
			try
			{
				srvSocket.close();
			}
			catch (IOException e)
			{
			}
		}
	}

	/**
	 * Connects to another device located at provided MAC Address.
	 */
	private class JoinThread extends Thread
	{
		private final BluetoothSocket socket;
		private final BluetoothDevice srvDevice;

		public JoinThread(BluetoothDevice device)
		{
			BluetoothSocket tmp = null;
			srvDevice = device;
			try
			{
				tmp = srvDevice.createInsecureRfcommSocketToServiceRecord(mUUID);
			}
			catch (IOException e)
			{
				connectionChange(false);
			}
			socket = tmp;
		}

		public void run()
		{
			mBtAdapter.cancelDiscovery();
			try
			{
				socket.connect();
			}
			catch (IOException e)
			{
				connectionChange(false);
			}
			connect(socket);
		}

		public void cancel()
		{
			try
			{
				socket.close();
			}
			catch (IOException e)
			{
			}
		}
	}

	/**
	 * This thread manages the connection between two bluetooth devices.
	 * Handles reading and writing from the socket.
	 */
	private class ConnectedThread extends Thread
	{
		private final BluetoothSocket connection;
		private final DataInputStream in;
		private final DataOutputStream out;

		private boolean keep_connecting = true;

		public ConnectedThread(BluetoothSocket socket)
		{
			connection = socket;
			InputStream tmpIn = null;
			OutputStream tmpOut = null;
			try
			{
				tmpIn = socket.getInputStream();
				tmpOut = socket.getOutputStream();
			}
			catch (IOException e)
			{
				connectionChange(false);
			}
			in = new DataInputStream(tmpIn);
			out = new DataOutputStream(tmpOut);
		}

		public void run()
		{
			char type;
			float value;

			while (keep_connecting)
			{
				try
				{
					type = in.readChar();
					value = in.readFloat();
					readHandle.obtainMessage(type, 0, 0, value).sendToTarget();
					// try to send bytes to activity using a message
				}
				catch (IOException e)
				{
					Log.e("exception", e.getMessage());
					connectionChange(false);
					break;
				}
			}
		}

		public void write(char type, float value)
		{
			try
			{
				out.writeChar(type);
				out.writeFloat(value);
			}
			catch (IOException e)
			{
				Log.e("exception2", e.getMessage());
			}
		}

		public String getConnectionAddress()
		{
			return connection.getRemoteDevice().toString();
		}

		public void cancel()
		{
			try
			{
				connection.close();
				in.close();
				out.close();
			}
			catch (IOException e)
			{
			}
		}
	}
}

