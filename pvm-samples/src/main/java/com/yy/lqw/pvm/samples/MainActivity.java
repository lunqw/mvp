package com.yy.lqw.pvm.samples;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.yy.lqw.pvm.PVManager;
import com.yy.lqw.pvm.Presenter;
import com.yy.lqw.pvm.annotations.PVM;
import com.yy.lqw.pvm.annotations.PVMSink;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@PVM({MainPresenter.class, UserPresenter.class, SettingPresenter.class})
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private Presenter[] mPresenters = {new MainPresenter(), new UserPresenter(), new SettingPresenter()};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PVManager.bind(this, Arrays.asList(mPresenters), getWindow().getDecorView());
    }

    @PVMSink
    void onGetProducts(List<Object> products) {
        Log.d(TAG, "onGetProducts");
    }

    @PVMSink(1)
    void onGetUserInfo(String nick, char sex, int age) {
        Log.d(TAG, "onGetUserInfo");
    }

    @PVMSink(2)
    void onGetUserSetting(Map<String, String> settings) {
        Log.d(TAG, "onGetUserSetting");
    }
}
