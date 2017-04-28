package cn.nubia.fulleclientdemo.base;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import cn.nubia.fulleclientdemo.MainActivity;
import cn.nubia.fulleclientdemo.R;

/**
 * 活动基类
 * Created by gbq on 2017-4-22.
 */

public class BaseActivity extends AppCompatActivity {
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void updateTvInfo(String string) {
        TextView tv_Info = (TextView) findViewById(R.id.tv_info);
        tv_Info.setText(string);
    }

    protected void showToast(String msg) {
        Toast.makeText(BaseActivity.this, msg, Toast.LENGTH_SHORT).show();
    }


    public void showProcess() {
        mProgressDialog = new ProgressDialog(this);
        if (!mProgressDialog.isShowing()) {
            mProgressDialog.setCancelable(false);
            mProgressDialog.setMessage("加载中...");
            mProgressDialog.show();
        }
    }

    public void closeProcess() {
        if (mProgressDialog != null && mProgressDialog.isShowing())
            mProgressDialog.dismiss();
    }

}
