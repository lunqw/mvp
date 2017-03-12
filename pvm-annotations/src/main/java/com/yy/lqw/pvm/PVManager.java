package com.yy.lqw.pvm;

import com.yy.lqw.pvm.annotations.PVM;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by lunqingwen on 2017/3/9.
 */

public enum PVManager {
    INSTANCE;
    private Map<Object, Object> mViewProxyMap = new ConcurrentHashMap<>();

    public <T> T bind(Object view) {
        Presenter presenter = null;
        Class<?> viewClass = view.getClass();
        PVM pvmAnnotation = viewClass.getAnnotation(PVM.class);
        Class<? extends Presenter> presenterClass = pvmAnnotation.presenter();
        try {
            presenter = presenterClass.newInstance();
            final String proxyName = viewClass.getName()
                    + presenterClass.getSimpleName()
                    + "ProxyImpl";
            Class<?> proxyClass = Class.forName(proxyName);
            Constructor<?> constructor = proxyClass.getConstructor(viewClass);
            mViewProxyMap.put(presenter, constructor.newInstance(view));
            presenter.attach();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (T) presenter;
    }

    public <T> T getProxy(Object presenter) {
        return (T) mViewProxyMap.get(presenter);
    }
}
