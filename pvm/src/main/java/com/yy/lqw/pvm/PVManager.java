package com.yy.lqw.pvm;

import android.util.Log;
import android.view.View;

import com.yy.lqw.pvm.annotations.PVM;

import java.lang.reflect.Constructor;

/**
 * Created by lunqingwen on 2017/3/9.
 */

public enum PVManager {
    INSTANCE;

    private static final String TAG = "PVManager";
    private boolean mDebug = false;

    /**
     * 开启调试模式
     *
     * @param debug
     */
    public void setDebug(boolean debug) {
        mDebug = debug;
    }

    /**
     * 绑定View和Present对象
     *
     * @param view MVP中的view对象, 必须含有PVM注解{@link PVM}
     * @return Presenter对象，生存周期跟view一致
     */
    public Presenter bind(final View view) {
        return bind(view, view);
    }

    /**
     * 绑定View和Present对象
     *
     * @param viewObject      MVP中的view对象, 必须含有PVM注解{@link PVM}，可以是Activity、Fragment、View，
     *                        又或者是用于单元测试的Mock对象
     * @param lifeCycleObject lifeCycleObject对象，用于控制presenter的生命周期
     * @return Presenter对象，生存周期跟lifeCycleObject一致
     */
    public Presenter bind(final Object viewObject, final View lifeCycleObject) {
        if (viewObject == null || lifeCycleObject == null) {
            throw new NullPointerException("viewObject or view was null");
        }

        if (mDebug) {
            Log.d(TAG, "Create PVM binding, viewObject: " + viewObject
                    + ", lifeCycleObject: " + lifeCycleObject);
        }

        Presenter result = null;
        final Class<?> viewClass = viewObject.getClass();
        final PVM pvmAnnotation = viewClass.getAnnotation(PVM.class);
        if (pvmAnnotation == null) {
            throw new IllegalArgumentException("@PVM annotation miss");
        }
        final Class<? extends Presenter> presenterClass = pvmAnnotation.presenter();
        try {
            final Presenter presenter = (result = presenterClass.newInstance());
            final String delegateImplName = viewClass.getName()
                    + presenterClass.getSimpleName()
                    + "DelegateImpl";
            if (mDebug) {
                Log.d(TAG, "Create DelegateImpl: " + delegateImplName);
            }
            final Class<? extends Delegate> delegateClass = Class.forName(delegateImplName)
                    .asSubclass(Delegate.class);
            final Constructor<? extends Delegate> constructor = delegateClass.getConstructor(viewClass);
            final Delegate delegate = constructor.newInstance(viewObject);
            lifeCycleObject.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
                @Override
                public void onViewAttachedToWindow(View v) {
                    presenter.onAttachedToView(delegate);
                }

                @Override
                public void onViewDetachedFromWindow(View v) {
                    presenter.onDetachedFromView(delegate);
                    lifeCycleObject.removeOnAttachStateChangeListener(this);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return result;
    }
}
