package com.ma.powersoundswitch.fragment;


import static android.content.Context.MODE_PRIVATE;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.PathUtils;
import com.blankj.utilcode.util.RomUtils;
import com.blankj.utilcode.util.ShellUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.ma.powersoundswitch.R;
import com.microsoft.appcenter.AppCenter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class SettingFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceClickListener,Preference.OnPreferenceChangeListener {

    // TODO: Rename parameter arguments, choose names that match

    private mViewModel viewModel;

    private ContentResolver cr;

    private Preference about,AppCanter,开源,电源音,低电量音,自定义低电量音路径,锁屏音,锁屏音路径,解锁音,解锁音路径;

    private SharedPreferences sp;
    private SharedPreferences.Editor editor;

    public SettingFragment() {

    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(mViewModel.class); // 实例化发送端

        about = findPreference("about");
        AppCanter =findPreference("appcanter");
        开源 = findPreference("opensource");
        电源音 =findPreference("power_sound");
        低电量音 = findPreference("low_battery_sound");
        自定义低电量音路径 = findPreference("low_battery_sound_path");
        锁屏音 = findPreference("lock_sound");
        锁屏音路径 = findPreference("lock_sound_path");
        解锁音 = findPreference("unlock_sound");
        解锁音路径 = findPreference("unlock_sound_path");


        about.setOnPreferenceClickListener(this);
        AppCanter.setOnPreferenceChangeListener(this);
        开源.setOnPreferenceClickListener(this);
        电源音.setOnPreferenceChangeListener(this);
        低电量音.setOnPreferenceChangeListener(this);
        自定义低电量音路径.setOnPreferenceClickListener(this);
        锁屏音.setOnPreferenceChangeListener(this);
        锁屏音路径.setOnPreferenceClickListener(this);
        解锁音.setOnPreferenceChangeListener(this);
        解锁音路径.setOnPreferenceClickListener(this);

        sp = requireContext().getSharedPreferences("status",MODE_PRIVATE);
        editor = requireContext().getSharedPreferences("status",MODE_PRIVATE).edit();

        cr = new ContentResolver(getContext()) {
            @Nullable
            @Override
            public String[] getStreamTypes(@NonNull Uri url, @NonNull String mimeTypeFilter) {
                return super.getStreamTypes(url, mimeTypeFilter);
            }
        };

        String cmd = "export PATH="+PathUtils.getInternalAppDataPath()+ "/files:"+"$PATH"+" " +
                " && echo PATH环境变量: $PATH"+
                " && cd "+PathUtils.getInternalAppDataPath()+ "/files"+
                " && pwd && ls -l ";

        if (!FileUtils.isFileExists(PathUtils.getInternalAppDataPath()+"/files/rish")){
            initRish(cmd);
            LogUtils.i("File Not Fount: "+PathUtils.getInternalAppDataPath()+"/files/rish"+"\nBut We Are fixed it");
        }else {
            checkPermissionStatus(Manifest.permission.WRITE_SECURE_SETTINGS);
            //LogUtils.e("sh "+PathUtils.getInternalAppDataPath()+"/files/rish -c "+"\"pm grant com.ma.powersoundswitch "+Manifest.permission.WRITE_SECURE_SETTINGS+ "\" &",false) ;
        }

        saveConfigInfo(); //初始化系统默认值

    }

    private void initRish(String cmd) {
        try {
            InputStream is = requireContext().getAssets().open("rish");
            inputStream2File(is,new File(PathUtils.getInternalAppDataPath()+"/files/rish"));
            InputStream is2 = requireContext().getAssets().open("rish_shizuku.dex");
            inputStream2File(is2,new File(PathUtils.getInternalAppDataPath()+"/files/rish_shizuku.dex"));
        }catch (Exception e){
            LogUtils.e(e.fillInStackTrace());
        }

        ShellUtils.execCmd("/system/bin/chmod 777 "+PathUtils.getInternalAppDataPath()+"/files/rish",false);
        //Ops检查权限状态(AppOpsManager.permissionToOp(Manifest.permission.WRITE_SECURE_SETTINGS));
        checkPermissionStatus(Manifest.permission.WRITE_SECURE_SETTINGS);
    }


    /**
     * 将inputStream转化为file
     * @param is
     * @param file 要输出的文件目录
     */
    public static void inputStream2File (InputStream is, File file) throws IOException {
        OutputStream os = null;
        try {
            os = new FileOutputStream(file);
            int len = 0;
            byte[] buffer = new byte[8192];

            while ((len = is.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
        } finally {
            os.close();
            is.close();
        }
    }

    private int checkPermissionStatus(String permission) //调用ops权限管理器校验权限是否真的授权
    {
        if (ContextCompat.checkSelfPermission(requireContext(),permission) != 0) {
            LogUtils.e(permission+" 未授权");
            ShellUtils.execCmd("sh "+PathUtils.getInternalAppDataPath()+"/files/rish -c "+"\"pm grant com.ma.powersoundswitch "+Manifest.permission.WRITE_SECURE_SETTINGS+ "\" &",false);
        }else {
            LogUtils.i("已备份默认数据"+sp.getAll());
        }
        return ContextCompat.checkSelfPermission(requireContext(),permission);
    }


    private void saveConfigInfo(){
        switch (RomUtils.getRomInfo().getName()){
            case "xiaomi":
                editor.putString("low_battery_sound_path","/system/media/audio/ui/LowBattery.ogg").commit();
                editor.putString("lock_sound_path","/system/media/audio/ui/Lock.ogg").commit();
                editor.putString("unlock_sound_path","/system/media/audio/ui/Unlock.ogg").commit();
                LogUtils.i("已适配"+RomUtils.getRomInfo().getName());
                break;
            default:
                ToastUtils.showShort("抱歉，本应用仅适配了"+RomUtils.getRomInfo().getName()+"\n\n可联系开发者申请适配");
                LogUtils.e("当前设备未适配："+RomUtils.getRomInfo().getName());
                低电量音.setEnabled(false);
                自定义低电量音路径.setEnabled(false);
                锁屏音.setEnabled(false);
                锁屏音路径.setEnabled(false);
                解锁音.setEnabled(false);
                解锁音路径.setEnabled(false);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences_settings);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        switch (preference.getKey()){
            case "low_battery_sound_path":
                ToastUtils.showShort("请选择ogg格式文件");
                startActivityForResult(new Intent(Intent.ACTION_GET_CONTENT).setType("audio/ogg").addCategory(Intent.CATEGORY_OPENABLE), 66);
                viewModel.getCallString().observe(this, s -> {
                    自定义低电量音路径.setSummary(s);
                    //p2.setDefaultValue("true");
                    Settings.Global.putString(cr, "low_battery_sound",s);
                    ToastUtils.showShort(getString(R.string.low_battery_sound) + "已设置为\n" + s);
                });
                break;
            case "lock_sound_path":
                ToastUtils.showShort("请选择ogg格式文件");
                startActivityForResult(new Intent(Intent.ACTION_GET_CONTENT).setType("audio/ogg").addCategory(Intent.CATEGORY_OPENABLE), 666);
                viewModel.getCallString().observe(this, s -> {
                    锁屏音路径.setSummary(s);
                   // p7.setDefaultValue("true");
                    Settings.Global.putString(cr, "lock_sound",s);
                    ToastUtils.showShort(getString(R.string.lock_sound) + "已设置为\n" + s);
                });
                break;
            case "unlock_sound_path":
                ToastUtils.showShort("请选择ogg格式文件");
                startActivityForResult(new Intent(Intent.ACTION_GET_CONTENT).setType("audio/ogg").addCategory(Intent.CATEGORY_OPENABLE), 6666);
                viewModel.getCallString().observe(this, s -> {
                    解锁音路径.setSummary(s);
                    //p9.setDefaultValue("true");
                    Settings.Global.putString(cr, "unlock_sound",s);
                    ToastUtils.showShort(getString(R.string.unlock_sound) + "已设置为\n" + s);
                });
                break;
            case "opensource":
                openCustomTabs("https://github.com/sbmatch/powersoundswitch");
                break;
            case "about":
                //ToastUtils.showShort(AppUtils.getAppVersionName());
                new AlertDialog.Builder(requireContext())
                        .setCancelable(false)
                        .setIcon(R.drawable.ic_launcher_foreground)
                        .setTitle(R.string.about)
                        .setMessage(R.string.at)
                        .setPositiveButton(R.string.lab_submit, (dialog, which) -> { })
                        //.setNegativeButton(R.string.lab_cancel, (dialog, which) -> { LogUtils.i(dialog.toString()); })
                        .show();
                break;
        }
        return true;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {

        if (checkPermissionStatus(Manifest.permission.WRITE_SECURE_SETTINGS) == 0)
        {
            try {
                switch (preference.getKey()) {
                    case "power_sound":
                            if (((boolean) newValue)) {
                                LogUtils.i("已开启");
                                Settings.Global.putInt(cr, "power_sounds_enabled", 1);
                                ToastUtils.showShort("已开启");
                                //ShellUtils.execCmd("sh "+PathUtils.getInternalAppDataPath()+"/files/rish -c "+"\"settings put global power_sounds_enabled 1\" &",false);
                            } else {
                                ToastUtils.showShort("已禁用" + getString(R.string.power_sound));
                                //LogUtils.e(newValue);
                                Settings.Global.putInt(cr, "power_sounds_enabled", 0);
                                //ShellUtils.execCmd("sh "+PathUtils.getInternalAppDataPath()+"/files/rish -c "+"\"settings put global power_sounds_enabled 0\" &",false);
                            }
                        LogUtils.e("电源音状态：" + Settings.Global.getString(cr, "power_sounds_enabled"));
                        break;
                    case "low_battery_sound":
                        LogUtils.i(preference.getKey()+" is "+newValue);
                        if (((boolean) newValue)) {
                            ToastUtils.showShort(getString(R.string.low_battery_sound) + "已设置为\n" + Settings.Global.getString(cr, "low_battery_sound"));
                            /*viewModel.getCallString().observe(this, s -> {
                                Settings.Global.putString(cr, "low_battery_sound", s);
                                p2.setSummary(s);
                                ToastUtils.showShort(getString(R.string.low_battery_sound) + "已设置为\n" + s);
                            });*/
                            自定义低电量音路径.setSummary(Settings.Global.getString(cr, "low_battery_sound"));

                        }else {
                            Settings.Global.putString(cr, "low_battery_sound",sp.getString("low_battery_sound_path",""));
                            自定义低电量音路径.setSummary("当前是系统默认值");
                            ToastUtils.showShort("已恢复为默认值");
                        }
                        break;
                    case "lock_sound":
                        LogUtils.i(preference.getKey()+" is "+newValue);
                        if (((boolean) newValue)) {

                            ToastUtils.showShort(getString(R.string.lock_sound) + "已设置为\n" + Settings.Global.getString(cr, "lock_sound"));
                            锁屏音路径.setSummary(Settings.Global.getString(cr, "lock_sound"));

                        }else {
                            Settings.Global.putString(cr, "lock_sound",sp.getString("lock_sound_path",""));
                            锁屏音路径.setSummary("当前是系统默认值");
                            ToastUtils.showShort("已恢复为默认值");
                            LogUtils.i("已恢复为默认值"+sp.getString("lock_sound_path",""));
                        }
                        break;
                    case "unlock_sound":
                        LogUtils.i(preference.getKey()+" is "+newValue);
                        if (((boolean) newValue)) {

                            ToastUtils.showShort(getString(R.string.lock_sound) + "已设置为\n" + Settings.Global.getString(cr, "unlock_sound"));
                            解锁音路径.setSummary(Settings.Global.getString(cr, "unlock_sound"));

                            /*viewModel.getCallString().observe(this, s -> {
                                Settings.Global.putString(cr, "unlock_sound", s);
                                p9.setSummary(s);
                                ToastUtils.showShort(getString(R.string.low_battery_sound) + "已设置为\n" + s);
                            });*/

                        }else {
                            Settings.Global.putString(cr, "unlock_sound",sp.getString("unlock_sound_path",""));
                            解锁音路径.setSummary("当前是系统默认值");
                            ToastUtils.showShort("已恢复为默认值");
                            LogUtils.i("已恢复为默认值"+sp.getString("unlock_sound",""));
                        }
                        break;
                    case "appcanter":
                        if (((boolean) newValue)) {
                            AppCenter.setEnabled(true);
                        } else {
                            AppCenter.setEnabled(false);
                        }
                        break;
                }

            } catch (Exception e) {
                //e.fillInStackTrace();
                LogUtils.e(e.fillInStackTrace());
                ToastUtils.showShort(e.fillInStackTrace().toString());
                //ActivityUtils.finishActivity(requireActivity());
            }
        }else {
            ToastUtils.showShort("未授权");
        }
        return true;
    }

    private void openCustomTabs(String url) {
        CustomTabsIntent.Builder intentBuilder = new CustomTabsIntent.Builder();
        intentBuilder.setShowTitle(true);
        CustomTabsIntent customTabsIntent = intentBuilder.build();
        customTabsIntent.launchUrl(requireContext(), Uri.parse(url));
    }

}