package com.yy.lqw.mvp.samples;

import com.yy.lqw.mvp.Delegate;
import com.yy.lqw.mvp.Presenter;

import java.util.HashMap;

/**
 * Created by lunqingwen on 2017/4/6.
 */

public class SettingPresenter implements Presenter {
    private SettingPresenterDelegate mDelete;

    @Override
    public void onAttachedToView(Delegate delegate) {
        mDelete = (SettingPresenterDelegate) delegate;
        mDelete.onGetUserSetting(new HashMap<String, String>());
    }

    @Override
    public void onDetachedFromView(Delegate delegate) {
    }
}
