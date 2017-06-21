package com.yy.lqw.mvp.annotations;


import com.yy.lqw.mvp.Presenter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by lunqingwen on 2017/3/9.
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MVP {
    Class<? extends Presenter>[] presenters();
}
