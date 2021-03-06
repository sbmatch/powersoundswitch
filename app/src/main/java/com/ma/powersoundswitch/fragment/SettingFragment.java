package com.ma.powersoundswitch.fragment;


import static android.content.Context.MODE_PRIVATE;
import static android.content.Intent.ACTION_SEND;
import static android.content.Intent.ACTION_SENDTO;

import android.Manifest;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.AlarmClock;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.view.Gravity;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.ActivityResultRegistry;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.FileIOUtils;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.PathUtils;
import com.blankj.utilcode.util.RomUtils;
import com.blankj.utilcode.util.ShellUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.blankj.utilcode.util.Utils;
import com.ma.powersoundswitch.R;
import com.ma.powersoundswitch.activity.ContentUriUtil;
import com.microsoft.appcenter.AppCenter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;


public class SettingFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceClickListener,Preference.OnPreferenceChangeListener {

    // TODO: Rename parameter arguments, choose names that match

    private mViewModel viewModel;

    private ContentResolver cr;

    private Preference about,AppCanter,??????,?????????,????????????,???????????????????????????,?????????,????????????????????????,?????????,????????????????????????,bugreply;

    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    private String NullOgg;

    ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
            uri -> {
                // Handle the returned Uri
                if (uri != null) {
                    ???????????????????????????.setSummary(ContentUriUtil.getPath(requireContext(), uri));
                    Settings.Global.putString(cr, "low_battery_sound",ContentUriUtil.getPath(requireContext(), uri));
                    ToastUtils.showShort(getString(R.string.low_battery_sound) + "????????????\n" + ContentUriUtil.getPath(requireContext(), uri));
                    LogUtils.i("??????Uri???" + uri + "\n?????????????????????" + ContentUriUtil.getPath(requireContext(), uri));
                }
            });

    ActivityResultLauncher<String> mGetContent2 = registerForActivityResult(new ActivityResultContracts.GetContent(),
            uri -> {
                // Handle the returned Uri
                if (uri != null) {
                    ????????????????????????.setSummary(ContentUriUtil.getPath(requireContext(), uri));
                    Settings.Global.putString(cr, "lock_sound",ContentUriUtil.getPath(requireContext(), uri));
                    ToastUtils.showShort(getString(R.string.low_battery_sound) + "????????????\n" + ContentUriUtil.getPath(requireContext(), uri));
                    LogUtils.i("??????Uri???" + uri + "\n?????????????????????" + ContentUriUtil.getPath(requireContext(), uri));
                }
            });

    ActivityResultLauncher<String> mGetContent3 = registerForActivityResult(new ActivityResultContracts.GetContent(),
            uri -> {
                // Handle the returned Uri
                if (uri != null) {
                    ????????????????????????.setSummary(ContentUriUtil.getPath(requireContext(), uri));
                    Settings.Global.putString(cr, "unlock_sound",ContentUriUtil.getPath(requireContext(), uri));
                    ToastUtils.showShort(getString(R.string.low_battery_sound) + "????????????\n" + ContentUriUtil.getPath(requireContext(), uri));
                    LogUtils.i("??????Uri???" + uri + "\n?????????????????????" + ContentUriUtil.getPath(requireContext(), uri));
                }
            });

    public SettingFragment() {

    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(mViewModel.class); // ??????????????????

        about = findPreference("about");
        AppCanter =findPreference("appcanter");
        ?????? = findPreference("opensource");
        ????????? =findPreference("power_sound");
        ???????????? = findPreference("low_battery_sound");
        ??????????????????????????? = findPreference("low_battery_sound_path");
        ????????? = findPreference("lock_sound");
        ???????????????????????? = findPreference("lock_sound_path");
        ????????? = findPreference("unlock_sound");
        ???????????????????????? = findPreference("unlock_sound_path");
        bugreply = findPreference("bugreply");

        ToastUtils.make().setDurationIsLong(true).setLeftIcon(R.drawable.ic_baseline_error_24).setGravity(Gravity.CENTER,0,0).setMode(ToastUtils.MODE.DARK).setTextSize(24).show("???????????????????????????");

        about.setOnPreferenceClickListener(this);
        AppCanter.setOnPreferenceChangeListener(this);
        ??????.setOnPreferenceClickListener(this);
        ?????????.setOnPreferenceChangeListener(this);
        ????????????.setOnPreferenceChangeListener(this);
        ???????????????????????????.setOnPreferenceClickListener(this);
        ?????????.setOnPreferenceChangeListener(this);
        ????????????????????????.setOnPreferenceClickListener(this);
        ?????????.setOnPreferenceChangeListener(this);
        ????????????????????????.setOnPreferenceClickListener(this);
        bugreply.setOnPreferenceClickListener(this);

        sp = requireContext().getSharedPreferences("status",MODE_PRIVATE);
        editor = requireContext().getSharedPreferences("status",MODE_PRIVATE).edit();

        cr = new ContentResolver(getContext()) {
            @Nullable
            @Override
            public String[] getStreamTypes(@NonNull Uri url, @NonNull String mimeTypeFilter) {
                return super.getStreamTypes(url, mimeTypeFilter);
            }
        };

        initConfig();
        initNullOggPath();
    }

    private void initNullOggPath() {
        //????????????
        InputStream is = getResources().openRawResource(R.raw.disable);
        FileIOUtils.writeFileFromIS(PathUtils.getExternalAppMusicPath() + "/" + "disable.ogg", is);
        NullOgg = PathUtils.getExternalAppMusicPath() + "/" + "disable.ogg";
    }

    private void initConfig() {
        editor.putString("low_battery_sound_path","/system/media/audio/ui/LowBattery.ogg").commit();
        editor.putString("lock_sound_path","/system/media/audio/ui/Lock.ogg").commit();
        editor.putString("unlock_sound_path","/system/media/audio/ui/Unlock.ogg").commit();
    }


    private int checkPermissionStatus(String permission) //????????????????????????
    {
        if (ContextCompat.checkSelfPermission(requireContext(),permission) != 0) {
            LogUtils.e(permission+" ?????????");

            ShellUtils.execCmd("sh "+ PathUtils.getInternalAppDataPath()+"/files/rish -c "
                    +"\"pm grant com.ma.powersoundswitch "
                    + Manifest.permission.WRITE_SECURE_SETTINGS
                    + "\" &",false);

        }else {
            LogUtils.i("?????????????????????"+sp.getAll());

        }
        return ContextCompat.checkSelfPermission(requireContext(),permission);
    }


    private void saveConfigInfo(){
        switch (RomUtils.getRomInfo().getName()){
            case "xiaomi":
            //case "":
                if (Settings.Global.getString(cr,"low_battery_sound").equals(sp.getString("low_battery_sound_path",""))) {
                    LogUtils.i("?????????" + RomUtils.getRomInfo().getName());
                }
                break;
            default:
                ToastUtils.make().setGravity(Gravity.CENTER,0,0).setMode(ToastUtils.MODE.DARK).show("??????????????????????????????"+RomUtils.getRomInfo().getName()+"\n\n??????????????????????????????");
                LogUtils.e("????????????????????????"+RomUtils.getRomInfo().getName());
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
                ToastUtils.showShort("?????????ogg????????????");

                mGetContent.launch("audio/ogg");
                //startActivityForResult(new Intent(Intent.ACTION_GET_CONTENT).setType("audio/ogg").addCategory(Intent.CATEGORY_OPENABLE), 666);

                break;
            case "lock_sound_path":
                ToastUtils.showShort("?????????ogg????????????");

                mGetContent2.launch("audio/ogg");

                break;
            case "unlock_sound_path":
                ToastUtils.showShort("?????????ogg????????????");
                mGetContent3.launch("audio/ogg");
                break;
            case "bugreply":
               /* try {
                    File file = new File(String.valueOf(LogUtils.getLogFiles().get(0)));

                    requireActivity().startActivity(new Intent(ACTION_SEND).putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file))
                            .putExtra(Intent.EXTRA_SUBJECT, AppUtils.getAppName() +" ????????????")
                            .putExtra(Intent.EXTRA_TEXT, "??????????????????????????????\n???????????????????????????????????????????????????????????????????\n???????????????????????????log???????????????????????????????????????" + "\n") //??????
                            .setData(Uri.parse("mailto:3207754367@qq.com")));

                }catch (IndexOutOfBoundsException outOfBoundsException){
                    LogUtils.e(outOfBoundsException.fillInStackTrace()+"\n"+LogUtils.getLogFiles());
                }*/
                openCustomTabs("https://privacy.mpcloud.top/valine");

                break;
            case "opensource":
                openCustomTabs("https://github.com/sbmatch/powersoundswitch");
                break;
            case "about":
                //ToastUtils.showShort(AppUtils.getAppVersionName());
                new AlertDialog.Builder(requireContext())
                        .setCancelable(false)
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
                                LogUtils.i("?????????");
                                Settings.Global.putInt(cr, "power_sounds_enabled", 1);
                                ToastUtils.showShort("?????????");
                                //ShellUtils.execCmd("sh "+PathUtils.getInternalAppDataPath()+"/files/rish -c "+"\"settings put global power_sounds_enabled 1\" &",false);
                            } else {
                                ToastUtils.showShort("?????????" + getString(R.string.power_sound));
                                //LogUtils.e(newValue);
                                Settings.Global.putInt(cr, "power_sounds_enabled",0);
                                //ShellUtils.execCmd("sh "+PathUtils.getInternalAppDataPath()+"/files/rish -c "+"\"settings put global power_sounds_enabled 0\" &",false);
                            }
                        LogUtils.e("??????????????????" + Settings.Global.getString(cr, "power_sounds_enabled"));
                        break;
                    case "low_battery_sound":
                        LogUtils.i(preference.getKey()+" is "+newValue);
                        if (((boolean) newValue)) {
                           // ToastUtils.showShort(getString(R.string.low_battery_sound) + "????????????\n" + Settings.Global.getString(cr, "low_battery_sound"));

                            /*viewModel.getCallString().observe(this, s -> {
                                Settings.Global.putString(cr, "low_battery_sound", s);
                                p2.setSummary(s);
                                ToastUtils.showShort(getString(R.string.low_battery_sound) + "????????????\n" + s);
                            });*/

                            Settings.Global.putString(cr, "low_battery_sound",sp.getString("low_battery_sound_path",""));
                            ???????????????????????????.setSummary("????????????????????????");
                            ToastUtils.showShort("?????????????????????");

                        }else {
                            Settings.Global.putString(cr, "low_battery_sound",NullOgg);
                            ToastUtils.showShort(getString(R.string.low_battery_sound) + "?????????");
                            ???????????????????????????.setSummary("?????????");
                        }
                        break;
                    case "lock_sound":
                        LogUtils.i(preference.getKey()+" is "+newValue);
                        if (((boolean) newValue)) {

                            Settings.Global.putString(cr, "lock_sound",sp.getString("lock_sound_path",""));
                            ????????????????????????.setSummary("????????????????????????");
                            ToastUtils.showShort("?????????????????????");
                            LogUtils.i("?????????????????????"+sp.getString("lock_sound_path",""));

                        }else {
                            Settings.Global.putString(cr, "lock_sound",NullOgg);
                            ToastUtils.showShort(getString(R.string.lock_sound) + "?????????");
                            ????????????????????????.setSummary("?????????");

                        }
                        break;
                    case "unlock_sound":
                        LogUtils.i(preference.getKey()+" is "+newValue);
                        if (((boolean) newValue)) {

                            /*viewModel.getCallString().observe(this, s -> {
                                Settings.Global.putString(cr, "unlock_sound", s);
                                p9.setSummary(s);
                                ToastUtils.showShort(getString(R.string.low_battery_sound) + "????????????\n" + s);
                            });*/

                            Settings.Global.putString(cr, "unlock_sound",sp.getString("unlock_sound_path",""));
                            ????????????????????????.setSummary("????????????????????????");
                            ToastUtils.showShort("?????????????????????");
                            LogUtils.i("?????????????????????"+sp.getString("unlock_sound",""));

                        }else {
                            Settings.Global.putString(cr, "unlock_sound",NullOgg);
                            ToastUtils.showShort(getString(R.string.lock_sound) + "?????????");
                            ????????????????????????.setSummary("?????????");
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
            ToastUtils.showShort("?????????");
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