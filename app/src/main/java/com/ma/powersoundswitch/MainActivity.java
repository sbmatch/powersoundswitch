package com.ma.powersoundswitch;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;


import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.PermissionUtils;
import com.blankj.utilcode.util.RomUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.blankj.utilcode.util.Utils;
import com.ma.powersoundswitch.activity.SettingActivity;

import rikka.shizuku.Shizuku;

public class MainActivity extends AppCompatActivity implements Shizuku.OnRequestPermissionResultListener {

    private Intent intent ;
    private Dialog dialog;
    private static final int REQUEST_CODE = 1000;
    private static Bundle outState;
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    private final Shizuku.OnRequestPermissionResultListener REQUEST_PERMISSION_RESULT_LISTENER = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Utils.init(getApplication());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        intent = new Intent(this, SettingActivity.class);
        //BarUtils.setStatusBarColor(this, Color.TRANSPARENT);
        //getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        sp = getSharedPreferences("StartSate",MODE_PRIVATE);
        editor = getSharedPreferences("StartSate",MODE_PRIVATE).edit();

        Shizuku.addRequestPermissionResultListener(REQUEST_PERMISSION_RESULT_LISTENER);

        CheckShizukuStat();

        /*if (sp != null) {
            if (!sp.getBoolean("start",false)) {
                ToastUtils.showShort("此应用已拒绝启动,请授权后重试");
                ActivityUtils.startHomeActivity();
                LogUtils.e("此应用已拒绝启动！");
                new Thread(() -> {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    //AppUtils.exitApp();
                    System.exit(0);
                }).start();
            }else {
                editor.putBoolean("start", true).commit();
                startActivity(intent);
            }
        }*/

    }

    private void CheckShizukuStat() {
        try {
            if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
                // Granted
                LogUtils.i("已授权");
                editor.putBoolean("start", true).commit();
                startActivity(intent);
                //startActivity(new Intent(this,SettingActivity.class));
            } else {
                // Request the permission
                new AlertDialog.Builder(this)
                        .setCancelable(false)
                        .setTitle("权限申请")
                        .setMessage("要授权 Shizuku 吗")
                        .setPositiveButton(R.string.lab_submit, (dialog, which) -> {
                            Shizuku.requestPermission(REQUEST_CODE);
                        })
                        .setNegativeButton(R.string.lab_cancel, (dialog, which) -> {
                            LogUtils.i(dialog.toString());
                        }).show();
            }

        }catch (IllegalStateException e){
            LogUtils.e(e.fillInStackTrace());
            new AlertDialog.Builder(this)
                    .setCancelable(false)
                    .setTitle("运行环境检查")
                    .setMessage("Shizuku服务未运行")
                    .setPositiveButton("启动Shizuku", (dialog, which) -> {
                        try {
                            Intent intent1 = new Intent().setComponent(new ComponentName("moe.shizuku.privileged.api","moe.shizuku.manager.MainActivity")).setAction(Intent.ACTION_VIEW);
                            this.startActivityForResult(intent1,1234);
                        }catch (NullPointerException e1){
                            LogUtils.e(e1.fillInStackTrace());
                            ToastUtils.showShort("Shizuku未安装或其他原因，未能启动");
                        }
                    })
                    .setNegativeButton(R.string.lab_cancel, (dialog, which) -> {
                        LogUtils.i(dialog.toString());
                    }).show();
            ToastUtils.showShort("Shizuku服务未运行");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (resultCode >= 0){
            switch (requestCode){
                case 1234:
                    CheckShizukuStat();
                    break;
            }
        }


        super.onActivityResult(requestCode, resultCode, data);
    }

    private void AtDialog(String title, String msg) {
        new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton(R.string.lab_submit, (dialog, which) -> {
                    //Shizuku.requestPermission(REQUEST_CODE);
                })
                .setNegativeButton(R.string.lab_cancel, (dialog, which) -> {
                    LogUtils.i(dialog.toString());
                }).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            ActivityUtils.startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void Ops检查权限状态(String a) //调用ops权限管理器校验权限是否真的授权
    {
        AppOpsManager opsMgr = (AppOpsManager) getApplicationContext().getSystemService(APP_OPS_SERVICE);;
        // 检查权限是否已授权
        switch (opsMgr.checkOp(a, android.os.Process.myUid(), getPackageName())) {
            case (AppOpsManager.MODE_ALLOWED):
                LogUtils.i("AppOps", a + "已授权");
                switch (a){
                    case "android:audio_media_volume":
                        break;
                    case AppOpsManager.OPSTR_WRITE_SETTINGS:
                        ToastUtils.showShort("已授权"+a);
                        break;
                }
                break;
            case (AppOpsManager.MODE_IGNORED):
                LogUtils.e(a + " 权限被设置为忽略\n"+"锁我喉是吧!? 搞偷袭!? 小伙子，你不讲武德\n"+ RomUtils.getRomInfo().getName()+" 是吧？ 算你狠");
            case (AppOpsManager.MODE_ERRORED):
                LogUtils.e(a + " 权限被设置为拒绝\n"+"锁我喉是吧!? 搞偷袭!? 小伙子，你不讲武德\n"+ RomUtils.getRomInfo().getName()+" 是吧？ 算你狠");
                break;
        }

    }

    private void 申请权限(String 权限) {

        PermissionUtils.permission(权限).callback(new PermissionUtils.SimpleCallback() {
            @Override
            public void onGranted() {
                LogUtils.i("成功获取权限："+权限);
                ToastUtils.showLong(权限+" 已授权");
            }
            @Override
            public void onDenied() {

            }
        }).explain((activity, denied, shouldRequest) -> {
            LogUtils.e(denied.toString());
        }).request();
    }


    @Override
    protected void onPause() {
        super.onPause();
        outState = new Bundle();
        outState.putBoolean("start", false);
        //onSaveInstanceState(outState);
        editor.putBoolean("start", false).commit();
    }

    @Override
    public void onRequestPermissionResult(int requestCode, int grantResult) {


        if (grantResult == 0){
            LogUtils.i("太好了，授权完成");
            //editor.putBoolean("start", true).commit();
            startActivity(intent);
        }else {

            try {
                new AlertDialog.Builder(this)
                        .setCancelable(false)
                        .setTitle("权限申请")
                        .setMessage("因本应用仅适配Shizuku \n\n如需继续使用请授权，否则请卸载本应用\n\n再给一次机会不咯？")
                        .setPositiveButton("授权", (dialog, which) -> {
                            Shizuku.requestPermission(REQUEST_CODE);
                        })
                        .setNegativeButton("这么嚣张？给爷死", (dialog, which) -> {
                            LogUtils.i(dialog.toString());
                            editor.putBoolean("start", false).commit();
                            AppUtils.uninstallApp(getPackageName());
                            AppUtils.exitApp();

                        }).show();
            }catch (Exception e2){
                LogUtils.e(e2.fillInStackTrace());
            }

        }
    }
}
