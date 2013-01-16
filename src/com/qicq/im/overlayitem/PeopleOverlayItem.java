package com.qicq.im.overlayitem;

import android.graphics.drawable.Drawable;

import com.baidu.mapapi.GeoPoint;
import com.qicq.im.R;

public class PeopleOverlayItem extends AbstaractOverlayItem{

	public String uid;
	public int dmandDrawableId;
	public int genderDrableId;
	
	public PeopleOverlayItem(GeoPoint pt, String title, String snippet,Drawable drawable,String uid,String gender,String demandName) {
		super(pt, title, snippet,drawable);
		
		this.uid = uid;
		
		if(gender.equals("��"))
			genderDrableId = R.drawable.gender_male;
		else if(gender.equals("Ů"))
			genderDrableId = R.drawable.gender_female;
		else 
			genderDrableId = R.drawable.avatar;
		
		if("����".equals(demandName))
			dmandDrawableId = R.drawable.coffee;
		else if("�Է�".equals(demandName))
			dmandDrawableId = R.drawable.dinner;
		else
			dmandDrawableId = R.drawable.bar;
	}

	public int getType() {
		return IOverlayItemType.TYPE_PEOPLE;
	}
}
