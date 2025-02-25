package game.imotofantasy.utils;

import android.app.Activity;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

public class WriteLogToLocal {

    private static final String TAG = "WebView";

    private final Activity activity;

    public WriteLogToLocal(Activity activity) {
        this.activity = activity;
    }

    // 输出无跟踪错误日志
    public void logError(String message) {
        logError(message, null);
    }

    // 输出有跟踪错误日志
    public void logError(String message, Throwable e) {
        Log.e(TAG, message, e);
        writeToLogFile(message, e);
    }

    // 将错误日志保存到指定目录
    public void writeToLogFile(String message, Throwable e) {
        // 日志目录：/Android/data/包名/file
        File logDir = activity.getExternalFilesDir(null); // 获取外部存储的files目录
        if (logDir == null) {
            Log.e(TAG, "无法访问外部存储目录");
            return;
        }

        // 错误日志文件
        File logFile = new File(logDir, "logs.txt");

        try (FileOutputStream fos = new FileOutputStream(logFile, true); // 追加模式
             OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
             BufferedWriter writer = new BufferedWriter(osw)
        ) {
            // 构建日志条目
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            String logEntry = String.format("[%s] ERROR: %s", timestamp, message);

            writer.write(logEntry);
            writer.newLine();

            if (e != null) {
                writer.write(Log.getStackTraceString(e));
                writer.newLine();
            }
        } catch (IOException ex) {
            Log.e(TAG, "写入日志文件失败", ex);
        }
    }
}
