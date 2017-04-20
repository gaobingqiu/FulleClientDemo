
package cn.nubia.fulleclientdemo;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import cn.nubia.accountsdk.aidl.IAppWebSynLoginListener;
import cn.nubia.accountsdk.aidl.ICheckPasswordListener;
import cn.nubia.accountsdk.aidl.IGetAccountInfoListener;
import cn.nubia.accountsdk.aidl.IGetBaiduAccountInfoListener;
import cn.nubia.accountsdk.aidl.IGetThirdBindInfoListener;
import cn.nubia.accountsdk.aidl.SystemAccountInfo;
import cn.nubia.accountsdk.aidl.ThirdAccountBindInfo;
import cn.nubia.accountsdk.common.CetificationLackingException;
import cn.nubia.accountsdk.common.SDKLogUtils;
import cn.nubia.accountsdk.fullclient.AccountFullClient;
import cn.nubia.accountsdk.simpleclient.AccountSimpleClient;
import cn.nubia.fulleclientdemo.base.Define;
import cn.nubia.fulleclientdemo.base.LogUtils;

public class SimpleActivity extends Activity implements OnClickListener {
    private ImageView headimage;
    private EditText mPasswordEdit;
    private MainHandler mHandler;
    private TextView tv_Info;
    private final static int HANDLER_GET_SYSTEM_ACCOUNT = 1;
    private final static int HANDLER_GET_BAIDU_CLOUD_SPACE = 2;
    private final static int HANDLER_GET_BAIDU_CLOUD_ACCOUNTINFO = 3;
    private final static int HANDLER_START_BIND_BAIDU_CLOUD = 4;
    private final static int HANDLER_GET_THIRD_BINDINFO = 5;
    private final static int HANDLER_CHECKOUT_PASSWORD = 6;
    private final static int HANDLER_WEB_SYN_LOGIN = 7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IntentFilter filter = new IntentFilter("cn.nubia.account.broadcastchange");
        this.getApplicationContext().registerReceiver(new TestReceiver(), filter);
        setContentView(R.layout.activity_simple);
        SDKLogUtils.setLogFlag(true);
        SDKLogUtils.i("账户App是否支持新的SDK API：" + AccountSimpleClient.get(this).isSurportNewApi());
        findViewById(R.id.account_login_register).setOnClickListener(this);
        findViewById(R.id.system_account_detail).setOnClickListener(this);
        findViewById(R.id.account_login_token_invlaid).setOnClickListener(this);
        findViewById(R.id.bt_certification).setOnClickListener(this);
        findViewById(R.id.bt_web_syn_login).setOnClickListener(this);
        headimage = (ImageView) this.findViewById(R.id.head_image);
        findViewById(R.id.get_system_account_info).setOnClickListener(this);
        findViewById(R.id.get_baiducloudspace).setOnClickListener(this);
        findViewById(R.id.get_baiduclou_accountinfo).setOnClickListener(this);
        findViewById(R.id.start_bind_baiducloud).setOnClickListener(this);
        findViewById(R.id.get_thirdbindinfo).setOnClickListener(this);
        mPasswordEdit = (EditText) findViewById(R.id.edt_input_password);
        findViewById(R.id.check_password).setOnClickListener(this);
        tv_Info = (TextView) findViewById(R.id.tv_info);
        mHandler = new MainHandler(SimpleActivity.this, getMainLooper());
    }

    @Override
    public void onClick(View v) {
        AccountFullClient fullClient = FullApplication.getApplication().getFullClient();
        switch (v.getId()) {
            case R.id.account_login_register:
                fullClient.loginOrRegister(SimpleActivity.this);
                break;
            case R.id.system_account_detail:
                fullClient.jumptoAccountDetailActivity(SimpleActivity.this);
                break;
            case R.id.account_login_token_invlaid:
                fullClient.reLoginWhenTokenInvalid(SimpleActivity.this);
                break;
            case R.id.bt_certification:
                try {
                    fullClient.jumptoCertificationActivity(SimpleActivity.this);
                } catch (CetificationLackingException exception) {
                    updateTvInfo("CetificationLackingException");
                }
                break;
            case R.id.bt_web_syn_login:
                showSingleChoiceDialog(fullClient);
                break;
            case R.id.get_system_account_info:
                try {
                    fullClient.getSystemAccountInfo(new IGetAccountInfoListener.Stub() {
                        @Override
                        public void onException(int errorCode, String errorMsg) throws RemoteException {
                            SDKLogUtils.i("errorCode:" + errorCode + " | " + "errorMsg:" + errorMsg);
                        }

                        @Override
                        public void onComplete(SystemAccountInfo accountInfo) throws RemoteException {
                            Define.tokenId = accountInfo.getTokenId();
                            Message msg = Message.obtain();
                            Bundle bundle = new Bundle();
                            bundle.putParcelable("account_info", accountInfo);
                            msg.setData(bundle);
                            msg.what = HANDLER_GET_SYSTEM_ACCOUNT;
                            mHandler.sendMessage(msg);
                        }
                    });
                } catch (RemoteException e) {
                    SDKLogUtils.i("远程服务挂掉");
                    e.printStackTrace();
                }
                break;
            case R.id.get_baiducloudspace:
                try {
                    fullClient.getCloudSpace(new IGetAccountInfoListener.Stub() {
                        @Override
                        public void onException(int errorcode, String errormsg) throws RemoteException {
                        }

                        @Override
                        public void onComplete(SystemAccountInfo info) throws RemoteException {
                            if (info != null) {
                                String cloud_space = info.getString(SystemAccountInfo.KEY_MY_CLOUD_SPACE);
                                SDKLogUtils.i("云空间剩余：" + cloud_space);
                                Message msg = Message.obtain();
                                msg.what = HANDLER_GET_BAIDU_CLOUD_SPACE;
                                Bundle bundle = new Bundle();
                                bundle.putString("cloud_space", cloud_space);
                                msg.setData(bundle);
                                mHandler.sendMessage(msg);
                            } else {
                                Message msg = Message.obtain();
                                msg.what = HANDLER_GET_BAIDU_CLOUD_SPACE;
                                mHandler.sendMessage(msg);
                            }

                        }
                    });
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.get_baiduclou_accountinfo:
                try {
                    fullClient.getBaiduAccountInfo(new IGetBaiduAccountInfoListener.Stub() {

                        @Override
                        public void onException(String errormsg) throws RemoteException {
                        }

                        @Override
                        public void onComplete(String baidutoken, String baiduuid, String expiresin) throws RemoteException {
                            SDKLogUtils.i("baidutoken:" + baidutoken + "|baiduuid:" + baiduuid + "|expiresin:" + expiresin);
                            Message msg = Message.obtain();
                            msg.what = HANDLER_GET_BAIDU_CLOUD_ACCOUNTINFO;
                            Bundle bundle = new Bundle();
                            bundle.putString("baidutoken", baidutoken);
                            bundle.putString("baiduuid", baiduuid);
                            bundle.putString("expiresin", expiresin);
                            msg.setData(bundle);
                            mHandler.sendMessage(msg);
                        }

                        @Override
                        public void onCancel() throws RemoteException {
                        }
                    });
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.start_bind_baiducloud:
                try {
                    fullClient.startBindBaiduAccount(false, new IGetBaiduAccountInfoListener.Stub() {

                        @Override
                        public void onException(String errormsg) throws RemoteException {
                        }

                        @Override
                        public void onComplete(String baidutoken, String baiduuid, String expiresin) throws RemoteException {
                            SDKLogUtils.i("baidutoken:" + baidutoken + "|baiduuid:" + baiduuid + "|expiresin:" + expiresin);
                            Message msg = Message.obtain();
                            msg.what = HANDLER_START_BIND_BAIDU_CLOUD;
                            Bundle bundle = new Bundle();
                            bundle.putString("baidutoken", baidutoken);
                            bundle.putString("baiduuid", baiduuid);
                            bundle.putString("expiresin", expiresin);
                            msg.setData(bundle);
                            mHandler.sendMessage(msg);
                        }

                        @Override
                        public void onCancel() throws RemoteException {

                        }
                    });
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.get_thirdbindinfo:
                try {
                    fullClient.getThirdBindInfo(new IGetThirdBindInfoListener.Stub() {

                        @Override
                        public void onException(int errorcode, String errormsg) throws RemoteException {
                            SDKLogUtils.i("ThirdAccountBindInfo:" + errorcode + errormsg);
                            Message msg = Message.obtain();
                            msg.what = HANDLER_GET_THIRD_BINDINFO;
                            Bundle bundle = new Bundle();
                            bundle.putString("errormsg", errormsg);
                            bundle.putString("errorcode", "" + errorcode);
                            msg.setData(bundle);
                            mHandler.sendMessage(msg);
                        }

                        @Override
                        public void onComplete(ThirdAccountBindInfo info) throws RemoteException {
                            SDKLogUtils.i("ThirdAccountBindInfo:" + info.toString());
                            Message msg = Message.obtain();
                            msg.what = HANDLER_GET_THIRD_BINDINFO;
                            String thirdInfo = info.toString();
                            Bundle bundle = new Bundle();
                            bundle.putString("third_bind", thirdInfo);
                            msg.setData(bundle);
                            mHandler.sendMessage(msg);
                        }
                    });
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.check_password:
                String checkPassword = mPasswordEdit.getText().toString();
                if (TextUtils.isEmpty(checkPassword)) {
                    Toast.makeText(SimpleActivity.this, "请输入要检查的密码", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    fullClient.checkPassword(checkPassword, new ICheckPasswordListener.Stub() {
                        @Override
                        public void onException(String arg0) throws RemoteException {
                            SDKLogUtils.i("check 密码,错误：" + arg0);
                            Message msg = Message.obtain();
                            msg.what = HANDLER_CHECKOUT_PASSWORD;
                            boolean isCheckResult = false;
                            Bundle bundle = new Bundle();
                            bundle.putBoolean("isCheckResult", isCheckResult);
                            msg.setData(bundle);
                            mHandler.sendMessage(msg);
                        }

                        @Override
                        public void onComplete(boolean arg0) throws RemoteException {
                            SDKLogUtils.i("check 密码：" + arg0);
                            Message msg = Message.obtain();
                            msg.what = HANDLER_CHECKOUT_PASSWORD;
                            boolean isCheckResult = arg0;
                            Bundle bundle = new Bundle();
                            bundle.putBoolean("isCheckResult", isCheckResult);
                            msg.setData(bundle);
                            mHandler.sendMessage(msg);
                        }
                    });
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        SDKLogUtils.i("requestCode=" + requestCode);
        if (requestCode == AccountSimpleClient.REQUEST_TOKEN_CERTIFICATION && resultCode != 0) {
            updateTvInfo(requestCode + ":" + resultCode);
        }
    }

    public static class MainHandler extends Handler {
        WeakReference<SimpleActivity> reference;

        public MainHandler(SimpleActivity activity, Looper mainLooper) {
            super(mainLooper);
            reference = new WeakReference<SimpleActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle bundle = msg.getData();
            switch (msg.what) {
                case HANDLER_GET_SYSTEM_ACCOUNT:
                    if (null != bundle) {
                        SystemAccountInfo accountInfo = (SystemAccountInfo) bundle.getParcelable("account_info");
                        if (accountInfo == null) {
                            reference.get().toTast("读取系统账号信息为null，请先登录");
                        }
                        reference.get().updateAccountInfo(accountInfo);
                    }
                    break;
                case HANDLER_GET_BAIDU_CLOUD_SPACE:
                    if (null != bundle) {
                        String cloud_space = bundle.getString("cloud_space");
                        if (!TextUtils.isEmpty(cloud_space)) {
                            reference.get().toTast("获取百度云空间" + cloud_space);
                            reference.get().updateTvInfo("获取百度云空间" + cloud_space);
                        } else {
                            reference.get().toTast("获取百度云空间为null，请先登录");
                            reference.get().updateTvInfo("获取百度云空间为null，请先登录");
                        }
                    }
                    break;
                case HANDLER_GET_BAIDU_CLOUD_ACCOUNTINFO:
                    if (null != bundle) {
                        String baidutoken = bundle.getString("baidutoken", "");
                        String baiduuid = bundle.getString("baiduuid", "");
                        String expiresin = bundle.getString("expiresin", "");
                        if (TextUtils.isEmpty(baidutoken)
                                && TextUtils.isEmpty(baiduuid)
                                && TextUtils.isEmpty(expiresin)) {
                            reference.get().toTast("获取百度云账号信息:baidutoken uid expiersin为null，请先登录");
                            reference.get().updateTvInfo("获取百度云账号信息:baidutoken uid expiersin为null，请先登录");
                            return;
                        }
                        StringBuffer string = new StringBuffer();
                        string.append("获取百度云账号信息");
                        string.append("baidutoken=");
                        string.append(baidutoken);
                        string.append("|baiduuid=");
                        string.append(baiduuid);
                        string.append("|expiresin=");
                        string.append(expiresin);
                        reference.get().toTast(string.toString());
                        reference.get().updateTvInfo(string.toString());
                    }
                    break;
                case HANDLER_START_BIND_BAIDU_CLOUD:
                    if (null != bundle) {
                        String baidutoken = bundle.getString("baidutoken", "");
                        String baiduuid = bundle.getString("baiduuid", "");
                        String expiresin = bundle.getString("expiresin", "");
                        StringBuffer string = new StringBuffer();
                        string.append("重新绑定百度云");
                        string.append("baidutoken=");
                        string.append(baidutoken);
                        string.append("|baiduuid=");
                        string.append(baiduuid);
                        string.append("|expiresin=");
                        string.append(expiresin);
                        reference.get().toTast(string.toString());
                        reference.get().updateTvInfo(string.toString());
                    }
                    break;
                case HANDLER_GET_THIRD_BINDINFO:
                    if (null != bundle) {
                        String third_bind = bundle.getString("third_bind", "");
                        if (!TextUtils.isEmpty(third_bind)) {
                            reference.get().toTast("第三方绑定情况: " + third_bind);
                            reference.get().updateTvInfo("第三方绑定情况: " + third_bind);
                        } else {
                            String errormsg = bundle.getString("errormsg", "");
                            String errorcode = bundle.getString("errorcode", "");
                            reference.get().toTast("第三方绑定情况: errorcode=" + errorcode + "errormsg=" + errormsg + "请先登录");
                            reference.get().updateTvInfo("第三方绑定情况: errorcode=" + errorcode + "errormsg=" + errormsg + "请先登录");
                        }
                    }
                    break;
                case HANDLER_CHECKOUT_PASSWORD:
                    if (null != bundle) {
                        boolean isCheckResult = bundle.getBoolean("isCheckResult", false);
                        String is = isCheckResult ? "密码正确" : "密码错误";
                        reference.get().toTast("验证密码: " + is);
                        reference.get().updateTvInfo("验证密码: " + is);
                    }
                    break;

                case HANDLER_WEB_SYN_LOGIN:
                    if (null != bundle) {
                        String errorMsg = bundle.getString("error_msg", "");
                        if (!TextUtils.isEmpty(errorMsg)) {
                            reference.get().updateTvInfo(errorMsg);
                        } else {
                            String synUrl = bundle.getString("syn_url", "");
                            reference.get().startWebActivity(synUrl);
                        }
                    }
                    break;
            }
            SDKLogUtils.i("收到handler 刷新");
        }
    }

    private void startWebActivity(String synUrl) {
        Intent intent = new Intent();
        intent.putExtra("url", mUrl);
        intent.putExtra("syn_url", synUrl);
        intent.setClass(SimpleActivity.this, WebActivity.class);
        startActivity(intent);
    }

    private void updateHeadImage(Bitmap map) {
        headimage.setImageBitmap(map);
        headimage.invalidate();
    }

    private void toTast(String msg) {
        Toast.makeText(SimpleActivity.this, msg, Toast.LENGTH_SHORT).show();
    }

    private void updateAccountInfo(SystemAccountInfo accountInfo) {
        if (accountInfo == null) {
            headimage.setImageBitmap(null);
            tv_Info.setText("");
        }
        Bitmap bitmap = accountInfo.getHeadImage();
        if (bitmap != null && !bitmap.isRecycled()) {
            headimage.setImageBitmap(bitmap);
        }
        headimage.invalidate();
        tv_Info.setText("账户详情=" + accountInfo.toString());
    }

    private void updateTvInfo(String string) {
        tv_Info.setText(string);
    }

    private int mChoice;
    private String mUrl;

    private void showSingleChoiceDialog(final AccountFullClient fullClient) {
        final AlertDialog.Builder singleChoiceDialog =
                new AlertDialog.Builder(this);
        singleChoiceDialog.setTitle("请选择url：");
        // 第二个参数是默认选项，此处设置为0
        singleChoiceDialog.setSingleChoiceItems(Define.WEB_URL, 0,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mChoice = which;
                    }
                });
        singleChoiceDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mUrl = Define.WEB_URL[mChoice];
                        try {
                            fullClient.appWebSynlogin(Define.WEB_URL[mChoice], new IAppWebSynLoginListener.Stub() {
                                @Override
                                public void onComplete(String s) {
                                    LogUtils.d(s);
                                    Message msg = Message.obtain();
                                    msg.what = HANDLER_WEB_SYN_LOGIN;
                                    Bundle bundle = new Bundle();
                                    bundle.putString("syn_url", s);
                                    msg.setData(bundle);
                                    mHandler.sendMessage(msg);
                                }

                                @Override
                                public void onException(String s) {
                                    LogUtils.d(s);
                                    Message msg = Message.obtain();
                                    msg.what = HANDLER_WEB_SYN_LOGIN;
                                    Bundle bundle = new Bundle();
                                    bundle.putString("error_msg", s);
                                    msg.setData(bundle);
                                    mHandler.sendMessage(msg);
                                }
                            });
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                }
        );
        singleChoiceDialog.show();
    }
}