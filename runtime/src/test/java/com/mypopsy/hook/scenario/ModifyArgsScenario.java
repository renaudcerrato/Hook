package com.mypopsy.hook.scenario;

import com.mypopsy.hook.annotations.Hook;
import com.mypopsy.hook.annotations.Hooked;
import com.mypopsy.hook.HookedMethod;

public class ModifyArgsScenario extends BaseScenario<String> {


    @Hook(value = "test", priority = 3)
    String hook3(HookedMethod<String> method) throws Throwable {
        return method.proceed("foo");
    }

    @Hook(value = "test", priority = 2)
    String hook2(HookedMethod<String> method, String string) throws Throwable {
        return method.proceed(string.replace("f", "z"));
    }

    @Hook(value = "test", priority = 1)
    String hook1(HookedMethod<String> method, String string) throws Throwable {
        return method.proceed(string.replace("oo", "orro"));
    }

    @Override
    public String result() {
        return call("abc");
    }

    @Hooked("test")
    private String call(String text) {
        return text.toUpperCase();
    }

    @Override
    public String expected() {
        return "ZORRO";
    }
}
