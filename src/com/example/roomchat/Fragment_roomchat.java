package com.example.roomchat;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Fragment;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.greentooth.R;

public class Fragment_roomchat extends Fragment {
	private ListView mConversationView;
	private EditText mOutEditText;
	private Button mSendButton;
	Communicator comm_sendMessage = null;

	public void setCommunicator(Communicator comm) {
		this.comm_sendMessage = comm;
	}

	// Array adapter for the conversation thread
	private ArrayAdapter<String> mConversationArrayAdapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_roomchat, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		setupChat();
	}

	public interface Communicator {
		void sendMessageFromFragmentRoomChat(String data);
	}

	private void setupChat() {

		// Initialize the array adapter for the conversation thread
		mConversationArrayAdapter = new ArrayAdapter<String>(getActivity(),
				R.layout.message);
		mConversationView = (ListView) getActivity().findViewById(R.id.in);
		mConversationView.setAdapter(mConversationArrayAdapter);
		mConversationView.setRight(TRIM_MEMORY_COMPLETE);
		// Initialize the compose field with a listener for the return key
		mOutEditText = (EditText) getActivity()
				.findViewById(R.id.edit_text_out);
		mOutEditText.setOnEditorActionListener(mWriteListener);

		// Initialize the send button with a listener that for click events
		mSendButton = (Button) getActivity().findViewById(R.id.button_send);
		mSendButton.setOnClickListener(Click);

		// Initialize the BluetoothChatService to perform bluetooth connections

		new StringBuffer("");
	}

	public ArrayAdapter<String> getmConversationArrayAdapter() {
		return mConversationArrayAdapter;
	}

	public void setmConversationArrayAdapter(
			ArrayAdapter<String> mConversationArrayAdapter) {
		this.mConversationArrayAdapter = mConversationArrayAdapter;
	}

	private TextView.OnEditorActionListener mWriteListener = new TextView.OnEditorActionListener() {
		public boolean onEditorAction(TextView view, int actionId,
				KeyEvent event) {
			// If the action is a key-up event on the return key, send the
			// message
			if (actionId == EditorInfo.IME_NULL
					&& event.getAction() == KeyEvent.ACTION_UP) {
				String message = view.getText().toString();

				comm_sendMessage.sendMessageFromFragmentRoomChat(message);
				mOutEditText.setText("");
			}

			return true;
		}
	};

	OnClickListener Click = new OnClickListener() {

		@Override
		public void onClick(View v) {

			String message = mOutEditText.getText().toString();
			if (isMessageNotTooLong()) {
				comm_sendMessage.sendMessageFromFragmentRoomChat(message);
			}
			mOutEditText.setText("");
		}

		private boolean isMessageNotTooLong() {
			if (mOutEditText.getLineCount() > 5) {
				Toast.makeText(getActivity(), "Message too Long",
						Toast.LENGTH_LONG);
				return false;
			} else {
				return true;
			}
		}
	};

	public void clearArrayAdapter() {
		mConversationArrayAdapter.clear();
	}

	public void addArrayAdapterToFragmentRoomChat(String data) {
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		String currentDateandTime = sdf.format(new Date());
		mConversationArrayAdapter.add("[" + currentDateandTime + "]" + " "
				+ data);
	}

}
