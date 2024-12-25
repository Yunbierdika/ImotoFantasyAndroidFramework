package game.imotofantasy;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
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
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;

import game.imotofantasy.utils.MakeAlertDialog;
import game.imotofantasy.utils.SaveFileDeleter;
import game.imotofantasy.utils.SaveFileExporter;
import game.imotofantasy.utils.SaveFileImporter;

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

        Context context = getBaseContext();

        // 获取私有目录中的存档文件夹：/Android/data/game.imotofantasy/file/leveldb
        File targetSaveDir = new File(context.getExternalFilesDir(null), "leveldb");

        // 判断游戏私有目录（Android/data/game.imotofantasy）是否存在，是否可创建文件夹
        if (!targetSaveDir.exists() && !targetSaveDir.mkdirs()) {
            System.err.println("Failed to create save directory: " + targetSaveDir.getAbsolutePath());
            return;
        }

        // 导出位置：Android/data/game.imotofantasy/file/leveldb/
        Button saveFileExportBtn = findViewById(R.id.export_save_btn);
        saveFileExportBtn.setOnClickListener(v -> MakeAlertDialog.show(MainActivity.this, "导出存档",
                "存档将导出到：'Android/data/game.imotofantasy/file/'目录下",
                "取消操作", (dialog, i) -> dialog.dismiss(),
                "确定导出存档", (dialogInterface, i) -> SaveFileExporter.exportSaveFiles(context)
        ));

        // 手动导入存档文件夹
        Button saveFileImportBtn = findViewById(R.id.import_save_btn);
        saveFileImportBtn.setOnClickListener(v -> MakeAlertDialog.show(MainActivity.this, "导入存档", "此操作会删除当前游戏存档并加载选择的存档，是否确定？导入完毕后需重启游戏。",
                "取消操作", (dialogInterface, i) -> dialogInterface.dismiss(),
                "确定并选择需要导入的存档", (dialogInterface, i) -> SaveFileImporter.startFolderPicker(this)
        ));

        // 删除游戏私有目录内（/data/data/game.imotofantasy/）的存档
        Button saveFileDeleterBtn = findViewById(R.id.delete_save_btn);
        saveFileDeleterBtn.setOnClickListener(v -> MakeAlertDialog.show(MainActivity.this, "删除存档", "此操作会删除当前游戏存档，是否确定？删除完毕后需重启游戏。",
                "取消操作", (dialogInterface, i) -> dialogInterface.dismiss(),
                "确定删除存档", (dialogInterface, i) -> SaveFileDeleter.deleteSaveFiles(context)
        ));

        // 关于信息按钮
        Button showInfoBtn = findViewById(R.id.show_info_btn);
        showInfoBtn.setOnClickListener(v -> MakeAlertDialog.show(MainActivity.this, "提示信息",
                "游戏作者：いぬすく\n" +
                        "游戏移植：HBOX.JP\n" +
                        "本项目仅供学习交流严禁用于商业用途。\n" +
                        "本项目完全免费，喜欢请购买正版！",
                "好的", (dialogInterface, i) -> dialogInterface.dismiss(),
                "明白", (dialogInterface, i) -> dialogInterface.dismiss()
        ));
    }

    // 用于接收导入存档时选择的文件夹事件结果
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 调用导入存档方法
        SaveFileImporter.handleFolderSelection(this, requestCode, resultCode, data);
    }
}