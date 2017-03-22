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

    /**
     * @param viewObject      含有PVM注解的任意对象，可以是Activity、Fragment、View，
     *                        又或者是用于单元测试的Mock对象
     * @param lifeCycleObject View对象，用于控制presenter的生命周期
     * @return presenter Presenter对象
     */
    public Presenter bind(final Object viewObject, final View lifeCycleObject) {
        if (viewObject == null || lifeCycleObject == null) {
            throw new NullPointerException("viewObject or view was null");
        }
        Class<?> viewClass = viewObject.getClass();
        PVM pvmAnnotation = viewClass.getAnnotation(PVM.class);
        if (pvmAnnotation == null) {
            throw new IllegalArgumentException("@PVM annotation miss");
        }
        Presenter result = null;
        Class<? extends Presenter> presenterClass = pvmAnnotation.presenter();
        try {
            final Presenter presenter = (result = presenterClass.newInstance());
            final String delegateImplName = viewClass.getName()
                    + presenterClass.getSimpleName()
                    + "DelegateImpl";
            Class<? extends Delegate> delegateClass = Class.forName(delegateImplName)
                    .asSubclass(Delegate.class);
            Constructor<? extends Delegate> constructor = delegateClass.getConstructor(viewClass);
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
            Log.e(TAG, e.getMessage());
        }
        return result;
    }
}
