package game.imotofantasy.utils;

import android.content.Context;
import android.widget.Toast;

import java.io.File;
import java.util.Objects;

public class SaveFileDeleter {
    public static void deleteSaveFiles(Context context) {
        // 游戏 WebView 存档文件夹：/data/data/game.imotofantasy/app_webview/Default/Local Storage/leveldb
        String webViewSavePath = "/data/data/" + context.getPackageName() + "/app_webview/Default/Local Storage/leveldb/";
        File targetDir = new File(webViewSavePath);

        // 判断文件夹是否存在，存在则删除
        if (targetDir.exists()) {
            deleteDirectory(targetDir);
            Toast.makeText(context, "已成功将游戏存档删除。", Toast.LENGTH_LONG).show();
        }
    }

    private static boolean deleteDirectory(File directory) {
        if (directory.isDirectory()) {
            for (File child : Objects.requireNonNull(directory.listFiles())) {
                if (!deleteDirectory(child)) {
                    return false;
                }
            }
        }
        return directory.delete();
    }
}