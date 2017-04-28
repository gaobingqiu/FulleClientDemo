
package cn.nubia.fulleclientdemo;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

import cn.nubia.accountsdk.common.SDKLogUtils;
import cn.nubia.accountsdk.fullclient.AccountFullClient;
import cn.nubia.accountsdk.http.NetResponseListener;
import cn.nubia.accountsdk.http.model.CommonResponse;
import cn.nubia.accountsdk.http.util.HttpApis;
import cn.nubia.fulleclientdemo.base.BaseActivity;
import cn.nubia.fulleclientdemo.base.Define;
import cn.nubia.fulleclientdemo.base.LogUtils;

public class MainActivity extends BaseActivity implements OnClickListener {
    private EditText edt_username;
    private EditText edt_password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IntentFilter filter = new IntentFilter("cn.nubia.account.broadcastchange");
        this.getApplicationContext().registerReceiver(new TestReceiver(), filter);
        setContentView(R.layout.activity_main);
        findIds();
        LogUtils.e("error");
        Log.e("MainActivity","error");
    }

    private void findIds() {
        findViewById(R.id.btn_set_app).setOnClickListener(this);
        findViewById(R.id.btn_set_app).setVisibility(View.GONE);
        findViewById(R.id.btn_to_simple).setOnClickListener(this);
        findViewById(R.id.btn_check_account).setOnClickListener(this);
        findViewById(R.id.btn_web_syn_login).setOnClickListener(this);
        findViewById(R.id.btn_web_syn_login).setVisibility(View.GONE);
        edt_username = (EditText) findViewById(R.id.edt_username);
        edt_password = (EditText) findViewById(R.id.edt_password);
    }

    @Override
    public void onClick(View v) {
        AccountFullClient fullClient = FullApplication.getApplication().getFullClient();
        switch (v.getId()) {
            case R.id.btn_set_app:
                showInputInfoDialog();
                break;
            case R.id.btn_to_simple:
                Intent intent = new Intent(MainActivity.this, SimpleActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_check_account:
                String username = edt_username.getText().toString();
                String password = edt_password.getText().toString();
                if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
                    showToast("用户或密码不能为空");
                    return;
                }
                showProcess();
                fullClient.loginOrCheckAccount(username, password, new NetResponseListener<CommonResponse>() {
                    @Override
                    public void onResult(CommonResponse result) {
                        closeProcess();
                        if (result.getErrorCode() == 0) {
                            Object value = result.get("unique_code");
                            if (value != null && !TextUtils.isEmpty((String) value)) {
                                showToast("loginOrCheckAccount=" + (String) value);
                            }
                        } else {
                            showToast("code=" + result.getErrorCode() + ",msg=" + result.getErrorMessage());
                        }
                    }
                });
                break;
            case R.id.btn_web_syn_login:
                if (TextUtils.isEmpty(Define.tokenId)) {
                    showToast("请先登录apk并且获取tokenId");
                    return;
                }
                showSingleChoiceDialog(fullClient);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        SDKLogUtils.i("requestCode=" + requestCode);
    }

    private int mChoice;
    private String mUrl;

    private void showSingleChoiceDialog(final AccountFullClient fullClient) {
        final AlertDialog.Builder singleChoiceDialog =
                new AlertDialog.Builder(this);
        singleChoiceDialog.setTitle("请选择url：");
        // 第二个参数是默认选项，此处设置为0
        singleChoiceDialog.setSingleChoiceItems(Define.WEB_URL, mChoice,
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
//                        fullClient.appWebSynlogin(Define.tokenId, mUrl, mListener);
                    }
                }
        );
        singleChoiceDialog.show();
    }

    NetResponseListener<CommonResponse> mListener = new NetResponseListener<CommonResponse>() {
        @Override
        public void onResult(CommonResponse response) {
            LogUtils.d(String.valueOf(response));
            if (response.getErrorCode() == 0) {
                Intent intent = new Intent();
                intent.putExtra("url", mUrl);
                intent.putExtra("syn_url", (String) response.get("syn_url"));
                intent.setClass(MainActivity.this, WebActivity.class);
                startActivity(intent);
            } else {
                showToast(response.getErrorMessage());
            }
        }
    };

    private void showInputInfoDialog() {
        final AlertDialog.Builder inputDialog =
                new AlertDialog.Builder(this);
        final View dialogView = LayoutInflater.from(MainActivity.this)
                .inflate(R.layout.dialog_input_info, null);
        final EditText realNameEt = (EditText) dialogView.findViewById(R.id.et_real_name);
        realNameEt.setHint("appId");
        realNameEt.setText(Define.NUBIA_DEVELOPER_APPID_TEST);
        final EditText idCardEt = (EditText) dialogView.findViewById(R.id.et_id_card);
        idCardEt.setHint("appKey");
        idCardEt.setText(Define.NUBIA_DEVELOPER_APPKEY_TEST);
        inputDialog.setTitle("请输入appId和appKey：").setView(dialogView);
        inputDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String realName = realNameEt.getText().toString();
                        String idCard = idCardEt.getText().toString();
                        if (TextUtils.isEmpty(realName) || TextUtils.isEmpty(idCard)) {
                            Toast.makeText(MainActivity.this, "请输入appId和appKey", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        AccountFullClient.release();
                        AccountFullClient client = AccountFullClient.get(MainActivity.this,
                                realName, idCard, null, HttpApis.ServerEnvType.TEST, true, HttpApis.CustomType.COMMON);
                        FullApplication.getApplication().setFullClient(client);
                        Toast.makeText(MainActivity.this, "设置完成", Toast.LENGTH_SHORT).show();
                    }
                }
        );
        inputDialog.show();
    }

}
