package com.yy.lqw.pvm;

import android.view.View;

import com.yy.lqw.pvm.annotations.PVM;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by lunqingwen on 2017/3/9.
 */

public enum PVManager {
    INSTANCE;
    private Map<Presenter, Proxy> mViewProxyMap = new ConcurrentHashMap<>();

    /**
     * 绑定view对象，与presenter建立关联关系
     *
     * @param viewObject
     * @return presenter
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
            final String proxyName = viewClass.getName()
                    + presenterClass.getSimpleName()
                    + "ProxyImpl";
            Class<? extends Proxy> proxyClass = Class.forName(proxyName).asSubclass(Proxy.class);
            Constructor<? extends Proxy> constructor = proxyClass.getConstructor(viewClass);
            mViewProxyMap.put(presenter, constructor.newInstance(viewObject));
            lifeCycleObject.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
                @Override
                public void onViewAttachedToWindow(View v) {
                    presenter.onAttachedToView(viewObject);
                }

                @Override
                public void onViewDetachedFromWindow(View v) {
                    presenter.onDetachedFromView(viewObject);
                    lifeCycleObject.removeOnAttachStateChangeListener(this);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 获取presenter对应的Proxy对象
     *
     * @param presenter presenter
     * @return presenter对应的Proxy对象
     */
    public Proxy getProxy(Presenter presenter) {
        return mViewProxyMap.get(presenter);
    }
}
