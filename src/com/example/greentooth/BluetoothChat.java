/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.greentooth;

//import com.example.android.BluetoothChat;
//import android.R;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.greentooth.Const.Const;
import com.example.greentooth.bluetooth.BluetoothChatService;
import com.example.greentooth.bluetooth.DeviceListActivity;
import com.example.greentooth.bluetooth.Fragment_roomchat;

/**
 * This is the main Activity that displays the current chat session.
 */
public class BluetoothChat extends Activity implements
		Fragment_roomchat.Communicator {

	// Bluetooth members
	private String connectedDeviceName = null;

	private BluetoothAdapter bluetoothAdapter = null;
	/**
	 * Bluetooth Chat Service manages thread Which respons to connect to other
	 * devices, send and recieves messages
	 */
	private BluetoothChatService chatService = null;

	// Fragment
	Fragment_roomchat fragment_roomchat = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bluetooth_chat);
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		// If the adapter is null, then Bluetooth is not supported
		if (bluetoothAdapter == null) {
			Toast.makeText(this, "Bluetooth is not available",
					Toast.LENGTH_LONG).show();
			finish();
			return;
		}
	}

	@Override
	public void onStart() {
		super.onStart();

		if (!bluetoothAdapter.isEnabled()) {
			Intent enableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, Const.REQUEST_ENABLE_BT);
			// Otherwise, setup the chat session
		} else {
			if (chatService == null)
				chatService = new BluetoothChatService(this, mHandler);
			// Create new ChatService
		}
	}

	protected void startDeviceListActivity() {
		// Launch the DeviceListActivity to see devices and do scan
		Intent serverIntent = new Intent(this, DeviceListActivity.class);
		startActivityForResult(serverIntent, Const.REQUEST_CONNECT_DEVICE);
	}

	@Override
	public synchronized void onResume() {
		super.onResume();
		// Show Devices List Activity
		Button button_search = (Button) findViewById(R.id.button_search);
		button_search.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				startDeviceListActivity();
			}
		});

		// Show roomchat Fragment
		Button button_roomchat = (Button) findViewById(R.id.button_roomchat);
		button_roomchat.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				fragment_roomchat = new Fragment_roomchat();
				startFragmentRoomChat();
				setRoomChatCommunicate();

			}
		});

		if (chatService != null) {
			// Only if the state is STATE_NONE, do we know that we haven't
			// started already
			if (chatService.getState() == Const.STATE_NONE) {
				// Start the Bluetooth chat services
				chatService.start();
			}
		}
	}

	protected void setRoomChatCommunicate() {
		// BluetoothChat and Fragment Room Chat communicate via
		// Fragment_roomchat.Communicator
		fragment_roomchat.setCommunicator(this);
	}

	protected void startFragmentRoomChat() {
		FragmentTransaction transaction = getFragmentManager()
				.beginTransaction();

		transaction.replace(R.id.fragment_roomchat, fragment_roomchat);
		transaction.commit();

	}

	@Override
	public synchronized void onPause() {
		super.onPause();
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// Stop the Bluetooth chat services
		if (chatService != null)
			chatService.stop();
	}

	// This method is used by Fragment room chat
	void addArrayAdapterFromFragmentRoomChat(String data) {
		if (null != fragment_roomchat) {
			fragment_roomchat.addArrayAdapterToFragmentRoomChat(data);
			Toast.makeText(getApplicationContext(), data, Toast.LENGTH_SHORT)
					.show();
		}
	}

	//
	private final void setStatus(CharSequence subTitle) {
		final ActionBar actionBar = getActionBar();
		actionBar.setSubtitle(subTitle);
	}

	//

	// The Handler that gets information back from the BluetoothChatService
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case Const.MESSAGE_STATE_CHANGE:

				switch (msg.arg1) {
				case Const.STATE_CONNECTED:
					setStatus("Connected to device" + connectedDeviceName);

					break;
				case Const.STATE_CONNECTING:
					setStatus("Connecting");
					break;
				case Const.STATE_LISTEN:
				case Const.STATE_NONE:
					setStatus("not connect");

					break;
				}
				break;
			case Const.MESSAGE_WRITE:
				byte[] writeBuf = (byte[]) msg.obj;
				// construct a string from the buffer
				String writeMessage = new String(writeBuf);
				addArrayAdapterFromFragmentRoomChat("Me:  " + writeMessage);
				break;
			case Const.MESSAGE_READ:

				byte[] readBuf = (byte[]) msg.obj;
				// construct a string from the valid bytes in the buffer
				String readMessage = new String(readBuf, 0, msg.arg1);

				addArrayAdapterFromFragmentRoomChat(connectedDeviceName + ":  "
						+ readMessage);
				break;
			case Const.MESSAGE_DEVICE_NAME:
				// save the connected device's name
				connectedDeviceName = msg.getData()
						.getString(Const.DEVICE_NAME);
				Toast.makeText(getApplicationContext(),
						"Connected to " + connectedDeviceName,
						Toast.LENGTH_SHORT).show();
				break;
			case Const.MESSAGE_TOAST:
				Toast.makeText(getApplicationContext(),
						msg.getData().getString(Const.TOAST),
						Toast.LENGTH_SHORT).show();
				break;
			}
		}
	};

	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		switch (requestCode) {
		case Const.REQUEST_CONNECT_DEVICE:
			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {
				connectDevice(data);
			}
			break;
		case Const.REQUEST_ENABLE_BT:
			// When the request to enable Bluetooth returns
			if (resultCode == Activity.RESULT_OK) {
				// Bluetooth is now enabled, so set up a chat session
				// setupChat();
			} else {
				// User did not enable Bluetooth or an error occurred

				Toast.makeText(this, "Leaving", Toast.LENGTH_SHORT).show();
				finish();
			}
		}
	}

	private void connectDevice(Intent data) {
		// Get the device MAC address
		String address = data.getExtras().getString(
				DeviceListActivity.EXTRA_DEVICE_ADDRESS);
		// Get the BluetoothDevice object
		BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
		// Attempt to connect to the device
		chatService.connect(device);
	}

	@Override
	public void sendMessageFromFragmentRoomChat(String data) {
		sendMessage(data);
	}

	private void sendMessage(String message) {
		// Check that we're actually connected before trying anything
		if (chatService.getState() != Const.STATE_CONNECTED) {
			Toast.makeText(this, "Not Connected", Toast.LENGTH_SHORT).show();
			return;
		}

		// Check that there's actually something to send
		if (message.length() > 0) {
			// Get the message bytes and tell the BluetoothChatService to write
			byte[] send = message.getBytes();
			chatService.write(send);

		}
	}

}
