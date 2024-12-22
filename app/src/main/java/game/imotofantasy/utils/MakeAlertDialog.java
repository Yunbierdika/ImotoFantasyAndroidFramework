package game.imotofantasy.utils;

import android.app.Activity;
import android.content.DialogInterface;

import androidx.appcompat.app.AlertDialog;

public class MakeAlertDialog {
    public static void show(Activity activity, String title, String message, String negText, DialogInterface.OnClickListener negativeCallback, String posText, DialogInterface.OnClickListener positiveCallback) {
        //弹出基本对话框
        AlertDialog dialog = new AlertDialog.Builder(activity).create();
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, negText, negativeCallback);
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, posText, positiveCallback);
        dialog.show();
    }
}
