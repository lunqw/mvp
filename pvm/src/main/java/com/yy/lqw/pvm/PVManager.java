package com.yy.lqw.pvm;

import android.util.Log;
import android.view.View;

import com.yy.lqw.pvm.annotations.PVM;

import java.lang.reflect.Constructor;

/**
 * Created by lunqingwen on 2017/3/9.
 */

public class PVManager {
    private static final String TAG = "PVManager";
    private static boolean sDebug = false;

    private PVManager() {
    }

    /**
     * 开启调试模式
     *
     * @param debug
     */
    public static void setDebug(boolean debug) {
        sDebug = debug;
    }

    /**
     * 绑定View和Present对象
     *
     * @param viewObject      MVP中的view对象, 必须含有PVM注解{@link PVM}，可以是Activity、Fragment、View，
     *                        又或者是用于单元测试的Mock对象
     * @param presenter       MVP中的presenter对象
     * @param lifeCycleObject lifeCycleObject对象，用于控制presenter的生命周期
     * @return true - bind成功， false - bind失败
     */
    public static boolean bind(final Object viewObject,
                               final Presenter presenter,
                               final View lifeCycleObject) {
        if (viewObject == null || presenter == null || lifeCycleObject == null) {
            throw new NullPointerException("viewObject, presenter and lifeCycleObject must not be null");
        }

        if (sDebug) {
            Log.d(TAG, "Create PVM binding, viewObject: " + viewObject
                    + ", presenter: " + presenter
                    + ", lifeCycleObject: " + lifeCycleObject);
        }

        final Class<?> viewClass = viewObject.getClass();
        final PVM pvmAnnotation = viewClass.getAnnotation(PVM.class);
        if (pvmAnnotation == null) {
            throw new IllegalArgumentException("@PVM annotation miss");
        }
        final Class<? extends Presenter> presenterClass = pvmAnnotation.presenter();
        try {
            final String delegateImplName = viewClass.getName()
                    + presenter.getClass().getSimpleName()
                    + "DelegateImpl";
            if (sDebug) {
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
            return true;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return false;
    }
}
