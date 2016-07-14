package com.mypopsy.hook.scenario;

import com.mypopsy.hook.annotations.Register;
import com.mypopsy.hook.annotations.Unregister;

public abstract class BaseScenario<T> {

    @Register
    public void register() {
        System.out.println("register("+getClass()+")");
    }

    @Unregister
    public void unregister() {
        System.out.println("unregister("+getClass()+")");
    }

    public abstract T result();
    public abstract T expected();

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}