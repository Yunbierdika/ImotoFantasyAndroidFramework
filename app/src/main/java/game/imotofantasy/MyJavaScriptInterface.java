package game.imotofantasy;

import android.app.Activity;
import android.util.Log;
import android.webkit.JavascriptInterface;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import game.imotofantasy.utils.LZString;
import game.imotofantasy.utils.WriteLogToLocal;

public class MyJavaScriptInterface {
    private final Activity activity;

    // 日志输出实例
    private final WriteLogToLocal writeLogToLocal;

    // 缓存存档数据
    private final HashMap<String, String> cache = new HashMap<>();

    public MyJavaScriptInterface(Activity activity, WriteLogToLocal writeLogToLocal) {
        this.activity = activity;
        this.writeLogToLocal = writeLogToLocal;
    }

    // 获取存档目录
    private File getSaveDir() {
        File saveDir = new File(activity.getExternalFilesDir(null), "save");
        if (!saveDir.exists() && !saveDir.mkdirs()) {
            writeLogToLocal.logError("Failed to create save directory: " + saveDir.getAbsolutePath());
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
        // 调用 Activity 的 finish() 方法
        activity.runOnUiThread(activity::finish);
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
            writeLogToLocal.logError("Failed to save game data：" + e.getMessage(), e);
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
            writeLogToLocal.logError("Failed to load game data" + e.getMessage(), e);
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
            writeLogToLocal.logError("Failed to delete common save: " + saveFile.getAbsolutePath());
        }
    }
}
