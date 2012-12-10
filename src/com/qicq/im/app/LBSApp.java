package com.qicq.im.app;

import java.lang.Thread.State;
import java.util.List;

import com.baidu.mapapi.GeoPoint;
import com.qicq.im.Utilities.Utility;
import com.qicq.im.api.APIManager;
import com.qicq.im.api.ChatMessage;
import com.qicq.im.api.LocationCluster;
import com.qicq.im.api.User;
import com.qicq.im.config.UserConfig;
import com.qicq.im.msg.ChatListItem;
import com.qicq.im.msg.MsgRcvListener;
import com.qicq.im.service.LBSService;
import com.qicq.im.service.LBSService.LBSBinder;
import com.qicq.im.thread.RcvMessageThread;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

/**
 * Main access point of the whole program
 * @author X
 *
 */
public class LBSApp extends Application{

	public static LBSApp app = null;
	private LBSService service;
	private APIManager api = null;

	@Override
	public void onCreate(){
		app = this;

		startService();
		initService();

		if(!Utility.isNetworkAvailable(this.getApplicationContext())){
			Toast.makeText(this, "���������������", Toast.LENGTH_LONG).show();
		}	

		super.onCreate();
	}

	private ServiceConnection conn = new ServiceConnection() {  

		public void onServiceConnected(ComponentName name, IBinder service) {  
			LBSBinder binder = (LBSService.LBSBinder) service;  

			app.service = (LBSService) (binder.getService());
			Log.v("LBSApp", "Service Connected!!!");
			api = app.service.api;
		}  

		public void onServiceDisconnected(ComponentName name) {
			Log.v("LBSApp", "Service Disconnected!!!");
		}
	};

	private void initService(){
		Intent intent = new Intent();
		intent.setAction("com.qicq.im.service.LBSService");
		bindService(intent, conn, Context.BIND_AUTO_CREATE); 
	}

	public void startService(){
		Intent intent = new Intent();
		intent.setAction("com.qicq.im.service.LBSService");
		startService(intent);
	}

	public void updateAllData(){
		//TODO update user,location,user list and cluster...
	}

	@Override
	public void onTerminate(){
	}

	public void startMsgThread(){
		if(service != null){
			if(service.rcvMsgThread.getState() == State.NEW)
				service.rcvMsgThread.start();
			else
				service.rcvMsgThread.setNeedPause(false);
			if(service.sendMsgThread.getState() == State.NEW)
				service.sendMsgThread.start();
			else
				service.sendMsgThread.setNeedPause(false);
		}else
			Log.e("LBSApp", "service not bind yet!!!");
	}

	public boolean isUserLogin(){
		if(service == null)
			return false;
		return service.user == null;
	}

	public GeoPoint getUserLocation(){
		if(service == null)
			return null;
		if(service.user == null)
			return null;
		return new GeoPoint(service.user.lat,service.user.lng);
	}

	//	public User getUser(){
	//		if(service.user == null){
	//			User u = service.userModel.getUser(userConfig.getUid());
	//			if(userConfig.isMeNeedUpdate() || u == null){
	//				service.user = api.getUser(null);
	//				service.userModel.updateUser(service.user);
	//				userConfig.setMeUpdate();
	//			}else{
	//				service.user = u;
	//			}
	//		}
	//		return service.user;
	//	}
	//
	//	public User getUser(String uid){
	//		User u = service.userModel.getUser(uid);
	//		if(u == null || userConfig.isFriendNeedUpdate()){
	//			u = api.getUser(uid);
	//			service.userModel.updateUser(u);
	//		}
	//		return u;
	//	}
	public User getUser(){
		return service.getUser();
	}

	public User getUser(String uid){
		return service.getUser(uid);
	}

	public boolean isNeedLogin(){
		//String cookie = service.userConfig.getCookie();
		String cookie = UserConfig.getCookie(this, "config");
		if(cookie != null){
			return false;
		}
		return true;
	}

