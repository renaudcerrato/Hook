package com.mypopsy.hook.scenario;

import com.mypopsy.hook.HookedMethod;
import com.mypopsy.hook.annotations.Hook;
import com.mypopsy.hook.annotations.Hooked;

public class ProceedWithArgsScenario extends BaseScenario<Integer> {

    @Hooked("test")
    int add(int a, int b) {
        return a + b;
    }

    @Hook("test")
    int hook(HookedMethod<Integer> method) throws Throwable {
        return method.proceed(1, 1);
    }

    @Override
    public Integer result() {
        return add(1, 8);
    }

    @Override
    public Integer expected() {
        return 2;
    }
}
