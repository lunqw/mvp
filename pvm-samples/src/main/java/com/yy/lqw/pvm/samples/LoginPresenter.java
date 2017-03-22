package com.yy.lqw.pvm.samples;

import com.yy.lqw.pvm.Delegate;
import com.yy.lqw.pvm.Presenter;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by lunqingwen on 2017/3/15.
 */

public class LoginPresenter implements Presenter {
    private Executor mExecutor = Executors.newSingleThreadExecutor();
    private LoginPresenterDelegate mDelegate;

    @Override
    public void onAttachedToView(Delegate delegate) {
        mDelegate = (LoginPresenterDelegate) delegate;
    }

    @Override
    public void onDetachedFromView(Delegate delegate) {
        // TODO: release resources
    }

    public void login(final String passport, final String password) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (System.currentTimeMillis() % 2 == 0) {
                    mDelegate.onLoginSuccess(passport + "-" + password);
                } else {
                    mDelegate.onLoginFailed(1, "Unknown error");
                }
            }
        });
    }
}
