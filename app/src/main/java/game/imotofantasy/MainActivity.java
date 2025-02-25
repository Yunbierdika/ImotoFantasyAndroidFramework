package game.imotofantasy;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;

public class MainActivity extends AppCompatActivity {

    private WebView gameWebview;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 动态设置屏幕方向为横屏
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);

        // 保持屏幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // 获取布局文件中的WebView控件
        gameWebview = findViewById(R.id.game_webview);

        // 去除系统UI对游戏界面的影响
        ViewCompat.setOnApplyWindowInsetsListener(gameWebview, (v, insets) -> {
            v.setPadding(0, 0, 0, 0);// 确保不为导航栏留空
            return insets;
        });

        // 创建 OnBackPressedCallback，拦截返回键误操作
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // 弹出确认框
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("提示")
                        .setMessage("您确定要退出游戏吗？未保存的存档将会丢失。")
                        .setPositiveButton("确定", (dialog, which) -> finish())
                        .setNegativeButton("取消", null)
                        .show();
            }
        };

        // 将 callback 添加到 OnBackPressedDispatcher
        getOnBackPressedDispatcher().addCallback(this, callback);

        // 仅在savedInstanceState为空时加载初始URL
        if (savedInstanceState == null) {

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
            // 设置缓存模式为本地缓存模式
            webSettings.setCacheMode(WebSettings.LOAD_CACHE_ONLY);
            // 启用WebView的数据库存储功能，允许WebView使用本地数据库存储数据
            webSettings.setDatabaseEnabled(true);
            // 设置WebView自动加载图片，即使页面中有图片资源，WebView也会自动加载并显示
            webSettings.setLoadsImagesAutomatically(true);
            // 启用WebView的多窗口支持，允许WebView在打开新链接时支持多窗口显示
            webSettings.setSupportMultipleWindows(true);
            // 允许JavaScript自动打开新窗口，当页面中的JavaScript代码尝试打开新窗口时，WebView会自动处理
            webSettings.setJavaScriptCanOpenWindowsAutomatically(true);

            // 设置WebView背景色默认为黑色
            gameWebview.setBackgroundColor(Color.BLACK);
            // 使用硬件加速
            gameWebview.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            // 添加JavaScript接口
            gameWebview.addJavascriptInterface(new MyJavaScriptInterface(this), "AndroidBridge");
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

    // 视图重新聚焦时执行全屏模式
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // Android 11+ 使用 WindowInsetsController 进入全屏
                WindowInsetsController controller = getWindow().getInsetsController();
                if (controller != null) {
                    controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                    controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
                }
            } else {
                // Android 10 及以下使用旧方法
                View decorView = getWindow().getDecorView();
                int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
                decorView.setSystemUiVisibility(flags);
            }
        }
    }

    // 避免锁屏时WebView自动刷新导致存档丢失
    @Override
    protected void onPause() {
        super.onPause();
        if (gameWebview != null) {
            gameWebview.onPause();  // 暂停 WebView
            gameWebview.pauseTimers();  // 暂停 WebView 中的定时器
        }
    }

    // 避免锁屏时WebView自动刷新导致存档丢失
    @Override
    protected void onResume() {
        super.onResume();
        if (gameWebview != null) {
            gameWebview.onResume();  // 恢复 WebView
            gameWebview.resumeTimers();  // 恢复 WebView 中的定时器
        }
    }

    @Override
    protected void onDestroy() {
        if (gameWebview != null) {
            // 停止加载内容
            gameWebview.stopLoading();
            gameWebview.loadUrl("about:blank");
            // 清理历史和资源
            gameWebview.clearHistory();
            gameWebview.removeAllViews();
            // 销毁 WebView
            gameWebview.destroy();
            gameWebview = null; // 避免内存泄漏
        }
        super.onDestroy();
    }

    // 保存 WebView 状态，应对切换应用时刷新问题
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (gameWebview != null) {
            // 保存状态
            gameWebview.saveState(outState);
        }
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (gameWebview != null) {
            // 恢复状态
            gameWebview.restoreState(savedInstanceState);
        }
    }
}