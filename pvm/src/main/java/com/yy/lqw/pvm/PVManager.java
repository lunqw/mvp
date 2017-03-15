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
    private Map<Presenter, Proxy> mViewProxyMap = new ConcurrentHashMap<>();

    /**
     * 绑定view对象，与presenter建立关联关系
     *
     * @param viewObject
     * @return presenter
     */
    public Presenter bind(Object viewObject) {
        if (viewObject == null) {
            throw new NullPointerException("viewObject was null");
        }
        Presenter presenter = null;
        Class<?> viewClass = viewObject.getClass();
        PVM pvmAnnotation = viewClass.getAnnotation(PVM.class);
        if (pvmAnnotation == null) {
            throw new IllegalArgumentException("@PVM annotation miss");
        }
        Class<? extends Presenter> presenterClass = pvmAnnotation.presenter();
        try {
            presenter = presenterClass.newInstance();
            final String proxyName = viewClass.getName()
                    + presenterClass.getSimpleName()
                    + "ProxyImpl";
            Class<? extends Proxy> proxyClass = Class.forName(proxyName).asSubclass(Proxy.class);
            Constructor<? extends Proxy> constructor = proxyClass.getConstructor(viewClass);
            mViewProxyMap.put(presenter, constructor.newInstance(viewObject));
            presenter.onAttachedToView(viewObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return presenter;
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
