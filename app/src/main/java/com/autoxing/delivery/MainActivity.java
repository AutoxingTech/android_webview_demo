package com.autoxing.delivery;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;


public class MainActivity extends AppCompatActivity {

    private static String TAG = "MainActivity";

    private WebView webView; //网页控件
    private View launcherView; //启动图

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT>=23&&checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        }
        getExternalFilesDir("").getAbsolutePath();//此行必须保留用于创建目录

        launcherView = findViewById(R.id.launch_view);
        launcherView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.this.loadWebView();
            }
        });

        webView = findViewById(R.id.web_view);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAllowContentAccess(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.setWebChromeClient(new WebChromeClient());

        webView.setWebViewClient(new WebViewClient(){
            Boolean pageFinished = false;
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                 Log.i(TAG,"onPageStarted");
                pageFinished = false;
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                Log.i(TAG,"onPageFinished"+view.getProgress());
                if(view.getProgress()==100&&pageFinished==false){
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(1000*1);
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    launcherView.setVisibility(View.GONE);
                                }
                            });
                        }
                    }).start();
                    pageFinished = true;
                }
                super.onPageFinished(view, url);
            }
        });

        //像js注入android多对象
        webView.addJavascriptInterface(MainActivity.this, "app");
        this.loadWebView();

        //android调用webview的js方法
        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                webView.loadUrl("javascript:callJSHello("+Math.random()+")");
            }
        });

        Log.i(TAG,"onCreate");
    }

    private void loadWebView() {
        launcherView.setVisibility(View.VISIBLE);

        //运行此项目 https://github.com/AutoxingTech/AX_SDK1.0_Example,
        //之后 webView.loadUrl("https://xxxxx/sdk/v1.0/example");
        //即可体验sdk功能
        //file:///android_asset/dist/index.html 为android与js交互demo
        webView.loadUrl("file:///android_asset/dist/index.html");
    }

    @android.webkit.JavascriptInterface
    public String actionFromJsHello(String args) {
        return args+Math.random()+"android_返回结果";
    }

    @android.webkit.JavascriptInterface
    public void actionFromJsWebRefresh() {
        Log.i(TAG,"刷新网页");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MainActivity.this.loadWebView();
            }
        });
    }

}