package com.ma.powersoundswitch;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.app.Dialog;
import android.app.Service;
import android.app.StatusBarManager;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;


import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;
import android.provider.Settings;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.ColorUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.PathUtils;
import com.blankj.utilcode.util.PermissionUtils;
import com.blankj.utilcode.util.RomUtils;
import com.blankj.utilcode.util.ShellUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.blankj.utilcode.util.Utils;
import com.huawei.hms.adapter.sysobs.SystemManager;
import com.ma.powersoundswitch.activity.SettingActivity;
import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.crashes.Crashes;

import java.io.FileDescriptor;
import java.lang.reflect.Method;

import rikka.shizuku.Shizuku;
import rikka.shizuku.ShizukuBinderWrapper;
import rikka.shizuku.SystemServiceHelper;

public class MainActivity extends AppCompatActivity implements Shizuku.OnRequestPermissionResultListener {

    private Intent intent ;
    private Dialog dialog;
    private static final int REQUEST_CODE = 1000;
    private static Bundle outState;
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    private mViewModel viewModel;
    private ImageView imageView ,imageView2;
    private Button button;
    private CardView cardView,cardView2;
    private TextView textview,textview2,ttextView,ttextView2;
    private  IBinder iBinder ;
    private final Shizuku.OnRequestPermissionResultListener REQUEST_PERMISSION_RESULT_LISTENER = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCenter.start(getApplication(), "b5f71581-37c7-42a2-b631-45a8a56a17df", Analytics.class, Crashes.class);
        Utils.init(getApplication());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        intent = new Intent(this, SettingActivity.class);
        cardView = findViewById(R.id.cardview);
        imageView = findViewById(R.id.imageview);
        textview= findViewById(R.id.tv);
        textview2 = findViewById(R.id.tv2);

        sp = getSharedPreferences("StartSate",MODE_PRIVATE);
        editor = getSharedPreferences("StartSate",MODE_PRIVATE).edit();

        viewModel = new ViewModelProvider(this).get(mViewModel.class);


        Shizuku.addRequestPermissionResultListener(REQUEST_PERMISSION_RESULT_LISTENER);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        checkShizukuStat();

        //checkPermissionStatus(Manifest.permission.WRITE_SECURE_SETTINGS);

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

    private int checkPermissionStatus(String permission) //调用权限管理器校验权限是否真的授权
    {
        if (ContextCompat.checkSelfPermission(getBaseContext(),permission) != 0) {
            LogUtils.e(permission+" 未授权");
            checkShizukuStat();
        }else {
            LogUtils.i("已获授权: "+permission);
            editor.putBoolean("start", true).commit();
            startActivity(intent);
        }
        return ContextCompat.checkSelfPermission(getBaseContext(),permission);
    }


    private void checkShizukuStat() {
        try {
            if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
                // Granted
                LogUtils.i("已授权shizuku");
                ShellUtils.execCmd("sh "+ PathUtils.getInternalAppDataPath()+"/files/rish -c "+"\"pm grant com.ma.powersoundswitch "+ Manifest.permission.WRITE_SECURE_SETTINGS+ "\" &",false);
                textview.setText("已授权Shizuku");
                imageView.setImageResource(R.drawable.ic_baseline_emoji_emotions_24);
                textview2.setVisibility(View.GONE);
                startActivity(intent);
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
                    checkShizukuStat();
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
       // getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.add(0,1,0,"授权").setIcon(R.drawable.ic_baseline_settings_24);
        return true;
    }


    @Override
    public boolean onMenuOpened(int featureId, @NonNull Menu menu) {

        if (menu.getClass().getSimpleName().equalsIgnoreCase("MenuBuilder")) {
            try {
                Method method = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
                method.setAccessible(true);
                method.invoke(menu, true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return super.onMenuOpened(featureId, menu);
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
        if (id == 1) {
            //ActivityUtils.startActivity(intent);
            checkShizukuStat();
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
                    case "android:write_secure_settings":
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
            textview.setText("已授权");
            imageView.setImageResource(R.drawable.ic_twotone_done_24);
            textview2.setVisibility(View.GONE);
            checkPermissionStatus(Manifest.permission.WRITE_SECURE_SETTINGS);
        }else {

            try {
                new AlertDialog.Builder(this)
                        .setCancelable(false)
                        .setTitle("权限申请")
                        .setMessage("因本应用仅适配Shizuku \n\n如需继续使用请授权，否则程序无法工作\n\n您想再次授权吗？")
                        .setPositiveButton("授权", (dialog, which) -> {
                            Shizuku.requestPermission(REQUEST_CODE);
                        })
                        .setNegativeButton(R.string.lab_cancel, (dialog, which) -> {
                            viewModel.add("未授权");
                            ToastUtils.showShort("好的，我们尊重您的决定");
                        }).show();

            }catch (Exception e2){
                LogUtils.e(e2.fillInStackTrace());
            }

        }
    }
}
