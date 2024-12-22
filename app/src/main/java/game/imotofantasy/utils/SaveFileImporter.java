package game.imotofantasy.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

public class SaveFileImporter {
    private static final int REQUEST_CODE_PICK_FOLDER = 1001;

    // Step 1: 开启文件夹选择器
    public static void startFolderPicker(Activity activity) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        activity.startActivityForResult(intent, REQUEST_CODE_PICK_FOLDER);
    }

    // Step 2: 处理被选中的文件夹
    public static void handleFolderSelection(Context context, int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_PICK_FOLDER && resultCode == Activity.RESULT_OK) {
            Uri folderUri = data.getData();
            if (folderUri != null) {
                // Grant permissions
                context.getContentResolver().takePersistableUriPermission(
                        folderUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                );

                // 调用 Step 3 导入存档
                importSaveFiles(context, folderUri);
            } else {
                Toast.makeText(context, "未选择存档目录！", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Step 3: 从选择的文件夹中导入文件
    private static void importSaveFiles(Context context, Uri folderUri) {
        try {
            // 获取游戏的 WebView 存档目录
            String webViewSavePath = "/data/data/" + context.getPackageName() + "/app_webview/Default/Local Storage/leveldb/";
            File targetDir = new File(webViewSavePath);

            // Step 1: 删除已存在的 leveldb 文件夹
            if (targetDir.exists()) {
                deleteDirectory(targetDir);
            }

            if (!targetDir.mkdirs()) {
                Toast.makeText(context, "重建 leveldb 文件夹失败！", Toast.LENGTH_SHORT).show();
                return;
            }

            // Step 2: Query the folder's files
            Uri childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(
                    folderUri, DocumentsContract.getTreeDocumentId(folderUri));
            Cursor cursor = context.getContentResolver().query(childrenUri, new String[]{
                    DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                    DocumentsContract.Document.COLUMN_DISPLAY_NAME
            }, null, null, null);

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String documentId = cursor.getString(0);
                    String displayName = cursor.getString(1);

                    Uri fileUri = DocumentsContract.buildDocumentUriUsingTree(folderUri, documentId);

                    // Step 3: Copy file to target directory
                    File targetFile = new File(targetDir, displayName);
                    try (InputStream inputStream = context.getContentResolver().openInputStream(fileUri);
                         OutputStream outputStream = new FileOutputStream(targetFile)) {
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = inputStream.read(buffer)) > 0) {
                            outputStream.write(buffer, 0, length);
                        }
                    }
                }
                cursor.close();
            }
            Toast.makeText(context, "存档导入成功！需要重启游戏。", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "导入存档失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // Helper method to delete a directory and its contents
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
