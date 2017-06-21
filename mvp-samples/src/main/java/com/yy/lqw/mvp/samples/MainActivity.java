package com.yy.lqw.mvp.samples;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.yy.lqw.mvp.MVPManager;
import com.yy.lqw.mvp.Presenter;
import com.yy.lqw.mvp.annotations.MVP;
import com.yy.lqw.mvp.annotations.MVPSink;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@MVP(presenters = {MainPresenter.class, UserPresenter.class, SettingPresenter.class})
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private Presenter[] mPresenters = {new MainPresenter(), new UserPresenter(), new SettingPresenter()};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MVPManager.bind(this, Arrays.asList(mPresenters), getWindow().getDecorView());
    }

    @MVPSink
    void onGetProducts(List<Object> products) {
        Log.d(TAG, "onGetProducts");
    }

    @MVPSink(ordinal = 1)
    void onGetUserInfo(String nick, char sex, int age) {
        Log.d(TAG, "onGetUserInfo");
    }

    @MVPSink(ordinal = 2)
    void onGetUserSetting(Map<String, String> settings) {
        Log.d(TAG, "onGetUserSetting");
    }
}
