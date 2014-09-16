package com.jike.shanglv.Enums;

import com.jike.shanglv.NetAndJson.UserInfo;

/**记录SharedPreferences中的键值
 * */
public enum SPkeys {
	SPNAME("mySP"),//SharedPreferences的名字
	
	userid("userid"),
	username("username"),
	userphone("userphone"),
	useremail("useremail"),
	amount("amount"),//虚拟账号金额
	siteid("siteid"),//系统id
	
	UserInfoJson("UserInfoJson"),
	lastUsername("lastUsername"),
	lastPassword("lastPassword"),
	autoLogin("autoLogin"),
	loginState("loginState"),
	gnjpContactPhone("gnjpContactPhone"),	//国内机票下单，上次的联系人手机号码
	gjjpContactPhone("gjjpContactPhone"),	//国际机票需求单，上次的联系人手机号码
	trainContactPhone("trainContactPhone")	//火车票，上次的联系人手机号码
	
	;

	private String key;

	private SPkeys(String s) {
		key = s;
	}

	public String getString() {
		return key;
	}
};
