package com.yy.lqw.pvm.annotations;

import com.yy.lqw.pvm.Presenter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by lunqingwen on 2017/3/9.
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface PVM {
    Class<? extends Presenter>[] value();
}
