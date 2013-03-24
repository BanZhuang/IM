package com.qicq.im.msg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.qicq.im.R;
import com.qicq.im.api.ChatMessage;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MsgListAdapter extends BaseAdapter {
	private Context context;

	private List<ChatListItem> list = new ArrayList<ChatListItem>();
	private Map<String,ChatListItem> mapedList = new HashMap<String,ChatListItem>();

	public MsgListAdapter(Context context) {
		super();
		this.context = context;
	}

	public int getCount() {
		return list.size();
	}

	public Object getItem(int position) {
		return list.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public void addItem(ChatListItem m){		
		if(m.msg.type == ChatMessage.MESSAGE_TYPE_HELLO){
			m.msg.targetId = ChatListItem.HELLO_ID;
		}
		else if(m.msg.type == ChatMessage.MESSAGE_TYPE_REQUEST){
			m.msg.targetId = ChatListItem.REQUEST_ID;
		}
		String id = m.msg.targetId;
		
		if(!mapedList.containsKey(id)){
			mapedList.put(id,m);
		}else{
			ChatListItem tmp = mapedList.get(id);
			if(tmp.msg.time < m.msg.time)
				tmp.setCountAndMsg(m.msg);
			mapedList.put(id,tmp);
		}
	}

	public void addItemDirectly(List<ChatListItem> cs){
		for(ChatListItem c : cs){
			mapedList.put(c.msg.targetId,c);
		}
	}

	//	public void addItem(List<ChatListItem> cs){
	//		for(ChatListItem c : cs){
	//			addItem(c);
	//		}
	//	}

	public List<ChatListItem> getItems(){
		return list;
	}

	public void setReaded(String targetId){
		ChatListItem tmp = mapedList.get(targetId);
		tmp.unreadCount = 0;
		mapedList.put(tmp.msg.targetId,tmp);
	}

	@Override
	public void notifyDataSetChanged(){
		list.clear();

		Iterator<Entry<String,ChatListItem>> iter = mapedList.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<String,ChatListItem> entry = iter.next();
			list.add((ChatListItem) entry.getValue());
		}

		super.notifyDataSetChanged();
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		ChatListItem m = list.get(position);
		if (convertView == null) {

			convertView = LayoutInflater.from(context).inflate(R.layout.msg_item, null);

			holder = new ViewHolder();
			holder.img = (ImageView) convertView.findViewById(R.id.msg_item_img);
			holder.name = (TextView) convertView.findViewById(R.id.msg_item_name);
			holder.info = (TextView) convertView.findViewById(R.id.msg_item_info);
			holder.time = (TextView) convertView.findViewById(R.id.msg_item_time);
			Drawable a = m.user.getAvatar();
			if(a == null)
				Log.v("MsgListAdapter","Fail to get avatar: " + m.user.localAvatarPath);
			else
				holder.img.setImageDrawable(a);
			if(m.msg.type == ChatMessage.MESSAGE_TYPE_REQUEST){
				//TODO Special item for request
				holder.name.setText("����");
				holder.info.setText("�����µ�����" + m.user.name + " ������");
				holder.time.setText(String.valueOf(m.unreadCount));
			}else if(m.msg.type == ChatMessage.MESSAGE_TYPE_HELLO){
				//TODO Special item for request
				holder.name.setText("�к�");
				holder.info.setText("�����µ�����" + m.user.name + " ���к�");
				holder.time.setText(String.valueOf(m.unreadCount));
			}else{
				holder.name.setText(m.user.name);
				holder.info.setText(m.msg.content);
				holder.time.setText(String.valueOf(m.unreadCount));
			}
		}
		convertView.setTag(holder);
		return convertView;
	}
	//�Ż�listview��Adapter
	static class ViewHolder {
		ImageView img;
		TextView name;
		TextView info;
		TextView time;
	}

}
