package game.imotofantasy;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 动态设置屏幕方向为横屏
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);

        // 设置全屏模式
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // 获取布局文件中的WebView控件
        WebView gameWebview = findViewById(R.id.game_webview);

        // 去除系统UI对游戏界面的影响
        ViewCompat.setOnApplyWindowInsetsListener(gameWebview, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 设置加载的本地HTML文件的URL
        final String file_url = "file:///android_asset/index.html";

        WebSettings webSettings = gameWebview.getSettings();

        // 启用WebView中的JavaScript支持
        webSettings.setJavaScriptEnabled(true);
        // 启用DOM存储支持
        webSettings.setDomStorageEnabled(true);
        // 允许媒体内容在没有用户手势的情况下自动播放
        webSettings.setMediaPlaybackRequiresUserGesture(false);
        // 允许WebView适应屏幕宽度（启用视口支持）
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        // 允许WebView访问本地文件
        webSettings.setAllowFileAccess(true);
        // 允许从本地文件的URL中访问其他文件
        webSettings.setAllowFileAccessFromFileURLs(true);
        // 允许从文件URL访问跨域资源
        webSettings.setAllowUniversalAccessFromFileURLs(true);

        // 使用硬件加速
        gameWebview.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        // 清除WebView的缓存
        gameWebview.clearCache(true);
        // 加载指定的本地HTML文件
        gameWebview.loadUrl(file_url);
        // 防止外部浏览器打开链接
        gameWebview.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                // 页面加载完成时的回调
                Log.d("WebView", "Page loaded: " + url);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                // 捕捉加载错误
                Log.e("WebView", "Error: " + error.getDescription());
            }
        });
    }
}