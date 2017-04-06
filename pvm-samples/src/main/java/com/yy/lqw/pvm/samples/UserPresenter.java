package com.yy.lqw.pvm.samples;

import com.yy.lqw.pvm.Delegate;
import com.yy.lqw.pvm.Presenter;

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
