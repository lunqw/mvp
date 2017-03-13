package com.yy.lqw.pvm.samples;

import android.net.Uri;

import com.yy.lqw.pvm.PVManager;
import com.yy.lqw.pvm.Presenter;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by lunqingwen on 2017/3/9.
 */

public class MainPresenter extends Presenter {
    public void fetchUserInfo(final long uid) {
        final MainPresenterProxy proxy = (MainPresenterProxy) PVManager.INSTANCE.getProxy(this);
        final Uri portraitUri = Uri.parse("http://www.yy.com");
        proxy.onFetchUserInfo(uid, portraitUri, "lunqw");

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                proxy.onUserInfoChanged(uid, portraitUri, "lunqingwen");
            }
        }, 5000);
    }
}
