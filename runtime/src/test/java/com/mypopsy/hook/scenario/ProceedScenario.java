package com.mypopsy.hook.scenario;

import com.mypopsy.hook.HookedMethod;
import com.mypopsy.hook.annotations.Hook;
import com.mypopsy.hook.annotations.Hooked;

public class ProceedScenario extends BaseScenario<Integer> {

    @Hooked("add")
    int add(int a, int b) {
        return a + b;
    }

    @Hook("add")
    int hook1(HookedMethod<Integer> method) throws Throwable {
        return method.proceed() + 1;
    }

    @Hook("add")
    int hook2(HookedMethod<Integer> method) throws Throwable {
        return method.proceed() + 1;
    }

    @Override
    public Integer result() {
        return add(1, 8);
    }

    @Override
    public Integer expected() {
        return 11;
    }
}
