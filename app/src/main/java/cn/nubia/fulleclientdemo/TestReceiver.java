
package cn.nubia.fulleclientdemo;

import cn.nubia.accountsdk.common.SDKLogUtils;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.widget.Toast;

public class TestReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = null;
        SDKLogUtils.i("onReceive()---------action:"+intent.getAction());
        if (intent != null && !TextUtils.isEmpty(action = intent.getAction())
                && "cn.nubia.account.broadcastchange".equalsIgnoreCase(action)) {
            String extra = intent.getExtras().getString("change");
            if ("login".equalsIgnoreCase(extra)) {
                SDKLogUtils.i("-----login--------");
                Toast.makeText(context, "账号登录", Toast.LENGTH_LONG).show();
            }
            if ("logout".equalsIgnoreCase(extra)) {
                SDKLogUtils.i("-------logout----");
                Toast.makeText(context, "账号退出", Toast.LENGTH_LONG).show();
            }
        }
    }

}
