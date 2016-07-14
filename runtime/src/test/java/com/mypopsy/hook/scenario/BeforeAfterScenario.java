package com.mypopsy.hook.scenario;

import com.mypopsy.hook.HookedMethod;
import com.mypopsy.hook.annotations.After;
import com.mypopsy.hook.annotations.Before;
import com.mypopsy.hook.annotations.Hook;
import com.mypopsy.hook.annotations.Hooked;
import com.mypopsy.hook.annotations.Param;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class BeforeAfterScenario extends BaseScenario<Integer> {

    private int token;

    @After("test")
    void after(String text, int i) {
        assertThat(text, is("foo"));
        assertThat(i, is(666));
        assertThat(token, is(5));
        token+=3;
    }

    @Before("test")
    void before(@Param("int") int i) {
        assertThat(i, is(666));
        assertThat(token, is(0));
        token = 5;
    }

    @Hook("test")
    Integer hook(HookedMethod<Integer> method) throws Throwable {
        assertThat(token, is(0));
        return method.proceed("foo", 666);
    }

    @Override
    public Integer result() {
        return call("abc", 42);
    }

    @Hooked("test")
    private Integer call(@Param("txt") String text, @Param("int") int foo) {
        assertThat(token, is(5));
        return 8;
    }

    @Override
    public Integer expected() {
        return token;
    }
}
