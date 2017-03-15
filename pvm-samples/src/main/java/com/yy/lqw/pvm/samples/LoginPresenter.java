package com.yy.lqw.pvm.samples;

import com.yy.lqw.pvm.PVManager;
import com.yy.lqw.pvm.Presenter;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by lunqingwen on 2017/3/15.
 */

public class LoginPresenter extends Presenter {
    private Executor mExecutor = Executors.newSingleThreadExecutor();
    private LoginPresenterProxy mProxy;

    @Override
    public void onAttachedToView(Object viewObject) {
        super.onAttachedToView(viewObject);
        mProxy = (LoginPresenterProxy) PVManager.INSTANCE.getProxy(this);
    }

    @Override
    public void onDetachedFromView(Object viewObject) {
        super.onDetachedFromView(viewObject);
    }

    public void login(String passport, String password) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (System.currentTimeMillis() % 2 == 0) {
                    mProxy.onLoginSuccess(toString());
                } else {
                    mProxy.onLoginFailed(1, "Unknown error");
                }
            }
        });
    }
}
