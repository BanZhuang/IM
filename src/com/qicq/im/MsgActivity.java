package com.qicq.im;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;

import com.qicq.im.api.ChatMessage;
import com.qicq.im.app.LBSApp;
import com.qicq.im.msg.ChattingAdapter;
import com.qicq.im.msg.MsgRcvEvent;
import com.qicq.im.msg.MsgRcvListener;

@SuppressLint("HandlerLeak")
public class MsgActivity extends Activity {

	protected static final String TAG = "MainActivity";
	private ChattingAdapter chatHistoryAdapter;
	private List<ChatMessage> messages = new ArrayList<ChatMessage>();

	private ListView chatHistoryLv;
	private Button sendBtn;
	private EditText textEditor;
	private ImageView sendImageIv;
	private ImageView captureImageIv;
	private View recording;
	private PopupWindow menuWindow = null;

	private TextView friendName;
	
	private String friendUID = null;
	
	private LBSApp app;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chatting);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.chatting_title_bar);
		chatHistoryLv = (ListView) findViewById(R.id.chatting_history_lv);
		
		app = (LBSApp)this.getApplication();
		friendUID = this.getIntent().getStringExtra("uid");
		setAdapterForThis();
		
		sendBtn = (Button) findViewById(R.id.send_button);
		textEditor = (EditText) findViewById(R.id.text_editor);
		sendImageIv = (ImageView) findViewById(R.id.send_image);
		captureImageIv = (ImageView) findViewById(R.id.capture_image);
		sendBtn.setOnClickListener(l);
		sendImageIv.setOnClickListener(l);
		captureImageIv.setOnClickListener(l);

		friendName = (TextView) findViewById(R.id.chatting_contact_name);
		friendName.setText(this.getIntent().getCharSequenceExtra("name"));
		
		
		
		recording = findViewById(R.id.recording);
		recording.setOnTouchListener(new View.OnTouchListener() {

			public boolean onTouch(View v, MotionEvent event) {
				int action = event.getAction();

				switch (action) {
				case MotionEvent.ACTION_UP:
					v.setBackgroundResource(R.drawable.hold_to_talk_normal);
					if (menuWindow != null)
						menuWindow.dismiss();
					Log.d(TAG, "---onTouchEvent action:ACTION_UP");
					break;
				case MotionEvent.ACTION_DOWN:
					v.setBackgroundResource(R.drawable.hold_to_talk_pressed);
					ViewGroup root = (ViewGroup) findViewById(R.id.chat_root);
					View view = LayoutInflater.from(MsgActivity.this).inflate(R.layout.audio_recorder_ring, null);
					menuWindow = new PopupWindow(view, 180, 180);
					// @+id/recorder_ring
					view.findViewById(R.id.recorder_ring).setVisibility(View.VISIBLE);
					view.setBackgroundResource(R.drawable.pls_talk);
					menuWindow.showAtLocation(root, Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL, 0, 0);
					Log.d(TAG, "---onTouchEvent action:ACTION_DOWN");
					// AudioRecorder recorder=new AudioRecorder();

					break;

				}

				return true;
			}
		});
		
		
		app.addMsgRcvListener(m);
		app.setTalkingToId(friendUID);

	}
	
	private MsgRcvListener m = new MsgRcvListener(){

		public void onMsgRcved(MsgRcvEvent e, List<ChatMessage> msgs) {
			for(ChatMessage m : msgs){
				Log.v("New msg for "+m.targetId,m.content);
				if(m.targetId.equals(friendUID)){
					messages.add(m);						
				}
			}
			mHandler.sendMessage(new Message());
		}
		
	};
	
	@Override
	public void onDestroy(){
		app.removeMsgRcvListener(m);
		app.setTalkingToId(null);
		//app.saveAllMsg(messages);
		super.onDestroy();
	}
	
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			chatHistoryAdapter.notifyDataSetChanged();
		}
	};
	
	// ����adapter
	private void setAdapterForThis() {
		List<ChatMessage> tmp = app.getAllMsg(friendUID);
		if(tmp.size() != 0)
			messages.addAll(tmp);
		chatHistoryAdapter = new ChattingAdapter(this, messages);
		chatHistoryLv.setAdapter(chatHistoryAdapter);
	}

	/**
	 * ����ʱ�����
	 */
	private View.OnClickListener l = new View.OnClickListener() {

		public void onClick(View v) {

			if (v.getId() == sendBtn.getId()) {
				String str = textEditor.getText().toString();
				String sendStr;
				if (str != null
						&& (sendStr = str.trim().replaceAll("\r", "").replaceAll("\t", "").replaceAll("\n", "")
						.replaceAll("\f", "")) != "") {
					sendMessage(sendStr);

				}
				textEditor.setText("");

			} else if (v.getId() == sendImageIv.getId()) {
				Intent i = new Intent();
				i.setType("image/*");
				i.setAction(Intent.ACTION_GET_CONTENT);
				startActivityForResult(i, Activity.DEFAULT_KEYS_SHORTCUT);
			} else if (v.getId() == captureImageIv.getId()) {
				Intent it = new Intent("android.media.action.IMAGE_CAPTURE");
				startActivityForResult(it, Activity.DEFAULT_KEYS_DIALER);
			}
		}

		// ģ�ⷢ����Ϣ
		private void sendMessage(String sendStr) {
			ChatMessage msg = ChatMessage.fromSender(ChatMessage.MESSAGE_TYPE_TEXT, sendStr,friendUID);
			messages.add(msg);
			chatHistoryAdapter.notifyDataSetChanged();
			app.sendMessage(msg);
		}

	};
}
