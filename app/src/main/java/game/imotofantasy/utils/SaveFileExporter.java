package game.imotofantasy.utils;

import android.content.Context;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

public class SaveFileExporter {
    public static void exportSaveFiles(Context context) {
        // 游戏 WebView 存档文件夹：/data/data/game.imotofantasy/app_webview/Default/Local Storage/leveldb
        String webViewSavePath = "/data/data/" + context.getPackageName() + "/app_webview/Default/Local Storage/leveldb/";

        // 获取私有目录中的存档文件夹：/Android/data/game.imotofantasy/file/leveldb
        File targetSaveDir = new File(context.getExternalFilesDir(null), "leveldb");

        // 判断游戏私有目录（Android/data/game.imotofantasy）是否存在，是否可创建文件夹
        if (!targetSaveDir.exists() && !targetSaveDir.mkdirs()) {
            System.err.println("Failed to create save directory: " + targetSaveDir.getAbsolutePath());
            return;
        }

        File sourceDir = new File(webViewSavePath);
        if (sourceDir.exists() && sourceDir.isDirectory()) {
            // 把游戏 WebView 存档文件夹内的存档文件导出
            for (File file : Objects.requireNonNull(sourceDir.listFiles())) {
                if (file.isFile()) {
                    try {
                        File targetFile = new File(targetSaveDir, file.getName());
                        copyFile(file, targetFile);
                        System.out.println("File exported: " + targetFile.getAbsolutePath());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            Toast.makeText(context, "存档文件已导出到'Android/data/game.imotofantasy/file/leveldb'目录下。", Toast.LENGTH_LONG).show();
        }
    }

    private static void copyFile(File source, File target) throws IOException {
        try (FileInputStream fis = new FileInputStream(source);
             FileOutputStream fos = new FileOutputStream(target)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }
        }
    }
}
