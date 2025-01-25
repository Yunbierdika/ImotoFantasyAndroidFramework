package game.imotofantasy;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import game.imotofantasy.utils.LZString;

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

            // 使用硬件加速
            gameWebview.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            // 添加JavaScript接口
            gameWebview.addJavascriptInterface(new WebAppInterface(this), "AndroidBridge");

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

    // 缓存存档数据
    private final HashMap<String, String> cache = new HashMap<>();

    // 安卓和JavaScript接口的通信事件
    public class WebAppInterface {

        private final Activity activity;

        public WebAppInterface(Activity activity) {
            this.activity = activity;
        }

        // 获取存档目录
        private File getSaveDir() {
            File saveDir = new File(getExternalFilesDir(null), "save");
            if (!saveDir.exists() && !saveDir.mkdirs()) {
                Log.e("WebView", "Failed to create save directory: " + saveDir.getAbsolutePath());
            }
            return saveDir;
        }

        // 测试时打日志使用
        @JavascriptInterface
        public void logEvent(String message) {
            Log.d("WebView", "Event from WebView: " + message);
        }

        // 使结束游戏按钮功能可用
        @JavascriptInterface
        public void closeGame() {
            activity.runOnUiThread(() -> {
                activity.finish(); // 调用 Activity 的 finish() 方法
            });
        }

        // 将发送过来的存档保存到指定目录
        @JavascriptInterface
        public void saveGameData(String saveData, String fileName) {
            // 如果缓存中有旧存档数据则删除
            if (cache.containsKey(fileName)) {
                //Log.d("WebView", "Cache found old save file: " + fileName);
                cache.remove(fileName);
                cache.put(fileName, saveData);
            }

            // 目录：Android/data/包名/file/save
            File saveDir = getSaveDir();
            File saveFile = new File(saveDir, fileName);

            // 使用 LZString 加密压缩
            String compressed = LZString.compressToBase64(saveData);

            // 将存档数据写入文件
            try (FileOutputStream fos = new FileOutputStream(saveFile)) {
                fos.write(compressed.getBytes());
            } catch (IOException e) {
                 Log.e("WebView", "Failed to save game data", e);
            }
        }

        // 加载存档，返回值为存档文件里的内容
        @JavascriptInterface
        public String loadGameData(String fileName) {
            // 如果缓存中已有数据，直接返回
            if (cache.containsKey(fileName)) {
                //Log.d("WebView", "Cache hit for file: " + fileName);
                return cache.get(fileName);
            }

            // 文件读取和解码
            File saveDir = getSaveDir();
            File saveFile = new File(saveDir, fileName);
            // 判断文件是否存在
            if (!saveFile.exists()) return null;
            StringBuilder stringBuilder = new StringBuilder();

            try (FileInputStream fis = new FileInputStream(saveFile);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(fis, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }

                // 读取的 Base64 数据
                String base64Data = stringBuilder.toString().trim();

                // 使用 LZString 解码
                String decodedData = LZString.decompressFromBase64(base64Data);

                // 将解码后的数据存入缓存
                cache.put(fileName, decodedData);

                //Log.d("WebView", "Loaded and cached file: " + fileName);
                return decodedData;
            } catch (IOException e) {
                Log.e("WebView", "Failed to load game data", e);
                return null;
            }
        }

        // 判断存档文件是否存在
        @JavascriptInterface
        public boolean existsGameSave(String fileName) {
            File saveDir = getSaveDir();
            File saveFile = new File(saveDir, fileName);
            return saveFile.exists();
        }

        // 删除CommonSave插件的专用存档（用于跨周目继承点数的存档）
        @JavascriptInterface
        public void removeCommonSave() {
            File targetDir = getSaveDir();
            File saveFile = new File(targetDir, "common.rpgsave");

            if (saveFile.exists() && !saveFile.delete()) {
                Log.e("WebView", "Failed to delete common save: " + saveFile.getAbsolutePath());
            }
        }
    }

    // 视图重新聚焦时执行全屏模式
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                // 隐藏顶部状态栏、底部导航栏
                controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                // 设置系统栏的行为，通过滑动手势临时显示系统栏
                controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
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