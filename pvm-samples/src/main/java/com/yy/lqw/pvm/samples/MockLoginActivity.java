package com.yy.lqw.pvm.samples;

import com.yy.lqw.pvm.annotations.PVM;
import com.yy.lqw.pvm.annotations.PVMSink;

/**
 * Created by lunqingwen on 2017/3/15.
 */

@PVM(presenter = LoginPresenter.class)
public class MockLoginActivity {
    @PVMSink
    void onLoginSuccess(String token) {

    }

    @PVMSink
    void onLoginFailed(int code, String message) {

    }
}