	public int login(String email,String pwd){
		User u = api.UserLogin(email, pwd);
		if (u == null)
			return -1;
		else{
			service.initDatabase(String.valueOf(u.uid));
			service.userModel.updateUser(u);
			service.userConfig.setUid(String.valueOf(u.uid));
			service.userConfig.setCookie(api.getCookie());
			return 0;
		}
	}

	public void logout(){
		service.sendMsgThread.setNeedPause(true);
		service.rcvMsgThread.setNeedPause(true);
		api.setCookie(null);
		service.userConfig.setCookie(null);
	}

	public List<User> getNearbyPeople(boolean refresh){
		List<User> tmp = service.userModel.fetchAllNearby();

		if(refresh || tmp.isEmpty() || service.userConfig.isNearbyNeedUpdate()){
			List<User> news = api.NearbyPeople();
			if(news.size() != 0){
				tmp = news;
				service.userModel.updateAll(tmp);
				service.userConfig.setNearbyUpdate();
			}
		}
		return tmp;
	}

	public List<User> getFriends(boolean refresh){
		List<User> tmp = service.userModel.fetchAllFriends();

		if(refresh || tmp.isEmpty()|| service.userConfig.isFriendNeedUpdate()){
			List<User> news = api.AllMyFriend();
			if(news.size() != 0){
				tmp = news;
				service.userModel.updateAll(tmp);
				service.userConfig.setFriendUpdate();
			}
		}
		return tmp;
	}

	public List<User> getFans(boolean refresh){
		List<User> tmp = service.userModel.fetchAllFans();

		if(refresh || tmp.isEmpty() || service.userConfig.isFriendNeedUpdate()){
			List<User> news = api.AllMyFriend();
			if(news.size() != 0){
				tmp = news;
				service.userModel.updateAll(tmp);
				service.userConfig.setFriendUpdate();
			}
		}
		return tmp;
	}

	public List<User> getFollowed(boolean refresh){
		List<User> tmp = service.userModel.fetchAllFollowed();

		if(refresh || tmp.isEmpty() || service.userConfig.isFriendNeedUpdate()){
			List<User> news = api.AllMyFriend();
			if(news.size() != 0){
				tmp = news;
				service.userModel.updateAll(tmp);
				service.userConfig.setFriendUpdate();
			}
		}
		return tmp;
	}

	public int LocationUpdate(int lat,int lng){
		service.user.lat = lat;
		service.user.lng = lng;
		return api.LocationUpdate(lat, lng);
	}

	public void sendMessage(ChatMessage msg){
		service.sendMsgThread.addMsgs(msg);
		service.rcvMsgThread.setSleepTime(RcvMessageThread.MIN_TIME);
	}

	public void addMsgRcvListener(MsgRcvListener l){
		service.rcvMsgThread.addMsgRcvListener(l);
	}

	public void removeMsgRcvListener(MsgRcvListener l){
		service.rcvMsgThread.removeMsgRcvListener(l);
	}

	public void setTalkingToId(String uid) {
		service.talkingToId = uid;
	}

	public String getCookie(){
		return api.getCookie();
	}

	public void setCookie(String cookie){
		api.setCookie(cookie);
	}

	public List<ChatMessage> getAllMsg(String targetid){
		return service.msgModel.fetchAll(targetid);
	}

	public void saveAllMsg(List<ChatMessage> msgs){
		service.msgModel.insertAll(msgs);
	}
	
	public List<ChatListItem> getAllChattingList(){
		return service.chatListModel.fetchAll();
	}
	
	public void saveAllChattingList(List<ChatListItem> clis){
		 service.chatListModel.insertAll(clis);
	}

	public int PublishDemands(String name,String startH,String startM,
			String endH,String endM,String sexType){
		return api.PublishDemands(name, startH, startM, endH, endM, sexType);
	}

	public int UserReg(String name,String email,String pwd){
		return api.UserReg(name, email, pwd);
	}

	public List<LocationCluster> GetLocationCluster(int ltLat, int ltLng, 
			int rbLat, int rbLng, int gender, int agelevel,String updatetime){
		return api.GetLocationCluster(ltLat,ltLng,rbLat,rbLng,gender,agelevel,updatetime);
	}
}
