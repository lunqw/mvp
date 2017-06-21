package com.yy.lqw.mvp;

import android.util.Log;
import android.view.View;

import com.yy.lqw.mvp.annotations.MVP;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by lunqingwen on 2017/3/9.
 */

public class MVPManager {
    private static final String TAG = "MVPManager";
    private static boolean sDebug = false;

    private MVPManager() {
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
     * @param lifeCycleObject lifeCycleObject对象，用于控制presenter的生命周期
     * @param viewObject      MVP中的view对象, 必须含有MVP注解{@link MVP}，可以是Activity、Fragment、View，
     *                        又或者是用于单元测试的Mock对象
     * @param presenters      MVP中的presenter对象
     * @return true - bind成功， false - bind失败
     */
    public static boolean bind(final Object viewObject,
                               final List<? extends Presenter> presenters,
                               final View lifeCycleObject) {
        if (viewObject == null || presenters == null || lifeCycleObject == null) {
            throw new NullPointerException("viewObject, presenters and lifeCycleObject must not be null");
        }

        if (presenters.size() == 0) {
            throw new IllegalArgumentException("presenters was empty");
        }

        if (sDebug) {
            Log.d(TAG, "MVP: create binding, viewObject: " + viewObject
                    + ", presenters: " + presenters
                    + ", lifeCycleObject: " + lifeCycleObject);
        }

        final Class<?> viewClass = viewObject.getClass();
        final MVP MVPAnnotation = viewClass.getAnnotation(MVP.class);
        if (MVPAnnotation == null) {
            throw new IllegalArgumentException("@MVP annotation miss");
        } else {
            final List<Class<? extends Presenter>> presenterClasses = Arrays.asList(MVPAnnotation.presenters());
            for (Presenter presenter : presenters) {
                if (!presenterClasses.contains(presenter.getClass())) {
                    throw new IllegalArgumentException("Presenter["
                            + presenter
                            + "] should declare in MVP annotation first");
                }
            }
        }

        try {
            final List<Delegate> delegates = new ArrayList<>();
            Class<? extends Delegate> delegateClass;
            Constructor<? extends Delegate> constructor;
            for (Presenter presenter : presenters) {
                final String delegateImplName = viewClass.getName()
                        + presenter.getClass().getSimpleName()
                        + "DelegateImpl";
                if (sDebug) {
                    Log.d(TAG, "MVP: Create DelegateImpl: " + delegateImplName);
                }
                delegateClass = Class.forName(delegateImplName)
                        .asSubclass(Delegate.class);
                constructor = delegateClass.getConstructor(viewClass);
                delegates.add(constructor.newInstance(viewObject));
            }

            lifeCycleObject.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
                @Override
                public void onViewAttachedToWindow(View v) {
                    for (int i = 0; i < delegates.size(); i++) {
                        presenters.get(i).onAttachedToView(delegates.get(i));
                    }
                }

                @Override
                public void onViewDetachedFromWindow(View v) {
                    for (int i = 0; i < delegates.size(); i++) {
                        presenters.get(i).onDetachedFromView(delegates.get(i));
                    }
                    lifeCycleObject.removeOnAttachStateChangeListener(this);
                }
            });
            return true;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return false;
    }

    public static boolean bind(final Object viewObject,
                               final Presenter presenter,
                               final View lifeCycleObject) {
        return bind(viewObject, Arrays.asList(presenter), lifeCycleObject);
    }
}
