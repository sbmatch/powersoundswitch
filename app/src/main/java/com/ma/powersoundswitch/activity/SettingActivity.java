package com.ma.powersoundswitch.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.StatusBarManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.FragmentUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ReflectUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.ma.powersoundswitch.R;
import com.ma.powersoundswitch.fragment.SettingFragment;
import com.ma.powersoundswitch.fragment.mViewModel;
import com.microsoft.appcenter.AppCenter;
import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import rikka.shizuku.ShizukuBinderWrapper;
import rikka.shizuku.SystemServiceHelper;

public class SettingActivity extends AppCompatActivity implements View.OnClickListener {
    private FragmentManager fm;
    private FragmentTransaction transition;
    private mViewModel viewModel;
    private RewardedAd mRewardedAd;
    private final static String adTestId = "ca-app-pub-3940256099942544/5224354917";
    private final static String adId = "ca-app-pub-6149360771976686~7073426268";
    private final String TAG = "SettingActivity";
    private View view;
    private androidx.appcompat.app.AlertDialog.Builder builder;
    private TextView textView;
    private ListView vv;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(@NonNull InitializationStatus initializationStatus) {
            }
        });

       /* TTAdSdk.init(this, new TTAdConfig.Builder().appId("5261678").allowShowNotify(false).asyncInit(true).supportMultiProcess(true).directDownloadNetworkType(TTAdConstant.NETWORK_STATE_WIFI).build(), new TTAdSdk.InitCallback() {
            @Override
            public void success() {
                LogUtils.e("???????????????");
            }

            @Override
            public void fail(int i, String s) {

            }
        });*/

        super.onCreate(savedInstanceState);

        setContentView(R.layout.setting);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle(AppUtils.getAppName());
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        fm = getSupportFragmentManager();
        transition = fm.beginTransaction();

        FragmentUtils.add(fm,new SettingFragment(),R.id.fragmentContainerView);
        FragmentUtils.show(fm);

        viewModel = new ViewModelProvider(this).get(mViewModel.class);

        view = View.inflate(getBaseContext(),R.layout.dialog,null);
        textView = view.findViewById(R.id.textView1);

        setTextViewFlag(textView);

    }


    private void setTextViewFlag(TextView textView) {
        textView.setTypeface(Typeface.DEFAULT_BOLD);
        textView.setFocusable(true);
        textView.setTextIsSelectable(true);
        textView.setLongClickable(true);
        textView.setEnabled(true);
        textView.getPaint().setFlags(Paint.ANTI_ALIAS_FLAG);
    }

    @Override
    public void onClick(View v) {
        LogUtils.i(v.getId());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            viewModel.add(ContentUriUtil.getPath(this,data.getData()));
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        startActivity(new Intent().setComponent(new ComponentName("com.miui.home","com.miui.home.launcher.Launcher")));
        //ActivityUtils.startHomeActivity();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(500);
                    AppUtils.exitApp();
                    System.exit(0);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        return super.onKeyDown(keyCode, event);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        // getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.add(0,1,0,"???????????????").setIcon(R.drawable.ic_baseline_card_giftcard_24).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
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

        if (id == 1) {

            ToastUtils.showShort("???????????????...");

            AdRequest adRequest = new AdRequest.Builder().build();
            RewardedAd.load(this, adTestId, adRequest, new RewardedAdLoadCallback() {
                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    LogUtils.e("???????????????"+loadAdError.getCode());
                    //mDialog("??????????????????","???????????????"+loadAdError.getCode() +"\n\n"+loadAdError.getMessage());
                    ToastUtils.showShort(loadAdError.getMessage());

                   // ?????????();
                    super.onAdFailedToLoad(loadAdError);
                }

                @Override
                public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                    mRewardedAd = rewardedAd;
                    mDialog("??????","???????????????????????????");
                    super.onAdLoaded(rewardedAd);
                }
            });

            return true;
        }


        if (id == 2){
            try {

                Object systemService = getSystemService(Context.AUDIO_SERVICE);

                List list = Arrays.asList(Arrays.stream(systemService.getClass().getDeclaredMethods()).toArray());

                view  = View.inflate(getBaseContext(),R.layout.dialog,null);
                vv = view.findViewById(R.id.listview);
                ListAdapter listAdapter = new ArrayAdapter<Objects>(getBaseContext(),R.layout.dialog,R.id.textView1,list);
                vv.setAdapter(listAdapter);
                mAtDialog(systemService.getClass().getSimpleName()+"????????????",view);

            } catch (NullPointerException n) {
                n.fillInStackTrace();
                ToastUtils.showShort(n.getMessage());
                LogUtils.e(n.fillInStackTrace());
            }
        }

            return super.onOptionsItemSelected(item);
    }

    private void mAtDialog(String systemService,View view) {
        builder = new androidx.appcompat.app.AlertDialog.Builder(this)
                .setCancelable(false)
                .setView(view)
                .setTitle(systemService)
                .setNegativeButton(R.string.lab_submit, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        Dialog dialog = builder.show();
        dialog.show();
    }
/*

    private void ?????????() {

        TTAdNative mTTAdNative=TTAdSdk.getAdManager().createAdNative(this);
        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId(String.valueOf(947626923))
                .setRewardName("??????") //??????????????? ??????
                .setRewardAmount(1)  //??????????????? ??????
                .setUserID("test1")//tag_id
                .setMediaExtra("media_extra") //????????????
                .setOrientation(TTAdConstant.VERTICAL) //?????????????????????????????????????????????TTAdConstant.HORIZONTAL ??? TTAdConstant.VERTICAL
                .setAdLoadType(TTAdLoadType.PRELOAD)//???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
                .build();

        mTTAdNative.loadRewardVideoAd(adSlot, new TTAdNative.RewardVideoAdListener() {
            @Override
            public void onError(int i, String s) {

            }

            @Override
            public void onRewardVideoAdLoad(TTRewardVideoAd ttRewardVideoAd) {
                ttRewardVideoAd.showRewardVideoAd(SettingActivity.this,TTAdConstant.RitScenes.HOME_OPEN_BONUS, "scenes_test");

            }

            @Override
            public void onRewardVideoCached() {

            }

            @Override
            public void onRewardVideoCached(TTRewardVideoAd ttRewardVideoAd) {

            }
        });
    }
*/

    private void mDialog(String title, String msg) {
        new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton(R.string.lab_submit, (dialog, which) -> {

                    try {
                    if (mRewardedAd.getResponseInfo().getResponseId() != null) {
                            mRewardedAd.show(getParent(), new OnUserEarnedRewardListener() {
                                @Override
                                public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                                    ToastUtils.showShort("????????????(1/1) ?????????");
                                }
                            });

                    }else {
                        ToastUtils.showShort(mRewardedAd.getResponseInfo().getResponseId());
                        }
                    }catch (NullPointerException e){
                        LogUtils.e(e.fillInStackTrace());
                        //ToastUtils.showShort(e.fillInStackTrace().toString());
                    }
                })
                .setNegativeButton(R.string.lab_cancel, (dialog, which) -> {
                }).show();
    }
}
