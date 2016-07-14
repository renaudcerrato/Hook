package com.mypopsy.hook.scenario;

import com.mypopsy.hook.annotations.Hook;
import com.mypopsy.hook.annotations.Hooked;
import com.mypopsy.hook.HookedMethod;
import com.mypopsy.hook.annotations.Param;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class SimpleParamScenario extends BaseScenario<Integer> {



    @Override
    public Integer result() {
        return add(0, 1, 2);
    }

    @Hooked("test")
    private Integer add(@Param("a") int a, @Param("b") int b, @Param("c") int c) {
        return a+b+c;
    }

    @Hook("test")
    Integer hook0(HookedMethod<Integer> method, int a, int b, int c) throws Throwable {
        assertThat(a, is(15));
        assertThat(b, is(16));
        assertThat(c, is(17));
        return method.proceed();
    }

    @Hook(value = "test", priority = 1)
    Integer hook1(HookedMethod<Integer> method, @Param("a") int a) throws Throwable {
        assertThat(a, is(12));
        return method.proceed(15,16,17);
    }

    @Hook(value = "test", priority = 2)
    Integer hook2(HookedMethod<Integer> method, @Param("b") int b) throws Throwable {
        assertThat(b, is(10));
        return method.proceed(12,13,14);
    }

    @Hook(value = "test", priority = 3)
    Integer hook3(HookedMethod<Integer> method, @Param("c") int c) throws Throwable {
        assertThat(c, is(8));
        return method.proceed(9,10,11);
    }

    @Hook(value = "test", priority = 4)
    Integer hook4(HookedMethod<Integer> method, @Param("b") int b, @Param("a") int a) throws Throwable {
        assertThat(a, is(3));
        assertThat(b, is(4));
        return method.proceed(6,7,8);
    }

    @Hook(value = "test", priority = 5)
    Integer hook5(HookedMethod<Integer> method, @Param("b") int b, @Param("a") int a, @Param("c") int c) throws Throwable {
        assertThat(a, is(0));
        assertThat(b, is(1));
        assertThat(c, is(2));
        return method.proceed(3,4,5);
    }

    @Override
    public Integer expected() {
        return 15+16+17;
    }
}
