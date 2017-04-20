package cn.nubia.fulleclientdemo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import cn.nubia.fulleclientdemo.base.LogUtils;

@SuppressLint({"NewApi", "SetJavaScriptEnabled"})
public class WebActivity extends Activity {
    WebView mWebView;
    String mUrl;
    String mSynUrl;
    String TAG = "WebActivity";
    boolean isSynFinish = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        setContentView(R.layout.activity_web);
        mWebView = (WebView) findViewById(R.id.wb_sync);
        mUrl = getIntent().getExtras().getString("url");
        mSynUrl = getIntent().getExtras().getString("syn_url");
        LogUtils.d("mUrl=" + mUrl);
        LogUtils.d("mSynUrl=" + mSynUrl);
        load();
    }

    private void load() {
        WebSettings wvSettings = this.mWebView.getSettings();
        wvSettings.setJavaScriptEnabled(true);
        wvSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        wvSettings.setAppCacheEnabled(false);
        wvSettings.setBuiltInZoomControls(true);
        wvSettings.setJavaScriptEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            wvSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        WebView.setWebContentsDebuggingEnabled(true);
        this.mWebView.setWebViewClient(new AuthorizeWebViewClient());
        mWebView.loadUrl(mSynUrl);
    }

    String cookieStr;

    class AuthorizeWebViewClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            Log.d(TAG, "onPageStarted" + "url = " + url);
            //必须加上以下三行，否则会返回空页面
            if(TextUtils.equals(url,mSynUrl) && isSynFinish){
                finish();
            }
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            Log.d(TAG, "onPageFinished" + "url = " + url);
            super.onPageFinished(view, url);
            if (!isSynFinish) {
                isSynFinish = true;
//				synCookies(WebActivity.this,mUrl,cookieStr);
                mWebView.loadUrl(mUrl);
            }
        }

        @SuppressWarnings("deprecation")
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return super.shouldOverrideUrlLoading(view, url);
        }

        @SuppressWarnings("deprecation")
        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            Log.d(TAG, "onReceivedError failingUrl=" + failingUrl);
            super.onReceivedError(view, errorCode, description, failingUrl);
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            Log.d(TAG, "onReceivedSslError");
            handler.proceed();
        }
    }

    /**
     * 同步一下cookie
     */
    public static void synCookies(Context context, String url, String cookies) {
        CookieSyncManager.createInstance(context);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.removeSessionCookie();//移除
        cookieManager.setCookie(url, cookies);//cookies是在HttpClient中获得的cookie
        CookieSyncManager.getInstance().sync();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mWebView.canGoBack()) {
                mWebView.goBack();//返回上一页面
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

}
