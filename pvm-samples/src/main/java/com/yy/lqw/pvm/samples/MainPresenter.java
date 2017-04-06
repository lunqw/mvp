package com.yy.lqw.pvm.samples;

import com.yy.lqw.pvm.Delegate;
import com.yy.lqw.pvm.Presenter;

/**
 * Created by lunqingwen on 2017/4/6.
 */

public class MainPresenter implements Presenter {
    private MainPresenterDelegate mDelete;

    @Override
    public void onAttachedToView(Delegate delegate) {
        mDelete = (MainPresenterDelegate) delegate;
        mDelete.onGetProducts(null);
    }

    @Override
    public void onDetachedFromView(Delegate delegate) {

    }
}
