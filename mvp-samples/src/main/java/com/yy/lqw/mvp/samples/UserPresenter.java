package com.yy.lqw.mvp.samples;


import com.yy.lqw.mvp.Delegate;
import com.yy.lqw.mvp.Presenter;

/**
 * Created by lunqingwen on 2017/4/6.
 */

public class UserPresenter implements Presenter {
    private UserPresenterDelegate mDelete;

    @Override
    public void onAttachedToView(Delegate delegate) {
        mDelete = (UserPresenterDelegate) delegate;
        mDelete.onGetUserInfo("lunqw", 'M', 18);
    }

    @Override
    public void onDetachedFromView(Delegate delegate) {
    }
}
