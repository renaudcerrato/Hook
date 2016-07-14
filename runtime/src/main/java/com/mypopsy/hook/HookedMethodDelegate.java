package com.mypopsy.hook;

import com.mypopsy.hook.annotations.Param;

import java.lang.reflect.Method;

class HookedMethodDelegate extends HookedMethod {

    private final HookedMethod instance;

    HookedMethodDelegate(HookedMethod instance) {
        this.instance = instance;
    }

    @Override
    public int getPosition(Param param) {
        return instance.getPosition(param);
    }

    @Override
    public Object proceed() throws Throwable {
        return instance.proceed();
    }

    @Override
    public Object proceed(Object... args) throws Throwable {
        return instance.proceed(args);
    }

    @Override
    public Method method() {
        return instance.method();
    }

    @Override
    public Object target() {
        return instance.target();
    }

    @Override
    public Object[] args() {
        return instance.args();
    }

    @Override
    public String toString() {
        return instance.toString();
    }

    final public HookedMethod delegate() {
        return instance;
    }
}