package com.mypopsy.hook.scenario;

import com.mypopsy.hook.HookedMethod;
import com.mypopsy.hook.annotations.Hook;
import com.mypopsy.hook.annotations.Hooked;
import com.mypopsy.hook.annotations.Target;

import org.hamcrest.core.Is;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class SimpleScenario extends BaseScenario<Integer> {

    @Hooked("test")
    int add(int a, int b) {
        return a + b;
    }


    @Hook("test")
    int hook(HookedMethod<Integer> method, @Target Object target) throws Throwable {
        assertThat(method.args().length, is(2));
        assertThat(method.target(), Is.<Object>is(this));
        assertThat((Integer)method.args()[0], is(1));
        assertThat((Integer)method.args()[1], is(8));
        assertThat(target, is(method.target()));
        return method.proceed() + 1;
    }

    @Override
    public Integer result() {
        return add(1, 8);
    }

    @Override
    public Integer expected() {
        return 9+1;
    }
}
