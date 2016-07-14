package com.mypopsy.hook.scenario;

import com.mypopsy.hook.annotations.Hook;
import com.mypopsy.hook.HookedMethod;
import com.mypopsy.hook.annotations.Hooked;

import static junit.framework.TestCase.assertTrue;

public class DoNotProceedScenario extends BaseScenario<Integer> {

    @Hooked("test")
    int add(int a, int b) {
        return a + b;
    }

    @Hook("test")
    int hook(HookedMethod<Integer> method) throws Throwable {
        assertTrue("we should not be there!", false);
        return 43;
    }

    @Hook(value = "test", priority = 1)
    int hook3(HookedMethod<Integer> method) throws Throwable {
        assertTrue("we should not be there!", false);
        return 44;
    }

    @Hook(value = "test", priority = 3)
    int hook1(HookedMethod<Integer> method) throws Throwable {
        return 42;
    }

    @Override
    public Integer result() {
        return add(1, 8);
    }

    @Override
    public Integer expected() {
        return 42;
    }
}
