package cn.nubia.fulleclientdemo;

import android.app.Application;

import cn.nubia.accountsdk.fullclient.AccountFullClient;
import cn.nubia.accountsdk.http.util.HttpApis;

import static cn.nubia.fulleclientdemo.base.Define.NUBIA_ACCOUNT_APPID;
import static cn.nubia.fulleclientdemo.base.Define.NUBIA_ACCOUNT_APPID_TEST;
import static cn.nubia.fulleclientdemo.base.Define.NUBIA_ACCOUNT_APPKEY;
import static cn.nubia.fulleclientdemo.base.Define.NUBIA_ACCOUNT_APPKEY_TEST;
import static cn.nubia.fulleclientdemo.base.Define.NUBIA_DEVELOPER_APPID;
import static cn.nubia.fulleclientdemo.base.Define.NUBIA_DEVELOPER_APPKEY;

public class FullApplication extends Application{
	private static FullApplication mInstance;
	private AccountFullClient fullClient = null;

	@Override
	public void onCreate() {
		super.onCreate();
		mInstance = this;
		int curEnv = HttpApis.ServerEnvType.RELEASE;
		String customType = HttpApis.CustomType.COMMON;
		String appId = (curEnv == HttpApis.ServerEnvType.RELEASE) ? NUBIA_ACCOUNT_APPID : NUBIA_ACCOUNT_APPID_TEST;
		String appKey = (curEnv == HttpApis.ServerEnvType.RELEASE) ? NUBIA_ACCOUNT_APPKEY : NUBIA_ACCOUNT_APPKEY_TEST;
		fullClient = AccountFullClient.get(this, appId, appKey, null, curEnv, true, customType);
	}
	
	public static FullApplication getApplication(){
		return mInstance;
	}
	
	public AccountFullClient getFullClient(){
		return fullClient;
	}

	public void setFullClient(AccountFullClient fullClient){
		this.fullClient = fullClient;
	}
}
