package com.mypopsy.hook.scenario;

import com.mypopsy.hook.annotations.Hook;
import com.mypopsy.hook.annotations.Hooked;
import com.mypopsy.hook.HookedMethod;

public class OrderingScenario extends BaseScenario<String> {

    @Hook(value = "test", priority = 3)
    String hook3(HookedMethod<String> method) throws Throwable {
        return method.proceed()+"f";
    }

    @Hook(value = "test", priority = 2)
    String hook2(HookedMethod<String> method) throws Throwable {
        return method.proceed()+"e";
    }

    @Hook(value = "test", priority = 1)
    String hook1(HookedMethod<String> method) throws Throwable {
        return method.proceed()+"d";
    }

    @Override
    @Hooked("test")
    public String result() {
        return "abc";
    }

    @Override
    public String expected() {
        return "abcdef";
    }
}
