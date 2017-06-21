package com.yy.lqw.mvp;

/**
 * Created by lunqingwen on 2017/3/13.
 */

public interface Presenter {
    void onAttachedToView(Delegate delegate);

    void onDetachedFromView(Delegate delegate);
}
