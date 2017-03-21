package com.yy.lqw.pvm.samples;

import com.yy.lqw.pvm.Presenter;
import com.yy.lqw.pvm.Proxy;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by lunqingwen on 2017/3/15.
 */

public class LoginPresenter extends Presenter {
    private Executor mExecutor = Executors.newSingleThreadExecutor();
    private LoginPresenterProxy mProxy;

    @Override
    public void onAttachedToView(Proxy proxy) {
        mProxy = (LoginPresenterProxy) proxy;
    }

    @Override
    public void onDetachedFromView(Proxy proxy) {

    }

    public void login(final String passport, final String password) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (System.currentTimeMillis() % 2 == 0) {
                    mProxy.onLoginSuccess(passport + "-" + password);
                } else {
                    mProxy.onLoginFailed(1, "Unknown error");
                }
            }
        });
    }
}
