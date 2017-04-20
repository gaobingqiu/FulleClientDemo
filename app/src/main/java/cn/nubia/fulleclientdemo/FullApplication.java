package cn.nubia.fulleclientdemo;

import android.app.Application;
import cn.nubia.accountsdk.fullclient.AccountFullClient;
import cn.nubia.accountsdk.http.util.HttpApis;

public class FullApplication extends Application{
	private static FullApplication mInstance;
	AccountFullClient fullClient = null;
	private static final String NUBIA_ACCOUNT_APPID = "SID-CSL905YZ5Jnp";
	private static final String NUBIA_ACCOUNT_APPKEY = "URD8Dizlhu5Fg2wK";
	private static final String NUBIA_ACCOUNT_APPID_TEST = "Ba5tw2bmEPZTmUe9";
	private static final String NUBIA_ACCOUNT_APPKEY_TEST = "GeOQy0fbHY1kru4Z";
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
}
