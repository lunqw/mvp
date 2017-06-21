package com.yy.lqw.mvp.samples;


import com.yy.lqw.mvp.Delegate;
import com.yy.lqw.mvp.Presenter;

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
